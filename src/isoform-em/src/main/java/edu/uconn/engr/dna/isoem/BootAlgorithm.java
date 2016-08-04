package edu.uconn.engr.dna.isoem;

import java.util.List;
import java.util.Map;

public interface BootAlgorithm {
    /**
     * Runs the Bootstrap algorithm
     */
    List<List<IsoformList>> doBootstrapClusters(Integer bootIterationId);

}
