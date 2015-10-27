package edu.uconn.engr.dna.isoem;

import java.util.List;
import java.util.Map;

public interface EMAlgorithm {

    /**
     * Runs the EM algorithm in order to estimate the frequencies of the
     * isoforms. Returns a mapping from isoform ids to isoform frequencies. All
     * frequencies are numbers between 0 and 1, the sum of all the returned
     * frequencies is 1.
     *
     * @param readClasses
     * @return a Map having isoform ids as keys, and frequencies as values
     */
    Map<Object, Double> computeFrequencies(List<IsoformList> readClasses);

    int getSteps();

}
