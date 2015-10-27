package edu.uconn.engr.dna.random;

import edu.uconn.engr.dna.format.Cluster;
import edu.uconn.engr.dna.format.Clusters;
import edu.uconn.engr.dna.probability.ProbabilityDistribution;

public class ClusterPicker implements RandomPicker<Cluster, Clusters> {

	private ProbabilityDistribution pd;

	public ClusterPicker(Clusters clusters, 
			ProbabilityDistribution pd) {
		this.pd = pd;
	}
	
	@Override
	public Cluster pick(Clusters clusters) {
		int clusterNr = pd.generateInt(clusters.size());
		return clusters.getValue(clusters.getKey(clusterNr));
	}

}
