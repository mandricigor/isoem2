package edu.uconn.engr.dna.io;

import java.io.InputStream;

import edu.uconn.engr.dna.format.Clusters;
import edu.uconn.engr.dna.format.Isoforms;
import edu.uconn.engr.dna.util.Pair;

public interface GenesAndIsoformsParser extends Parser<Pair<Clusters, Isoforms>> {

	public Pair<Clusters, Isoforms> parse(InputStream in) throws Exception;

}
