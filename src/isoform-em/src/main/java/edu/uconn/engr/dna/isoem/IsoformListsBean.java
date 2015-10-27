package edu.uconn.engr.dna.isoem;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jun 20, 2010
 * Time: 2:49:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class IsoformListsBean {
    private IsoformList[] isoformsForAlignments;
    private int[] fragmentLengths;
    private BitSet readStarts;

    public IsoformListsBean(IsoformList[] isoformsForAlignments, int[] fragmentLengths, BitSet readStarts) {
        this.isoformsForAlignments = isoformsForAlignments;
        this.fragmentLengths = fragmentLengths;
        this.readStarts = readStarts;
    }

    public IsoformList[] getIsoformsForAlignments() {
        return isoformsForAlignments;
    }

    public int[] getFragmentLengths() {
        return fragmentLengths;
    }

    public BitSet getReadStarts() {
        return readStarts;
    }
}
