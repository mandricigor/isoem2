package edu.uconn.engr.dna.isoem;

public interface SignatureCollection {

    Signature putSignature(Coord coord);

    Signature removeSignature(Coord coord);

    Signature getSignature(Object e);

}
