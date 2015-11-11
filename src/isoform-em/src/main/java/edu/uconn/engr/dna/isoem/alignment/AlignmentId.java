package edu.uconn.engr.dna.isoem.alignment;

public class AlignmentId {
    protected final int id;
    private double qualityScoreWeight;
    private double biasCorrectedWeight;
    protected String readName;

    protected final int fragmentStart;

    public AlignmentId(int id, String readName, int fragmentStart,
                       boolean upstreamReadOnPositiveStrand,
                       double qualityScoreWeight, double biasCorrectedWeight) {
        this.id = id;
        this.readName = readName;
        this.qualityScoreWeight = qualityScoreWeight;
        this.biasCorrectedWeight = biasCorrectedWeight;
        this.fragmentStart = upstreamReadOnPositiveStrand ? fragmentStart
                : -fragmentStart;
    }

    public String getReadName() {
        return readName;
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
