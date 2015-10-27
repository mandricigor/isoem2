package edu.uconn.engr.dna.isoem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A decorator class which first filters out any readclass that has more than
 * one isoform and then delegates to another EM algorithm to solve for the size
 * 1 read classes
 *
 * @author marius
 */
public class EmForUniq implements EMAlgorithm {
    private final EMAlgorithm delegate;

    public EmForUniq(EMAlgorithm delegate) {
        this.delegate = delegate;
    }

    @Override
    public Map<Object, Double> computeFrequencies(List<IsoformList> readClasses) {
        List<IsoformList> list = new ArrayList<IsoformList>();
        for (IsoformList rc : readClasses) {
            if (rc.size() == 1) {
                list.add(rc);
            }
        }
        return delegate.computeFrequencies(list);
    }

    @Override
    public int getSteps() {
        return delegate.getSteps();
    }

}
