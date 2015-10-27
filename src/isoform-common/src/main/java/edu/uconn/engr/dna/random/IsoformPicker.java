package edu.uconn.engr.dna.random;

import edu.uconn.engr.dna.format.Cluster;
import edu.uconn.engr.dna.format.Isoform;
import edu.uconn.engr.dna.format.Isoforms;
import edu.uconn.engr.dna.probability.ProbabilityDistribution;

public class IsoformPicker implements RandomPicker<Isoform, Cluster> {

	private final ProbabilityDistribution pd;
	private final Isoforms isoforms;

	public IsoformPicker(Isoforms isoforms, ProbabilityDistribution pd) {
		this.pd = pd;
		this.isoforms = isoforms;
	}

	@Override
	public Isoform pick(Cluster cluster) {
		int isoformNr = pd.generateInt(cluster.size());
		String isoformId = cluster.getKey(isoformNr);
		Isoform isoform = isoforms.getValue(isoformId);
		if (isoform == null) {
			System.out.println("WARNING: Isoform " + isoformId + " was not found");
		}
		return isoform;

	}

}
