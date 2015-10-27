package edu.uconn.engr.dna.util;

public class SimpleRandomAccessMap<K> extends AbstractRandomAccessMap<K, String, String> {
	
	@Override
	protected String addToExistingGroupOrCreateNew(K key, String cluster, String value) {
		return value;
	}

}
