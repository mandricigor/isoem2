package edu.uconn.engr.dna.isoem;


public interface ReadIdMapper<E, I> {
    void put(E externalId, int position, boolean firstReadInPair, I internalId,
             CharSequence cigar, CharSequence referenceSequenceName);

    ReadInfo<I> remove(E externalId, int position, boolean firstReadInPair);


    void clear();
}
