package edu.uconn.engr.dna.isoem.accuracy;

import edu.uconn.engr.dna.util.Converter;

public class EnumConverter<T extends Enum<T>> implements Converter<String, T>{

	private Class<T> enumClass;

	public EnumConverter(Class<T> enumClass) {
		this.enumClass = enumClass;
	}

	@Override
	public T convert(String item) throws IllegalStateException {
		try {
			return Enum.valueOf(enumClass, item);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
}
