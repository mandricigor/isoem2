package edu.uconn.engr.dna.util;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class SingleBatchThreadPoolExecutor<T, R>
        implements ParameterRunnable<T, Void> {
    private BatchThreadPoolExecutor<T,R> threadPoolExecutor;


    public SingleBatchThreadPoolExecutor(ParameterRunnable<T, R> runnable,
                                         int workQueueLength) {
		this(runnable, new ArrayBlockingQueue<T>(workQueueLength));
	}
    public SingleBatchThreadPoolExecutor(ParameterRunnable<T, R> runnable,
                                         Queue<T> workQueue) {
        this(runnable, workQueue, false, null);
    }

    public SingleBatchThreadPoolExecutor(ParameterRunnable<T, R> runnable,
                                         Queue<T> workQueue,
                                         boolean makeItemsAvailableAfterProcessing,
                                         ParameterRunnable<R, ?> finishedItemsResultsCollector) {
        this.threadPoolExecutor = new BatchThreadPoolExecutor<T,R>(1,
              workQueue,
              createSingleItemFactory(runnable),
              makeItemsAvailableAfterProcessing,
              finishedItemsResultsCollector);
        threadPoolExecutor.setCallingThreadCanBeUsedToRunTasks(false);
    }


    private static <T, R> ParameterRunnableFactory<T, R> createSingleItemFactory(final ParameterRunnable<T, R> runnable) {
        return new ParameterRunnableFactory<T, R>() {
            private boolean factoryCalled;

            @Override
            public synchronized ParameterRunnable<T, R> createParameterRunnable() {
                if (factoryCalled)
                    throw new IllegalStateException("This factory should have been called only once!");
                factoryCalled = true;
                return runnable;
            }
        };
    }

    @Override
    public void run(T item) {
        threadPoolExecutor.process(item);
    }

    @Override
    public Void done() {
		return null;
    }

    public R waitForTermination() throws InterruptedException {
        return threadPoolExecutor.waitForTermination().get(0);
    }
}
