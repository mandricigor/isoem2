package edu.uconn.engr.dna.util;

public interface GroupedRandomAccessMap<T, V, R> extends RandomAccessMap<T,R> {
	
	void add(T key, V value);
	
}
