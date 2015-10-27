package edu.uconn.engr.dna.util;


public interface Converter<R, T> {
	
	T convert(R item) throws IllegalStateException;

}
