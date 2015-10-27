package edu.uconn.engr.dna.util;

import java.util.Arrays;
import java.util.Iterator;

public class Intervals implements Iterable<Integer> {

	private int[] starts;
	private int[] ends;
	private int[] partialLengthSums;
	private int length;

	public Intervals(int[] starts, int[] ends) {
		this.starts = starts;
		this.ends = ends;
		this.length = starts == null ? 0 : starts.length;
		if (starts != null) {
			this.partialLengthSums = computePartialSums();
		}
	}

	public Intervals() {
		this.starts = null;
		this.ends = null;
		this.length = 0;
	}

	public int convertIsoToGenomeCoord(int isoCoord1Based) {
		if (isoCoord1Based < 1) {
			throw new IllegalArgumentException(
							"Invalid isoform coordinate (must be at least 1): "
							+ isoCoord1Based);
		}
		int totalLen = partialLengthSums[length - 1];
		if (isoCoord1Based > totalLen) {
			throw new IllegalArgumentException(
							"Invalid isoform coordinate (must be at most " + totalLen
							+ "): " + isoCoord1Based);
		}
		int i = Arrays.binarySearch(partialLengthSums, 0, length,
						isoCoord1Based);
		if (i < 0) {
			i = -i - 1;
		}

		return (int) ends[i] - (partialLengthSums[i] - isoCoord1Based);
	}

	public int convertGenomeToIsoCoord(int genomeCoord1Based) {
		int i = findIntervalForGenomeCoord(genomeCoord1Based, 0);
		if (i == -1) {
			return -1;
		}
		return pos(i, genomeCoord1Based);
	}

	public int getDistanceInIsoformBetween(int leftGenomeCoord1Based,
					int rightGenomeCoord1Based) {
		if (leftGenomeCoord1Based > rightGenomeCoord1Based) {
			throw new IllegalArgumentException(
							"Left coordinate must be smaller than right coordinate. Got "
							+ leftGenomeCoord1Based + " and "
							+ rightGenomeCoord1Based);
		}

		int i = findIntervalForGenomeCoord(leftGenomeCoord1Based, 0);
		if (i == -1) {
//sahar Oct 2015 error with paired end hisat alignments
//			throw new IllegalArgumentException("Coordinate "
//							+ leftGenomeCoord1Based + " not inside the intervals "
//							+ this);
//			System.err.println("Coordinate "
//							+ leftGenomeCoord1Based + " not inside the intervals "
//							+ this);
			return -1;

		}

		int j = findIntervalForGenomeCoord(rightGenomeCoord1Based, i);
		if (j == -1) {
//sahar Oct 2015 error with paired end hisat alignments
//			throw new IllegalArgumentException("Coordinate "
//							+ rightGenomeCoord1Based + " not inside the intervals "
//							+ this);
//			System.err.println("Coordinate "
//							+ leftGenomeCoord1Based + " not inside the intervals "
//							+ this);
			return -1;
		}

		return pos(j, rightGenomeCoord1Based) - pos(i, leftGenomeCoord1Based)
						+ 1;
	}

	private int pos(int interval, int genomeCoord1Based) {
		return partialLengthSums[interval]
						- ((int) ends[interval] - genomeCoord1Based);
	}

	private int findIntervalForGenomeCoord(int genomeCoord1Based,
					int startInterval) {
		int i = Arrays.binarySearch(ends, startInterval, length,
						genomeCoord1Based);
		if (i >= 0) // coord is right at the end of an interval
		{
			return i;
		}

		i = -i - 1;
		if (i >= length) // position off the right end
		{
			return -1;
		}

		if (genomeCoord1Based < starts[i]) // position falls within intron
		{
			return -1;
		}

		return i;
	}

	/**
	 * Returns the number of intervals
	 *
	 * @return
	 */
	public int size() {
		return length;
	}

	/**
	 * Returns the start of the ith interval (0 based count)
	 *
	 * @param i
	 * @return
	 */
	public int getStart(int i) {
		if (i >= length) {
			throw new ArrayIndexOutOfBoundsException("Index " + i + " length " + length);
		}
		return starts[i];
	}

	/**
	 * Returns the start of the leftmost interval
	 *
	 * @return
	 */
	public int getStart() {
		return starts[0];
	}

	/**
	 * Returns the end of the rightmost interval
	 *
	 * @return
	 */
	public int getEnd() {
		return ends[length - 1];
	}

	/**
	 * Returns the end of the ith interval
	 *
	 * @param i
	 * @return
	 */
	public int getEnd(int i) {
		if (i >= length) {
			throw new ArrayIndexOutOfBoundsException("Index " + i + " length " + length);
		}
		return ends[i];
	}

	public void add(int start, int end) {
		if (starts == null) {
			starts = new int[10];
			ends = new int[10];
			partialLengthSums = new int[10];
		} else if (length >= starts.length) {
			starts = Arrays.copyOf(starts, starts.length * 2);
			ends = Arrays.copyOf(ends, ends.length * 2);
			partialLengthSums = Arrays.copyOf(partialLengthSums,
							partialLengthSums.length * 2);
		}
		int i;
		for (i = length; i > 0 && start < starts[i - 1]; --i) {
			starts[i] = starts[i - 1];
			ends[i] = ends[i - 1];
		}
		starts[i] = start;
		ends[i] = end;
		++length;
		adjustPartialLengths(i, partialLengthSums, length);
//              System.err.println("inside intervals.add");
//		for (i = 0; i < length; i++)
//	            System.err.println(i+" " +starts[i]+" " +ends[i]);

	}

	private void adjustPartialLengths(int i, int[] p, int length) {
		if (i == 0) {
			p[0] = (int) length(0);
			++i;
		}
		for (; i < length; ++i) {
			p[i] = p[i - 1] + (int) length(i);
		}
	}

	private int[] computePartialSums() {
		if (length == 0) {
			return new int[0];
		}
		int s[] = new int[length];
		adjustPartialLengths(0, s, length);
		return s;
	}

	/**
	 * Appends a set of intervals to the end of this set of intervals
	 */
	public void append(Intervals intervals) {
		int n = intervals.size();
		for (int i = 0; i < n; ++i) {
			add(intervals.getStart(i), intervals.getEnd(i));
		}
	}

	/**
	 * Returns the length of the ith interval
	 *
	 * @param i
	 * @return
	 */
	public int length(int i) {
		if (i >= length) {
			throw new ArrayIndexOutOfBoundsException("Index " + i + " length " + length);
		}
		return ends[i] - starts[i] + 1;
	}

	/**
	 * Returns the size of the gap between intervals i and i-1
	 *
	 * @param i
	 * @return
	 */
	public int getGapSize(int i) {
		return getStart(i) - getEnd(i - 1) - 1;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size(); ++i) {
			sb.append('[');
			sb.append(getStart(i));
			sb.append(',');
			sb.append(getEnd(i));
			sb.append("] ");
		}
		return sb.toString();
	}

	public int[] getStarts() {
		return starts;
	}

	public int[] getEnds() {
		return ends;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(ends);
		result = prime * result + length;
		result = prime * result + Arrays.hashCode(starts);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Intervals other = (Intervals) obj;
		if (!Utils.equals(ends, other.ends, length)) {
			return false;
		}
		if (length != other.length) {
			return false;
		}
		if (!Utils.equals(starts, other.starts, length)) {
			return false;
		}
		return true;
	}

	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {

			private int exon = 0;
			private boolean start = true;

			@Override
			public boolean hasNext() {
				return exon < size();
			}

			@Override
			public Integer next() {
				if (start) {
					start = false;
					return (int) getStart(exon);
				} else {
					start = true;
					return (int) getEnd(exon++);
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
	}

	public int getNCoords() {
		return length << 1;
	}

	public int getCoord(int i) {
		if ((i & 1) == 0) {
			return (int) getStart(i >> 1);
		} else {
			return (int) getEnd(i >> 1);
		}
	}

	/**
	 * Returns the total length of all exons 
	 */
	public final int length() {
		return partialLengthSums[length - 1];
	}
}
