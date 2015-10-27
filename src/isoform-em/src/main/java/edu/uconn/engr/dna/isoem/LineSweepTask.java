package edu.uconn.engr.dna.isoem;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import edu.uconn.engr.dna.sort.Sorter;
import edu.uconn.engr.dna.util.TimingTool;

public class LineSweepTask implements Callable<EMTaskResult> {

    private final List<Coord> coords;
    private final Sorter<Coord> sorter;
    private final TimingTool timer;
    private final ReadClassPartitionAlgo rcpAlgo;
    private final List<Coord> sortedIsoCoords;

    public LineSweepTask(List<Coord> coords, List<Coord> sortedIsoCoords,
	    Sorter<Coord> sorter, ReadClassPartitionAlgo rcpAlgo) {
	this.coords = coords;
	this.sortedIsoCoords = sortedIsoCoords;
	this.sorter = sorter;
	this.rcpAlgo = rcpAlgo;
	this.timer = new TimingTool();
    }

    @Override
    public EMTaskResult call() throws Exception {
	EMTaskResult result = new EMTaskResult();
	List<Coord> sortedCoords = sort(coords, result);
	Map<Integer, IsoformList> isosForRead = findReadClasses(sortedCoords,
		result);
	result.setIsosForRead(isosForRead);
	// runEm(readClasses, result);
	return result;
    }

    private List<Coord> sort(List<Coord> coords2, EMTaskResult result) {
	timer.start(null);
	List<Coord> sortedCoords = sorter.sort(coords2);
	result.setCoords(sortedCoords.size());
	result.setSortTime(timer.stop());
	// checkOrder(sortedCoords);
	return sortedCoords;
    }

    private Map<Integer, IsoformList> findReadClasses(List<Coord> sortedCoords,
	    EMTaskResult result) {
	timer.start(null);
	Map<Integer, IsoformList> isosForRead = rcpAlgo.findIsoformsForReads(
		sortedCoords, sortedIsoCoords);
	result.setFragmentLengthCounts(rcpAlgo.getFragmentLengthCounts());
	result.setReadClassPartitionTime(timer.stop());
	return isosForRead;
    }
    //
    // private void checkOrder(List<Coord> coords) {
    // int n = coords.size();
    // if (n == 0) {
    // return;
    // }
    // Coord prev = coords.get(0);
    // for (int i = 1; i < n; ++i) {
    // Coord current = coords.get(i);
    // if (current.getPos() < prev.getPos()) {
    // throw new IllegalStateException("Coordinates are not sorted "
    // + prev.getObjectId() + " " + current.getObjectId()
    // + prev.getPos() + " " + current.getPos());
    // }
    // Signature prevSig = current.getPreviousSignature();
    // if (prevSig != null) {
    // if (!current.getObjectId().equals(prevSig.getObjectId())) {
    // throw new
    // IllegalStateException("Different ID Coordinates linked together "
    // + prevSig.getObjectId() + " " + current.getObjectId()
    // + prevSig.getObjectId() + " " + current.getObjectId());
    // }
    // if (current.getPos() < prevSig.getLastCoord()) {
    // throw new
    // IllegalStateException("Linked coordinates not in correct order "
    // + prevSig.getObjectId() + " " + current.getObjectId()
    // + prevSig.getLastCoord() + " " + current.getPos());
    // }
    // }
    // }
    // }

}
