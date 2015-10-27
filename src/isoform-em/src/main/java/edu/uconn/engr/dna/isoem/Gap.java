package edu.uconn.engr.dna.isoem;

public class Gap {
	private int start;
	private int end;
	public static int count;
	
	public Gap(int start, int end) {
		this.start = start;
		this.end = end;
		++count;
	}

	public int getStart() {
		return start;
	}
	
	public int getEnd() {
		return end;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + end;
		result = prime * result + start;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Gap other = (Gap) obj;
		if (end != other.end)
			return false;
		if (start != other.start)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "[" + start + "," + end + "]"; 
	}
	
}
