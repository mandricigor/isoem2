package edu.uconn.engr.dna.isoem;

import edu.uconn.engr.dna.util.DefaultSortedMultiMap;
import edu.uconn.engr.dna.util.SortedMultiMap;

import java.util.*;

public class CoordSignatureIsoCollection {
    private final LinkedHashMap<Gap, SortedMultiMap<Integer, Object>> currentIsoformsForGap;

    public CoordSignatureIsoCollection() {
        this.currentIsoformsForGap = new LinkedHashMap<Gap, SortedMultiMap<Integer, Object>>();
    }

    public IsoList getAllIsoformsWithMostRecentGapBefore(Integer readStart) {
        List<Object> isoforms = null;
        for (Map.Entry<Gap, SortedMultiMap<Integer, Object>> entry : currentIsoformsForGap
                .entrySet()) {
            Gap mostRecentGap = entry.getKey();
            if (mostRecentGap.getEnd() <= readStart) {
                if (isoforms == null)
                    isoforms = new ArrayList<Object>();
                isoforms.addAll(entry.getValue().values());
            } else
                // it's safe to break here because the gaps were added in
                // increasing order of their ends and we keep them in a
                // linked hash map, so all the gaps after this point have
                // greater coordinates
                break;
        }
        if (isoforms == null)
            return null;
        return new IsoList(isoforms, 1.0);
    }

    public IsoList getAllIsoformsMatchingMultipleGapSignature(
            Signature sig, SignatureCollection sigCollection) {
        // look at all the isoforms having the most recent gap
        // equal to the last gap of the signature, and the coordinate
        // before the gap exactly equal to the coordinate before
        // the gap in the signature
        Gap lastGap = sig.getLastGap();
        Integer leftContigBeforeGap = sig.getCoordBeforeLastGap();
        SortedMultiMap<Integer, Object> candidateIsoforms = currentIsoformsForGap
                .get(lastGap);
        if (candidateIsoforms == null)
            return null;

        Collection<Object> list = candidateIsoforms.get(leftContigBeforeGap);
        if (list == null)
            return null;

        return filter(list, sig, sigCollection);
    }

    public IsoList getAllIsoformsWithLastGapAndCoordBeforeGapSmallerOrEqualTo(
            Gap lastGap, Integer leftContigBeforeGap) {
        // look at all the isoforms having the most recent gap
        // equal to the last gap of the read, and the coordinate
        // before the gap smaller or equal to the coordinate before
        // the gap in the read
        SortedMultiMap<Integer, Object> candidateIsoforms = currentIsoformsForGap
                .get(lastGap);
        if (candidateIsoforms == null)
            return null;

        Collection<Object> objs = candidateIsoforms
                .getAllValuesForKeysSmallerOrEqualTo(leftContigBeforeGap);
        if (objs == null)
            return null;

        return new IsoList(objs, 1.0);
    }

    public void handleIsoformGapStart(Signature sig) {
        sig = sig.getPreviousSignature();
        Gap gap = sig.getLastGap();
        SortedMultiMap<Integer, Object> candidateIsoforms = currentIsoformsForGap
                .get(gap);
        if (candidateIsoforms == null)
            return;

        Integer leftContigBeforeGap = sig.getCoordBeforeLastGap();
        candidateIsoforms.remove(leftContigBeforeGap, sig.getObjectId());
        if (candidateIsoforms.isEmpty())
            currentIsoformsForGap.remove(gap);

    }

    public void handleIsoformGapEnd(Signature sig) {
        Gap lastGap = sig.getLastGap();
        SortedMultiMap<Integer, Object> candidateIsoforms = currentIsoformsForGap
                .get(lastGap);
        if (candidateIsoforms == null) {
            candidateIsoforms = createSortedMultiMap();
            currentIsoformsForGap.put(lastGap, candidateIsoforms);
        }
        Integer leftContigBeforeGap = sig.getCoordBeforeLastGap();
        candidateIsoforms.put(leftContigBeforeGap, sig.getObjectId());
    }

    public void handleIsoformStart(Signature sig) {
        handleIsoformGapEnd(sig);
    }

    public void handleIsoformEnd(Signature sig) {
        handleIsoformGapStart(sig);
    }

    /**
     * Returns only those isoforms matching the given signature;
     */
    private IsoList filter(Collection<Object> candidates,
                           Signature readSig, SignatureCollection sigCollection) {
        List<Object> isoforms = null;
        for (Object obj : candidates) {
            Signature sig = sigCollection.getSignature(obj);
            if (matches(sig, readSig)) {
                if (isoforms == null)
                    isoforms = new ArrayList<Object>();

                isoforms.add(sig.getObjectId());
            }
        }
        return new IsoList(isoforms, 1.0);
    }

    protected boolean matches(Signature isoSig, Signature readSig) {
        // assume last gap and coordinate before it already matched
        isoSig = skip(isoSig, 3);
        if (isoSig == null)
            // isoform to short
            return false;

        readSig = skip(readSig, 3);
        do {
            if (readSig.getPreviousSignature() == null)
                // read start (must fall after corresponding isoform start)
                return readSig.getLastCoord() >= isoSig.getLastCoord();

            if (isoSig.getLastCoord() != readSig.getLastCoord())
                return false;

            readSig = readSig.getPreviousSignature();
            isoSig = isoSig.getPreviousSignature();
        } while (isoSig != null && readSig != null);
        return false;
    }

    private Signature skip(Signature sig, int k) {
        while (k-- > 0 && sig != null)
            sig = sig.getPreviousSignature();

        return sig;
    }

    private SortedMultiMap<Integer, Object> createSortedMultiMap() {
        return new DefaultSortedMultiMap<Integer, Object>();
    }

}