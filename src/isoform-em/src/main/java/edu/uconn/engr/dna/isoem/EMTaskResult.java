package edu.uconn.engr.dna.isoem;

import java.util.Map;

public class EMTaskResult {
    private Map<String, Double> frequencies;
    private long sortTime;
    private long readClassPartitionTime;
    private long emTime;
    private int coords;
    private int emSteps;
    private Map<Integer, IsoformList> isosForRead;
    private Map<Integer, Integer> fragmentLengthCounts;

    public synchronized Map<String, Double> getFrequencies() {
	return frequencies;
    }

    public synchronized Map<Integer, IsoformList> getIsosForRead() {
	return isosForRead;
    }

    public synchronized void setIsosForRead(
	    Map<Integer, IsoformList> isosForRead) {
	this.isosForRead = isosForRead;
    }

    public synchronized void setFrequencies(Map<String, Double> freq) {
	this.frequencies = freq;
    }

    public synchronized void setSortTime(long sortTime) {
	this.sortTime = sortTime;
    }

    public synchronized void setReadClassPartitionTime(
	    long readClassPartitionTime) {
	this.readClassPartitionTime = readClassPartitionTime;
    }

    public synchronized long getReadClassPartitionTime() {
	return readClassPartitionTime;
    }

    public synchronized long getSortTime() {
	return sortTime;
    }

    public synchronized void setEmTime(long emTime) {
	this.emTime = emTime;
    }

    public synchronized long getEmTime() {
	return emTime;
    }

    public synchronized void setCoords(int size) {
	this.coords = size;
    }

    public synchronized int getCoords() {
	return coords;
    }

    public synchronized void setEmSteps(int steps) {
	this.emSteps = steps;
    }

    public synchronized int getEmSteps() {
	return emSteps;
    }

    public void setFragmentLengthCounts(
	    Map<Integer, Integer> fragmentLengthCounts) {
	this.fragmentLengthCounts = fragmentLengthCounts;
    }

    public Map<Integer, Integer> getFragmentLengthCounts() {
	return fragmentLengthCounts;
    }

}
