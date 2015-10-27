package edu.uconn.engr.dna.io;

import java.io.InputStream;

public interface Parser<T> {
	
	T parse (InputStream in) throws Exception;

}
