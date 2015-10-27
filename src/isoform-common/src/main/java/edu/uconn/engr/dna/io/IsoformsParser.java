package edu.uconn.engr.dna.io;

import java.io.InputStream;

import edu.uconn.engr.dna.format.Isoforms;


public interface IsoformsParser extends Parser<Isoforms> {

	Isoforms parse(InputStream transcriptInputStream) throws Exception;

}
