package edu.uconn.engr.dna.format;

import java.util.Collection;

/**
 * Generic interface for retrieving subsequences of other sequences
 * which are identified by tags (/names/id's). For example, one 
 * implementation can be used for accessing substrings of chromosomes 
 * (the tag is the name of the chromosome), other implementation for 
 * accessing substrings of isoforms (tag name is the name of the isoform)
 * 
 * @author marius
 *
 */
public interface TaggedSequences {

	/**
	 * Returns the subsequence found in the interval [start, end]
	 * of the sequence identified by the given tag
	 * 
	 * @param tag the name of the sequence from which to read a sequence
	 * @param start the starting position in sequence coordinates (1 based)
	 * @param end the end position in sequence coordinates
	 * @return a String representing the subsequence found in the specified
	 * interval of the specified sequence
	 */
	CharSequence getSequence(CharSequence tag, int start, int end);
	
	/**
	 * Returns the full sequence associated with the given tag
	 * 
	 * @param tag name of the sequence
	 * @return the full sequence associated with this name
	 */
	CharSequence getSequence(CharSequence tag);

	/**
	 * Returns a collection of all the tags which have an associated
	 * sequence
	 * 
	 * @return a Collection of Strings
	 */
	Collection<CharSequence> getAllTags();

	CharSequence remove(CharSequence tag);

	CharSequence put(CharSequence tag, CharSequence seq);
}
