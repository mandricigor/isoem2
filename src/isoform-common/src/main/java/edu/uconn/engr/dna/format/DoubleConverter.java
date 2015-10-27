package edu.uconn.engr.dna.format;

import edu.uconn.engr.dna.util.Converter;

public class DoubleConverter implements Converter<String, Double>{

	@Override
	public Double convert(String item) throws IllegalStateException {
		try {
			return Double.parseDouble(item);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}
