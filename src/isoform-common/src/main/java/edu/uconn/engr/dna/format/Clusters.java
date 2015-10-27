package edu.uconn.engr.dna.format;

import edu.uconn.engr.dna.util.AbstractRandomAccessMap;


public class Clusters extends AbstractRandomAccessMap<String, String, Cluster> {

	@Override
	protected Cluster addToExistingGroupOrCreateNew(String key, Cluster cluster, String value) {
		if (cluster == null) {
			cluster = new Cluster(key);
		}
		cluster.addIsoform(value);
		return cluster;
	}

}
