package edu.uconn.engr.dna.format;

import edu.uconn.engr.dna.util.Intervals;

public class Isoform {

	private final String name;
	private final String chromosome;
	private final char strand;
	private final Intervals exons;

	public Isoform(String name, String chromosome, char strand,
					int[] exonStartsInts, int[] exonEndsInts) {
		this.name = name;
		this.chromosome = chromosome;
		this.strand = strand;
		this.exons = new Intervals(exonStartsInts, exonEndsInts);
	}

	public final int length() {
		return exons.length();
	}

	public String getName() {
		return name;
	}

	public String getChromosome() {
		return chromosome;
	}

	public char getStrand() {
		return strand;
	}

	public Intervals getExons() {
		return exons;
	}

	@Override
	public String toString() {
		return name;
	}

	public final void addExon(int start, int end) {
		this.exons.add(start, end);
//              System.err.println(name+" "+start+" "+end);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
						+ ((chromosome == null) ? 0 : chromosome.hashCode());
		result = prime * result + ((exons == null) ? 0 : exons.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + strand;
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
		Isoform other = (Isoform) obj;
		if (chromosome == null) {
			if (other.chromosome != null) {
				return false;
			}
		} else if (!chromosome.equals(other.chromosome)) {
			return false;
		}
		if (exons == null) {
			if (other.exons != null) {
				return false;
			}
		} else if (!exons.equals(other.exons)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (strand != other.strand) {
			return false;
		}
		return true;
	}
}
