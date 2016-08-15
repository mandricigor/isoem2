package edu.uconn.engr.dna.isoem.processor;

import edu.uconn.engr.dna.isoem.EMAlgorithm;
import edu.uconn.engr.dna.isoem.EmForUniq;
import edu.uconn.engr.dna.isoem.FastWeightedEmAlgorithm;
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
public class EmParameterRunnable implements ParameterRunnable<List<IsoformList>, List<Map<Object, Double>>> {
    private static final int maxEmSteps = 400;
    private EMAlgorithm emAlgorithm;
    private List<Map<Object, Double>> map;

    public EmParameterRunnable(Map<Object, Double> adjustedIsoLengths, Boolean runUniq, Boolean reportCounts, Integer nrBootIterations) {
        this.emAlgorithm = new FastWeightedEmAlgorithm(adjustedIsoLengths, maxEmSteps,
                123, reportCounts);
        if (runUniq) {
            this.emAlgorithm = new EmForUniq(this.emAlgorithm);
        }
        this.map = new ArrayList<Map<Object, Double>>();
        for (int i = 0; i <= nrBootIterations; i ++) {
            this.map.add(new HashMap<Object, Double>());
        }
    }

    @Override
    public void run(List<IsoformList> readClasses) {
//        System.out.println("in EmParameterRunnable readClasses length =");
        Map<Object, Double> m = emAlgorithm.computeFrequencies(readClasses);
        int bootstrapId = m.get("bootstrapId").intValue();
        m.remove("bootstrapId");
        map.get(bootstrapId).putAll(m);
    }

    @Override
    public List<Map<Object, Double>> done() {
        return map;
    }
    
}
