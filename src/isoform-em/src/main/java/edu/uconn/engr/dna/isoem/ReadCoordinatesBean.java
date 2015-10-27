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
@Deprecated
public class ReadCoordinatesBean {
    private Map<String, List<Coord>> coordinates;
    private BitSet readStarts;
    private int nReads;
    private int nAlignments;

    public ReadCoordinatesBean(Map<String, List<Coord>> coordinates,
                               int nReads,
                               int nAlignments,
                               BitSet readStarts) {
        this.coordinates = coordinates;
        this.nReads = nReads;
        this.nAlignments = nAlignments;
        this.readStarts = readStarts;
    }

    public Map<String, List<Coord>> getCoordinates() {
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
