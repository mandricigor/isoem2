package edu.uconn.engr.dna.probability;

import java.util.Random;

public class GeometricProbabilityDistribution extends ProbabilityDistribution {

	private Random random;
	private double p;
	
	/**
	 * Constructs a new geometric probability distribution with the
	 * given probability of success
	 *  
	 * @param p probability of success in one Bernoulli trial
	 * @param seed random number generator seed
	 */
	public GeometricProbabilityDistribution(double p, long seed) {
		this.random = new Random(seed);
		this.p = p;
	}
	
	@Override
	public int generateInt() {
		return (int)Math.floor( Math.log( random.nextDouble()) / Math.log( 1.0D - p ) );
	}
	
	@Override
	public String toString() {
		return String.format("geometric_%.2f", p);
	}


	@Override
	public double getMean() {
		return (1-p)/p;
	}
	
	@Override
	public double getVariance() {
		return (1-p)/(p*p);
	}

	@Override
	public double getWeight(int i, int n) {
		if (i == n-1) {
			--i; 
		}
		return Math.pow(1-p, i)*p;
	}
}
