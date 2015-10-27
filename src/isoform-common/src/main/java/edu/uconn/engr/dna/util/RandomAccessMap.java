package edu.uconn.engr.dna.util;

import java.util.Map;

public interface RandomAccessMap<T, R> extends Map<T,R> {

	R getValue(T key);

	T getKey(int index);
	
	R removeValue(T key);
	
	Integer getIndexOf(T key);
	
	Iterable<T> idIterator();

	Iterable<R> groupIterator();
	
}
