package edu.uconn.engr.dna.io;


public interface Parser<T> {
	
	T parse (String in) throws Exception;

}
