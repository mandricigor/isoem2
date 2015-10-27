package edu.uconn.engr.dna.isoem;

import edu.uconn.engr.dna.util.BatchThreadPoolExecutor;
import edu.uconn.engr.dna.util.ParameterRunnableFactory;
import edu.uconn.engr.dna.util.SimpleTokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ThreadPooledSamParser<T> {

//    private static Logger log = Logger
//            .getLogger(ThreadPooledSamParser.class);

    private char delimitator = '\t';
    private String commentMarker = "@";
    private int maxThreads;
    private int maxBufferSize;
    private int queueSize;
    private ParameterRunnableFactory<List<String>, T> processorsFactory;
    private int readLength;
	private int readLimit;

    public ThreadPooledSamParser(int maxThreads,
                                 int queueSize,
                                 int maxBufferSize,
                                 ParameterRunnableFactory<List<String>, T> processorsFactory) {
		this(maxThreads, queueSize, maxBufferSize, processorsFactory, Integer.MAX_VALUE);
	}
	
    public ThreadPooledSamParser(int maxThreads,
                                 int queueSize,
                                 int maxBufferSize,
                                 ParameterRunnableFactory<List<String>, T> processorsFactory,
								 int readLimit) {
        this.maxThreads = maxThreads;
        this.queueSize = queueSize;
        this.maxBufferSize = maxBufferSize;
        this.processorsFactory = processorsFactory;
        this.readLength = -1;
		this.readLimit = readLimit;
    }

    public List<T> parse(Reader input) throws IOException, InterruptedException {
        BufferedReader reader;
        if (input instanceof BufferedReader)
            reader = (BufferedReader) input;
        else
            reader = new BufferedReader(input);

        String line;
        while ((null != (line = reader.readLine())) && line.startsWith(commentMarker))
            ;

        if (line == null)
            throw new IllegalStateException("Input is empty!");
//debug
//        System.out.print(" lines: ");
//        int lineCount = 0;
//        int linePrintThreshold = 1000000;
//        int readNumber = 0;
//debug end
        int stringsPerBatch = maxBufferSize - 20;

        BlockingQueue<List<String>> queue = new ArrayBlockingQueue<List<String>>(queueSize);
        BatchThreadPoolExecutor<List<String>, T> es = new BatchThreadPoolExecutor<List<String>, T>(
                maxThreads, queue, processorsFactory, true);

        List<String> l = new ArrayList<String>(maxBufferSize);

        String prevLine = " z z z z z z z z z z z z z z z z z z z z z z z ";
        int prevReadNameLengthPlus1 = prevLine.length();
		int readNumber = 0;
        do {
            if (line.isEmpty() || line.startsWith(commentMarker))
                continue;

//debug
//            ++lineCount;
//            if (lineCount % linePrintThreshold == 0) {
//                System.out.println(lineCount / linePrintThreshold + "M ");
//                int s = es.getPoolSize();
//                System.out.println("Thread pool size " + s + " queue size "
//                        + queue.size());
//            }
//debug end

            if (readLength == -1) {
                readLength = getReadLength(line);
            }

            if (!line.regionMatches(0, prevLine, 0, prevReadNameLengthPlus1)) {
                ++readNumber;
                // new read
                if (l.size() >= stringsPerBatch || readNumber >= readLimit) {
                    es.process(l);
                    l = es.pollProcessedItemsQueue();
                    if (l == null)
                        l = new ArrayList<String>(maxBufferSize);
                    else
                        l.clear();
                } else
                    // mark the end of a read group
                    l.add(null);

				if (readNumber >= readLimit) {
					break;
				}
                prevLine = line;
                prevReadNameLengthPlus1 = prevLine.indexOf(delimitator) + 1;
            }
            l.add(line);
        } while (null != (line = reader.readLine()));
        reader.close();
        if (l.size() > 0)
            es.process(l);
        return es.waitForTermination();
//        log.debug("READS: " + readNumber);
    }

    private int getReadLength(String line) {
		try {
        SimpleTokenizer tokenizer = new SimpleTokenizer(delimitator);
        tokenizer.setLine(line);
        tokenizer.skipNext(9); // skip readName, flags, chromosome, alignmentStart, mapq, cigar, mrnm, matePosition, isize
        String sequence = tokenizer.nextString();
        return sequence.length();
		} catch (Exception e) {
			System.err.println("LINE: " + line);
			e.printStackTrace();
			return 1;
		}
    }

    public int getReadLength() {
        return readLength;
    }

    public char getDelimitator() {
        return delimitator;
    }

    public void setDelimitator(char delimitator) {
        this.delimitator = delimitator;
    }

    public String getCommentMarker() {
        return commentMarker;
    }

    public void setCommentMarker(String commentMarker) {
        this.commentMarker = commentMarker;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getMaxBufferSize() {
        return maxBufferSize;
    }

    public void setMaxBufferSize(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

}