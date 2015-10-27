package edu.uconn.engr.dna.isoem;

public interface Signature {

    public int getLastCoord();

    public Gap getLastGap();

    public int getCoordBeforeLastGap();

    public boolean hasGaps();

    public boolean hasExactlyOneGap();

    public Object getObjectId();

    public Signature getPreviousSignature();
}