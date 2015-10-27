package edu.uconn.engr.dna.isoem.accuracy;

import edu.uconn.engr.dna.util.Converter;


public class LongConverter implements Converter<String, Long>{

	@Override
	public Long convert(String item) throws IllegalStateException {
		try {
			return Long.parseLong(item);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}
