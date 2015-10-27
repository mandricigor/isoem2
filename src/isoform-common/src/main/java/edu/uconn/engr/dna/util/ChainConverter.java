package edu.uconn.engr.dna.util;


/**
 * Succesively applies a chain of converters to the input 
 * 
 * @author marius
 *
 * @param <T>
 */
public class ChainConverter<T> implements Converter<T, T> {

	private Converter<T, T>[] converters;
	public ChainConverter(Converter<T,T>... converters) {
		this.converters = converters;
	}
	
	public T convert(T item) throws IllegalStateException {
		for (int i = 0; i < converters.length; ++i) {
			item = converters[i].convert(item);
		}
		return item;
	}
}
