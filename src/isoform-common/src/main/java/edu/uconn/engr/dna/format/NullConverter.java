package edu.uconn.engr.dna.format;

import edu.uconn.engr.dna.util.Converter;

public class NullConverter<T> implements Converter<T, T> {
	
	public T convert(T item) throws IllegalStateException {
		return item;
	};

}
