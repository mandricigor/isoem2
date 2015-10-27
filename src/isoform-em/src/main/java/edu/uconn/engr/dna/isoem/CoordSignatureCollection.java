package edu.uconn.engr.dna.isoem;

import java.util.HashMap;
import java.util.Map;

public class CoordSignatureCollection implements SignatureCollection {
    private final Map<Object, Signature> signatures;

    public CoordSignatureCollection() {
	signatures = new HashMap<Object, Signature>();
    }

    @Override
    public Signature getSignature(Object e) {
	return signatures.get(e);
    }

    @Override
    public Signature putSignature(Coord coord) {
	signatures.put(coord.getObjectId(), coord);
	return coord;
    }

    @Override
    public Signature removeSignature(Coord coord) {
	signatures.remove(coord.getObjectId());
	return coord;
    }

}
