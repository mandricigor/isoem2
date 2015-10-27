package edu.uconn.engr.dna.probability;

import java.util.Random;

public class CustomWeightProbabilityDistribution extends ProbabilityDistribution {

	private String fileName;
	private Random random;
	private int seed;
	
	public CustomWeightProbabilityDistribution(String fileName, int randomSeed) {
		this.fileName = fileName;
		this.random = new Random(randomSeed);
		this.seed = randomSeed;
	}
	
	@Override
	public int generateInt() {
		return random.nextInt();
	}	
	public String getFileName() {
		return fileName;
	}
	
	public String toString() {
		return "customWeights";
	}

	public int geRandomNumberGeneratorSeed() {
		return seed;
	}

	@Override
	public double getWeight(int i, int n) {
		throw new UnsupportedOperationException("Operation not available");
	}

	@Override
	public double getMean() {
		throw new UnsupportedOperationException("Operation not available");
	}
	
	@Override
	public double getVariance() {
		throw new UnsupportedOperationException("Operation not available");
	}	
	
}
