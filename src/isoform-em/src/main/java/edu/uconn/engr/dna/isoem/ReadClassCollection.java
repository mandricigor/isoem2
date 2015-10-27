package edu.uconn.engr.dna.isoem;

import java.util.Collection;

public interface ReadClassCollection {

    void addToClass(IsoformList multiKey, Object readId, int count);

    Collection<IsoformList> getClasses();

}
