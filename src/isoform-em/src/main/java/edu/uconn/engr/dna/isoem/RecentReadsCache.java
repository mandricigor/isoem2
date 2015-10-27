package edu.uconn.engr.dna.isoem;


public interface RecentReadsCache {
    boolean add(CharSequence readName,
                CharSequence referenceSequenceName, int alignmentStart, int matePosition,
                CharSequence cigar, CharSequence mateCigar);

    void clear();
}
