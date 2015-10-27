package edu.uconn.engr.dna.isoem;

public interface ReadInfoMapper {
    void put(String referenceSequenceName, int position,
	    boolean firstReadInPair, String cigar);

    String remove(String referenceSequenceName, int position,
	    boolean firstReadInPair);

    void clear();
}
