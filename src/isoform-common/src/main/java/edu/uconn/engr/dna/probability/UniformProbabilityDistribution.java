package edu.uconn.engr.dna.probability;

import java.util.Random;

/**
 * Random number generator for uniform probability distributions
 * 
 * @author Marius
 *
 */
public class UniformProbabilityDistribution extends ProbabilityDistribution {

	private Random r;

	public UniformProbabilityDistribution(long seed) {
		r = new Random(seed);
	}
	
	@Override
	public int generateInt() {
		return r.nextInt();
	}
	
	@Override
	public int generateInt(int n) {
		return r.nextInt(n);
	}
	
	@Override
	public String toString() {
		return "uniform";
	}

	@Override
	public double getMean() {
		throw new UnsupportedOperationException("Operation not available");
	}

	@Override
	public double getVariance() {
		throw new UnsupportedOperationException("Operation not available");
	}
	
	@Override
	public double getWeight(int i, int n) {
		return 1.0 / n;
	}
}
