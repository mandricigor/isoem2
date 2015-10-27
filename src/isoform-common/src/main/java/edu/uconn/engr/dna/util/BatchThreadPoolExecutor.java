package edu.uconn.engr.dna.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BatchThreadPoolExecutor<T, R> {

	private final Queue<T> workQueue;
	private final ParameterRunnableFactory<T, R> runnableFactory;
	private final Queue<ParameterRunnable<T, R>> recycledRunnables;
	private final ThreadPoolExecutor executor;
	private Queue<T> processedItemsQueue;
	protected List<R> results;
	protected boolean callingThreadCanBeUsedToRunTasks;

	public static <T, R> BatchThreadPoolExecutor<T, R> newInstance(int maximumPoolSize,
			ParameterRunnableFactory<T, R> runnableFactory) {
		return new BatchThreadPoolExecutor(maximumPoolSize,
				new ArrayBlockingQueue<T>(Math.max(100, 20 * maximumPoolSize)),
				runnableFactory, false);
	}

	public static <T, R> BatchThreadPoolExecutor<T, R> newInstance(int maximumPoolSize,
			Queue<T> workQueue,
			ParameterRunnableFactory<T, R> runnableFactory,
			boolean makeItemsAvailableAfterProcessing) {
		return new BatchThreadPoolExecutor(maximumPoolSize, workQueue,
				runnableFactory, makeItemsAvailableAfterProcessing);
	}

	public BatchThreadPoolExecutor(int maximumPoolSize,
			Queue<T> workQueue,
			ParameterRunnableFactory<T, R> runnableFactory,
			boolean makeItemsAvailableAfterProcessing) {
		this(maximumPoolSize, workQueue, runnableFactory, makeItemsAvailableAfterProcessing, null);
	}

	public BatchThreadPoolExecutor(int maximumPoolSize,
			Queue<T> workQueue,
			ParameterRunnableFactory<T, R> runnableFactory,
			boolean makeItemsAvailableAfterProcessing,
			ParameterRunnable<R, ?> finishedRunnableResultsCollector) {
		this.executor = new ThreadPoolExecutor(0, maximumPoolSize, 60, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(), createRejectedExecutionHandler()) {

			protected void terminated() {
				BatchThreadPoolExecutor.this.terminated();
			}
		};

		this.workQueue = workQueue;
		this.runnableFactory = runnableFactory;
		this.recycledRunnables = new ConcurrentLinkedQueue<ParameterRunnable<T, R>>();
		if (makeItemsAvailableAfterProcessing) {
			this.processedItemsQueue = new ConcurrentLinkedQueue<T>();
		}
		this.callingThreadCanBeUsedToRunTasks = true;
	}

	public BatchThreadPoolExecutor<T, R> process(T item) {
		executor.execute(new ItemRunnable(item));
		return this;
	}

	public BatchThreadPoolExecutor<T, R> processAll(List<T> items) {
		for (T item : items) {
			process(item);
		}
		return this;
	}

	public List<R> waitForTermination() throws InterruptedException {
//        System.out.println("Wait for termination; queue size is now "
//                + workQueue.size()
//                + " threads in pool " + getPoolSize());
		executor.shutdown();
		processedItemsQueue = null;
		while (!executor.isTerminated()) {
			executor.awaitTermination(500, TimeUnit.MILLISECONDS);
		}
		return results;
	}

	protected void rejected(Runnable r) {
		T item = ((ItemRunnable) r).getItem();
		if (callingThreadCanBeUsedToRunTasks) {
			if (!workQueue.offer(item)) {
				ParameterRunnable<T, R> worker = getRunnable();
				runOne(worker, item);
				recycledRunnables.add(worker);
			}
		} else {
			if (workQueue instanceof BlockingQueue) {
				try {
					((BlockingQueue) workQueue).put(item);
					return;
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

			// if nonblocking queue, or put failed, loop
			while (!workQueue.offer(item)) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

		}
	}

	private void runUntilQueueEmpty(ParameterRunnable<T, ?> r, T item) {
		do {
			runOne(r, item);
		} while (null != (item = workQueue.poll()));
	}

	private void runOne(ParameterRunnable<T, ?> r, T item) {
		r.run(item);
		if (processedItemsQueue != null) {
			processedItemsQueue.offer(item);
		}
	}

	private ParameterRunnable<T, R> getRunnable() {
		ParameterRunnable<T, R> r = recycledRunnables.poll();
		if (r == null) {
			return runnableFactory.createParameterRunnable();
		} else {
			return r;
		}
	}

	protected void terminated() {
		results = new ArrayList<R>();
		for (ParameterRunnable<T, R> worker; null != (worker = recycledRunnables.poll());) {
			results.add(done(worker));
		}
	}

	protected R done(ParameterRunnable<T, R> r) {
		return r.done();
	}

	public int getPoolSize() {
		return executor.getPoolSize();
	}

	public T pollProcessedItemsQueue() {
		return processedItemsQueue.poll();
	}

	public boolean isCallingThreadCanBeUsedToRunTasks() {
		return callingThreadCanBeUsedToRunTasks;
	}

	public void setCallingThreadCanBeUsedToRunTasks(boolean callingThreadCanBeUsedToRunTasks) {
		this.callingThreadCanBeUsedToRunTasks = callingThreadCanBeUsedToRunTasks;
	}

	class ItemRunnable implements Runnable {

		private T item;

		public ItemRunnable(T item) {
			this.item = item;
		}

		@Override
		public void run() {
			ParameterRunnable<T, R> r = getRunnable();
			runUntilQueueEmpty(r, item);
			recycledRunnables.add(r);
		}

		public T getItem() {
			return item;
		}
	}

	private RejectedExecutionHandler createRejectedExecutionHandler() {
		return new RejectedExecutionHandler() {

			@Override
			public void rejectedExecution(Runnable r,
					ThreadPoolExecutor executor) {
				rejected(r);
			}
		};
	}
}
