package edu.uconn.engr.dna.format;

import edu.uconn.engr.dna.util.AbstractRandomAccessMap;



public class Cluster extends AbstractRandomAccessMap<String, String, String>{

	private String name;

	public Cluster(String name) {
		this.name = name;
	}
	
	@Override
	protected String addToExistingGroupOrCreateNew(String key, String cluster,
			String value) {
		return value;
	}

	public String getName() {
		return name;
	}

	public void addIsoform(String value) {
		// trivial mapping: name to name
		this.add(value, value);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
