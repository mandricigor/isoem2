package edu.uconn.engr.dna.isoem.processor;

import edu.uconn.engr.dna.isoem.BootAlgorithm;
import edu.uconn.engr.dna.isoem.IgorBootAlgorithm;
import edu.uconn.engr.dna.isoem.IsoformList;
import edu.uconn.engr.dna.util.ParameterRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;


/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jun 20, 2010
 * Time: 5:56:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class BootParameterRunnable implements ParameterRunnable<Integer, List<List<IsoformList>>> {

    private BootAlgorithm bootAlgorithm;
    private List<List<IsoformList>> results;

    public BootParameterRunnable(List<List<IsoformList>> clusters, Integer bootCount, Map<String, Integer> m, List<String> bootArray) {
        results = new ArrayList<List<IsoformList>>();
        this.bootAlgorithm = new IgorBootAlgorithm(clusters, bootCount, m, bootArray);
    }

    @Override
    public void run(Integer bootIterationId) {
        List<List<IsoformList>> bootSample = bootAlgorithm.doBootstrapClusters(bootIterationId);
        results.addAll(bootSample);
    }

    @Override
    public List<List<IsoformList>> done() {
        return results;
    }
    
}
