package edu.uconn.engr.dna.isoem;

public interface IsoformsCollection {

    void handleIsoformStart(Signature sig);

    void handleIsoformEnd(Signature sig);

    public void handleIsoformGapStart(Signature sig);

    public void handleIsoformGapEnd(Signature sig);

    IsoformList getAllIsoformsWithMostRecentGapBefore(Integer pos);

    IsoformList getAllIsoformsMatchingMultipleGapSignature(Signature sig,
	    SignatureCollection isoformSignatures);

    IsoformList getAllIsoformsWithLastGapAndCoordBeforeGapSmallerOrEqualTo(
	    Gap lastGap, Integer leftContigBeforeGap);
}
