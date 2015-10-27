package edu.uconn.engr.dna.isoem;

import edu.uconn.engr.dna.probability.NormalProbabilityDistribution;
import edu.uconn.engr.dna.probability.ProbabilityDistribution;

public class DatasetParameters {
	private final boolean isFirstReadFromCodingStrand;
	private final boolean pairedReads;
	private final int readLength;
	private ProbabilityDistribution pd;

	public DatasetParameters(ProbabilityDistribution pd, boolean isFirstReadFromCodingStrand,
			boolean pairedReads, int readLength) {
		this.pd = pd;
		this.isFirstReadFromCodingStrand = isFirstReadFromCodingStrand;
		this.pairedReads = pairedReads;
		this.readLength = readLength;
	}
	
	public static DatasetParameters parseParams(String samReadsFile, boolean ignoreCodingStrandInfo) {
		String marker = "fragLenDistrib=normal_";
		
		int index = samReadsFile.indexOf(marker) + marker.length();
		int nextStop = samReadsFile.indexOf('_', index+1);
		double fragmentLengthMean = Double.parseDouble(samReadsFile.substring(index, nextStop));
		
		index = nextStop +1;
		nextStop = samReadsFile.indexOf('_', index);
		double fragmentLengthStdDev = Double.parseDouble(samReadsFile.substring(index, nextStop));
		
		boolean isFirstReadFromCodingStrand = samReadsFile.contains("_firstReadOrigin=codingStrand_");
		boolean pairedReads = samReadsFile.contains("_paired");
		
		marker = "_rl=";
		index = samReadsFile.indexOf(marker) + marker.length();
		nextStop = samReadsFile.indexOf('_', index+1);
		int readLength = Integer.parseInt(samReadsFile.substring(index, nextStop));
		return new DatasetParameters(new NormalProbabilityDistribution(fragmentLengthMean, fragmentLengthStdDev, 0xcafebabe), 
				isFirstReadFromCodingStrand, pairedReads, readLength);
	}

	public boolean isFirstReadFromCodingStrand() {
		return isFirstReadFromCodingStrand;
	}

	public boolean isPairedReads() {
		return pairedReads;
	}
	
	public ProbabilityDistribution getProbabilityDistribution() {
		return pd;
	}
	
	public void setProbabilityDistribution(ProbabilityDistribution pd) {
		this.pd = pd;
	}

	public boolean isIgnoreCodingStrandInfo() {
		return false;
	}

	public int getReadLength() {
		return readLength;
	}
	
}
