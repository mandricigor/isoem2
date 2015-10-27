package edu.uconn.engr.dna.genread.io;

import edu.uconn.engr.dna.sim.Read;

import java.io.IOException;


public interface OutputWriter {
	/**
	 * Writes a set of related reads to the corresponding output files.
	 * For single reads, the array would have size 1, for pair end reads, 
	 * the array would have two elements.
	 * 
	 * @param reads - one Read for single reads, two reads for pair end reads 
	 */
	void writeReads(Read... reads) throws IOException;

	/**
	 * Closes the output files
	 */
	void close() throws IOException;
}
