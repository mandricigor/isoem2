package edu.uconn.engr.dna.probability;



public abstract class ProbabilityDistribution {

	/**
	 * Generates an integer between 0 and n-1
	 * 
	 * @param n upper limit for the generated numbers
	 * @return an integer between 0 and n-1 taken from
	 * this probability distribution 
	 */
	public int generateInt(int n) {
		return generateInt()%n;
	}

	/**
	 * Generates a new integer number taken from this distribution
	 * 
	 * @return an integer value
	 */
	public abstract int generateInt();

	/**
	 * For a discrete distribution of items, returns the weigth
	 * of the ith item. These weights don't have to add up to 1,
	 * but they can be normalized afterwards.
	 * 
	 * When normalized they should reflect the probabilities of 
	 * items 1,2,...,i,...
	 * 
	 * @param i index of the item (starting with 0)
	 * @param n number of items in the series
	 * @return weight of item i
	 */
	public abstract double getWeight(int i, int n);
	
	/**
	 * Returns the mean of this distribution
	 * 
	 * @return the mean of this distribution
	 */
	public abstract double getMean();

	/**
	 * Returns the variance of this distribution
	 * 
	 * @return the variance of this distribution
	 */
	public abstract double getVariance();
}
