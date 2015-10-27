package edu.uconn.engr.dna.util;

import java.util.Collection;

public interface MultiMap<K, V> {
	
	Collection<V> values();

	Collection<V> get(K key);

	boolean isEmpty();

	/**
	 * Removes and returns a random element
	 * from the set of elements associated with this key
	 * 
	 * @param key
	 * @return
	 */
	V removeOne(K key);
	
	V remove(K key, V value);

	void put(K key, V value);

	void clear();
	
	int size();

}
