package edu.uconn.engr.dna.probability;

/**
 * An interface for classes which choose an item from a collection
 * based on some probabilistic distribution.
 * 
 * @author marius
 *
 * @param <T>
 * @param <V>
 */
public interface RandomPicker<T, V> {
	
	/**
	 * Chooses and returns an item; the info
	 * parameter offers additional information
	 * necessary for making the choice
	 * 
	 * @param info a parameter relevant to the choice 
	 * making process
	 * @return a chosen item
	 */
	T pick(V info);

}
