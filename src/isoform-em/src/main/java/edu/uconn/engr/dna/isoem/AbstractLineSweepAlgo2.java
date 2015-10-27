package edu.uconn.engr.dna.isoem;

import java.util.Iterator;
import java.util.List;

/**
 * Line sweep technique for isoform list construction
 *
 * @author marius
 */

public abstract class AbstractLineSweepAlgo2 {

    public abstract void handleRead(Object e, IsoList isoList, int readEnd);

    SignatureCollection isoformSignatures;
    CoordSignatureIsoCollection isoformsCollection;

    public void processSortedCoordinates(List<Coord> sortedReadCoordinates,
                                         List<Coord> sortedIsoformCoord) {
        if (sortedReadCoordinates.isEmpty() || sortedIsoformCoord.isEmpty())
            return;

        isoformSignatures = new CoordSignatureCollection();
        isoformsCollection = new CoordSignatureIsoCollection();

        Iterator<Coord> readIterator = sortedReadCoordinates.iterator();
        Iterator<Coord> isoIterator = sortedIsoformCoord.iterator();

        Coord readCoord = readIterator.next();
        Coord isoCoord = isoIterator.next();
        do {
            int rp = readCoord.getPos();
            int ip = isoCoord.getPos();
            if (rp < ip || (rp == ip && isoCoord.isIntervalEnd())) {
                processReadEnd(rp, readCoord);

                if (readIterator.hasNext())
                    readCoord = readIterator.next();
                else
                    return;

            } else {
                Signature sig = isoCoord;
                if (isoCoord.isIntervalStart()) {
                    isoformSignatures.putSignature(isoCoord);
                    isoformsCollection.handleIsoformGapEnd(sig);
                } else {
                    isoformSignatures.removeSignature(isoCoord);
                    isoformsCollection.handleIsoformGapStart(sig);
                }

                if (isoIterator.hasNext())
                    isoCoord = isoIterator.next();
                else
                    throw new IllegalStateException("This should never happen");

            }
        } while (true);

    }

    private void processReadEnd(int readEnd, Coord coord) {
        Signature sig = coord;
        sig = sig.getPreviousSignature();
        IsoList isoList;
        if (!sig.hasGaps()) {
            // read has no gaps:
            isoList = isoformsCollection
                    .getAllIsoformsWithMostRecentGapBefore(sig.getLastCoord());

        } else if (sig.hasExactlyOneGap()) {
            // read has exactly one gap:
            Gap lastGap = sig.getLastGap();
            isoList = isoformsCollection
                    .getAllIsoformsWithLastGapAndCoordBeforeGapSmallerOrEqualTo(
                            lastGap, sig.getCoordBeforeLastGap());
        } else {
            // read has more than one gap:
            isoList = isoformsCollection
                    .getAllIsoformsMatchingMultipleGapSignature(sig,
                            isoformSignatures);
        }
        Object e = coord.getObjectId();
        handleRead(e, isoList, readEnd);
    }

}