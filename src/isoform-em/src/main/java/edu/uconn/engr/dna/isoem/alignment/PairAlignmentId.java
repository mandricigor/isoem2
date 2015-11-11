package edu.uconn.engr.dna.isoem.alignment;

public class PairAlignmentId extends AlignmentId {

    public PairAlignmentId(int id,
                           String readName,
                           boolean upstreamReadOnPositiveStrand,
                           int fragmentStart,
                           boolean firstReadOnPositiveStrand,
                           double weight, double biasCorrectedWeight) {
        super(firstReadOnPositiveStrand ? id : -id, readName,
                fragmentStart, upstreamReadOnPositiveStrand,
                weight, biasCorrectedWeight);
    }

    public char getFirstReadStrand() {
        return (id < 0) ? '-' : '+';
    }

    @Override
    public int getId() {
        return Math.abs(id);
    }

}
