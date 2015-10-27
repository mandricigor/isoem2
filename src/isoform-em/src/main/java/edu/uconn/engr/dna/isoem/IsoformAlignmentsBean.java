package edu.uconn.engr.dna.isoem;

import java.util.BitSet;

/**
 * User: marius
 * Date: Jun 21, 2010
 * Time: 8:54:41 PM
 */
public class IsoformAlignmentsBean {
    private IsoformList[] isoformsForAlignments;
    private BitSet readStartMarkers;

    public IsoformAlignmentsBean(IsoformList[] isoformsForAlignments, BitSet readStartMarkers) {
        this.isoformsForAlignments = isoformsForAlignments;
        this.readStartMarkers = readStartMarkers;
    }

    public IsoformList[] getIsoformsForAlignments() {
        return isoformsForAlignments;
    }

    public void setIsoformsForAlignments(IsoformList[] isoformsForAlignments) {
        this.isoformsForAlignments = isoformsForAlignments;
    }

    public BitSet getReadStartMarkers() {
        return readStartMarkers;
    }

    public void setReadStartMarkers(BitSet readStartMarkers) {
        this.readStartMarkers = readStartMarkers;
    }
}
