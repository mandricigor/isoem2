package edu.uconn.engr.dna.isoem.alignment;

public class AlignmentId {
    protected final int id;
    private double qualityScoreWeight;
    private double biasCorrectedWeight;

    protected final int fragmentStart;

    public AlignmentId(int id, int fragmentStart,
                       boolean upstreamReadOnPositiveStrand,
                       double qualityScoreWeight, double biasCorrectedWeight) {
        this.id = id;
        this.qualityScoreWeight = qualityScoreWeight;
        this.biasCorrectedWeight = biasCorrectedWeight;
        this.fragmentStart = upstreamReadOnPositiveStrand ? fragmentStart
                : -fragmentStart;
    }

    public int getId() {
        return id;
    }

    public boolean isUpstreamReadOnPositiveStrand() {
        return fragmentStart > 0;
    }

    public int getFragmentStart() {
        return Math.abs(fragmentStart);
    }

    public double getQualityScoreWeight() {
        return qualityScoreWeight;
    }

    public double getBiasCorrectedWeight() {
        return biasCorrectedWeight;
    }
}
