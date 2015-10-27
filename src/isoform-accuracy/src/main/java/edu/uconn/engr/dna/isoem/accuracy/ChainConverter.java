package edu.uconn.engr.dna.isoem.accuracy;

import edu.uconn.engr.dna.util.Converter;

public class ChainConverter<R, T> implements Converter<R, T> {

	private Converter<R, ?>[] converters;

	public ChainConverter(Converter<R,?>... converters) {
		this.converters = converters;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T convert(R item) throws IllegalStateException {
		for (int i = 0; i < converters.length; ++i) {
			try {
				return (T)converters[i].convert(item);
			} catch (Exception e) {
				// do nothing (try the next converter)
			}
		}
		throw new IllegalStateException("No converter succeded on input " + item);
	}
}
