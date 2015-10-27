package edu.uconn.engr.dna.util;

public class StringToDoubleRandomAccessMap<K> extends AbstractRandomAccessMap<K, String, Double> {
	
	@Override
	protected Double addToExistingGroupOrCreateNew(K key, Double cluster,
		String value) {
		return Double.parseDouble(value.toString());
	}

}
