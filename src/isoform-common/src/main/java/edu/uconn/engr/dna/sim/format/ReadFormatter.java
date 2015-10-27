package edu.uconn.engr.dna.sim.format;

import edu.uconn.engr.dna.sim.Read;

public interface ReadFormatter {

	String format(Read read);
	
	/**
	 * Returns a String describing the fields displayed for each 
	 * line obtained with {@link #format(edu.uconn.engr.dna.sim.Read)}.
	 * Should start with # and end with \n
	 * ex: # isoformId k s1,s2,...,sk, e1,e2,...ek, 
	 * 
	 * @return a String representing the table header
	 */
	String getTableHeader();

}
