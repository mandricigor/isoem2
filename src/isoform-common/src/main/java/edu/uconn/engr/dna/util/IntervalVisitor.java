package edu.uconn.engr.dna.util;

/**
 * An interface to be used when converting a contiguous isoform interval 
 * to a set of genome intervals. The visit method will be called for each
 * of the intervals and will receive both the isoform coordinates of the 
 * current interval and the corresponding genome coordinates. Note that the
 * isoform intervals will be adjacent.
 *  
 * @author Marius
 *
 */
public interface IntervalVisitor {

	/**
	 * @param isoformIntervalStart start position of the current interval in the isoform
	 * @param isoformIntervalEnd end position of the current interval in the isoform
	 * @param genomeIntervalStart start position of the current interval in the genome
	 * @param genomeIntervalEnd end position of the current interval in the genome
	 */
	public void visit(int isoformIntervalStart, int isoformIntervalEnd,
			int genomeIntervalStart, int genomeIntervalEnd);
}
