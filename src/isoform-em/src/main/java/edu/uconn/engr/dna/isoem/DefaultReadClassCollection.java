package edu.uconn.engr.dna.isoem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DefaultReadClassCollection implements ReadClassCollection {

    private final Map<IsoformList, IsoformList> classes;

    public DefaultReadClassCollection() {
        classes = new HashMap<IsoformList, IsoformList>();
    }

    @Override
    public void addToClass(IsoformList multiKey, Object readId, int count) {
        if (multiKey.size() == 0)
            throw new IllegalStateException("ups");

        IsoformList old = classes.get(multiKey);
        if (old == null)
            classes.put(multiKey, multiKey);
        else
            old.setMultiplicity(old.getMultiplicity() + multiKey.getMultiplicity());
    }

    @Override
    public Collection<IsoformList> getClasses() {
        return classes.values();
    }
}

