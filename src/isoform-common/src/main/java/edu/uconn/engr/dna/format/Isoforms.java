package edu.uconn.engr.dna.format;

import edu.uconn.engr.dna.util.AbstractRandomAccessMap;
import edu.uconn.engr.dna.util.Intervals;

public class Isoforms extends AbstractRandomAccessMap<String, Isoform, Isoform> {

	public Isoforms() {
	}

	/**
	 * Copy constructor
	 * @param isoforms
	 */
	public Isoforms(Isoforms isoforms) {
		for (Isoform iso : isoforms.groupIterator()) {
			String isoformId = iso.getName();
			Isoform copy = new Isoform(isoformId, iso.getChromosome(),
					iso.getStrand(), null, null);
			add(isoformId, copy);
			Intervals exons = iso.getExons();
			for (int i = 0; i < exons.size(); ++i) {
				copy.addExon(exons.getStart(i), exons.getEnd(i));
			}
		}
	}

	@Override
	protected Isoform addToExistingGroupOrCreateNew(String key, Isoform isoform, Isoform value) {
		return value;
	}

	public Iterable<Isoform> isoformIterator() {
		return groupIterator();
	}
}
