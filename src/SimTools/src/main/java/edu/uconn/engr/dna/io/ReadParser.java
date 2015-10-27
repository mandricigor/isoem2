package edu.uconn.engr.dna.io;

import edu.uconn.engr.dna.main.Reads;




public interface ReadParser extends Parser<Reads> {
@Override
	Reads parse(String readsFileNames) throws Exception;

}
