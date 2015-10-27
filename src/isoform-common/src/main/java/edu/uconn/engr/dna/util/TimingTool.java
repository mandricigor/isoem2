package edu.uconn.engr.dna.util;

public class TimingTool {

    private long start;
    private long end;
    private long firstStart;
    private boolean firstTime = true;
    private long lastEnd;

    public void start(String message) {
	if (message != null) {
	    System.out.println(message);
	}
	start = System.currentTimeMillis();
	if (firstTime) {
	    firstStart = start;
	    firstTime = false;
	}
    }

    public void stopAndPrintTime() {
	stop();
	printTime();
    }

    public long stop() {
	end = System.currentTimeMillis();
	lastEnd = end;
	return end - start;
    }

    public void printTime() {
	System.out.printf("Done in %.3fs\n", (end - start) / 1000.0);
    }

    public void printGlobalTime() {
	System.out.printf("All done in %.3fs\n",
		(lastEnd - firstStart) / 1000.0);
    }

    public void reset() {
	firstTime = true;
    }

    public long getLastTime() {
	return end - start;
    }

    public long getGlobalTime() {
	return lastEnd - firstStart;
    }
}
