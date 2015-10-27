package edu.uconn.engr.dna.util;


public class DefaultRandomAccessMap<K> extends AbstractRandomAccessMap<K, Double, Double> {

	@Override
	protected Double addToExistingGroupOrCreateNew(K key, Double cluster,
			Double value) {
		return value;
	}

}
