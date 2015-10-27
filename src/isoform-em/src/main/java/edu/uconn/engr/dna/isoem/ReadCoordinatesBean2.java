package edu.uconn.engr.dna.isoem;

import java.util.BitSet;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jun 20, 2010
 * Time: 2:49:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReadCoordinatesBean2 {
    private Map<String, Coord2[]> coordinates;
    private BitSet readStarts;
    private int nReads;
    private int nAlignments;

    public ReadCoordinatesBean2(Map<String, Coord2[]> coordinates,
                               int nReads,
                               int nAlignments,
                               BitSet readStarts) {
        this.coordinates = coordinates;
        this.nReads = nReads;
        this.nAlignments = nAlignments;
        this.readStarts = readStarts;
    }

    public Map<String, Coord2[]> getCoordinates() {
        return coordinates;
    }

    public int getnReads() {
        return nReads;
    }

    public int getnAlignments() {
        return nAlignments;
    }

    public BitSet getReadStarts() {
        return readStarts;
    }
}
