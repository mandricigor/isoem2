package edu.uconn.engr.dna.sim;

import edu.uconn.engr.dna.format.Isoform;
import edu.uconn.engr.dna.util.Intervals;

public class Read {

	private Isoform isoform;
	private int readStart;
	private int readLength;
	private String clusterId;
	private String id;
	private char strand;

	public Read(String id,
			Object clusterId, 
			Isoform isoform, 
			int readStart,
			int readLength,
			char strand) {
		this.id = id;
		this.isoform = isoform;
		this.readStart = readStart;
		this.readLength = readLength;
		this.clusterId = String.valueOf(clusterId);
		this.strand = strand;
	}


	public String getIsoformId() {
		return isoform.getName();
	}
	
	public int getReadLength() {
		return readLength;
	}
	
	public int getReadStart() {
		return readStart;
	}

	public int getReadEnd() {
		return readStart + readLength - 1;
	}

	public String getChromosome() {
		return isoform.getChromosome();
	}

	public String getClusterId() {
		return clusterId;
	}

	public Intervals getIsoformExons() {
		return isoform.getExons();
	}

	public char getStrand() {
		return strand;
	}
	
	public String getId() {
		return id;
	}

}