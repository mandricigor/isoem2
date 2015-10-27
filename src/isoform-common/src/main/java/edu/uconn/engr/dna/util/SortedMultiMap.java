package edu.uconn.engr.dna.util;

import java.util.Collection;


public interface SortedMultiMap<K, V> extends MultiMap<K, V>{

	Collection<V> getAllValuesForKeysSmallerOrEqualTo(
			K key);


}
