package edu.uconn.engr.dna.isoem;

import edu.uconn.engr.dna.util.SingleBatchThreadPoolExecutor;
import edu.uconn.engr.dna.isoem.processor.*;
import edu.uconn.engr.dna.probability.CumulativeProbabilityDistribution;
import edu.uconn.engr.dna.probability.MapProbabilityDistribution;
import edu.uconn.engr.dna.util.*;

import org.apache.log4j.Logger;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jun 21, 2010
 * Time: 10:36:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class IsoEMFlowAutoProbabilityDistribution implements IsoEMFlow {
    private static final Logger log = Logger.getLogger(IsoEMFlowAutoProbabilityDistribution.class);
    private CumulativeProbabilityDistribution pd;
    private IsoEMFlowTool t;

    public IsoEMFlowAutoProbabilityDistribution(IsoEMFlowTool tool) {
        this.t = tool;
        this.pd = null;
    }

    public List<List<IsoformList>> computeClusters(Reader inputFile) throws Exception {
        Map<Integer, Integer> probMap = new HashMap<Integer, Integer>();
        List<IsoformListsBean> isoformsForAlignmentsList = new ArrayList<IsoformListsBean>();
        t.parse(inputFile, t.getNThreads(),
                createSamToProbabilityDistributionRunnableFactory(probMap, isoformsForAlignmentsList));
        System.out.println("pd size "+probMap.size());
        pd = new MapProbabilityDistribution(Utils.normalizeMap(probMap));
        log.debug("Found " + probMap.size() + " different fragment lengths ");
        log.debug("Mean " + pd.getMean() + " dev " + Math.sqrt(pd.getVariance()));
        System.out.println("Mean " + pd.getMean() + " dev " + Math.sqrt(pd.getVariance()));

        SingleBatchThreadPoolExecutor<List<IsoformList>, List<List<IsoformList>>> cluster
                = t.getClusterProcess();

        BatchThreadPoolExecutor.newInstance(t.getNThreads() - 1,
                createIsosPerAlignmentToClusterRunnableFactory(pd, cluster))
                .processAll(isoformsForAlignmentsList).waitForTermination();
        return cluster.waitForTermination();
    }


    @Override
    public List<Map<String, Double>> computeFpkms(List<List<IsoformList>> clusters, int nrBootIterations) throws Exception {
        log.debug("Reads after compact: "
                + t.countReads2(clusters) + " in "
                + t.countReadClasses(clusters) + " readClasses;"
                + " clusters " + clusters.size());
		System.out.println("Reads in the sam file: " + SamLinesToCoordParameterRunnable2.globalCount);
		System.out.println("Reads after processing quality scores: " + CoordToIsoformListForAlignmentsParameterRunnable2.totalNReads);
		System.out.println("Reads after computing compatibilities: " + AlignmentToReadConverter.totalReads);
		System.out.println("Reads that go into EM (possibly scaled if bias correction is enabled): " + t.countReads2(clusters));
        List<Map<String, Double>> result;
        Map<String, Double> adjustedWeights =  t.createAdjustedIsoLengths(pd);
        List<Map<String, Double>> map = t.runEM(clusters, adjustedWeights, nrBootIterations);
        if (!t.isReportCounts()) {
                result = new ArrayList<Map<String, Double>> ();
                for (Map<String, Double> bMap: map) {
                    Map<String, Double> bootResult = EmUtils.fpkm(bMap, adjustedWeights);
                    result.add(bootResult);
                }
        } else {
                result = map;
        }
        return result;
    }

    private ParameterRunnableFactory<List<String>, Object> createSamToProbabilityDistributionRunnableFactory(
            final Map<Integer, Integer> probMap,
            final List<IsoformListsBean> isoformsForAlignmentsList) {
        return new ParameterRunnableFactory<List<String>, Object>() {
            @Override
            public ParameterRunnable<List<String>, Object> createParameterRunnable() {
                return t.createSamLinesToCoord2Runnable(
                       t.createCoordToIsoformListRunnable2(null,
                       new CountFragmentsForUniqueReads(probMap, isoformsForAlignmentsList)));
            }
        };
    }


    public ParameterRunnableFactory<IsoformListsBean, Void> createIsosPerAlignmentToClusterRunnableFactory(
            final CumulativeProbabilityDistribution pd,
            final ParameterRunnable<List<IsoformList>, ?> forwardProcess) {
        return new ParameterRunnableFactory<IsoformListsBean, Void>() {
            @Override
            public ParameterRunnable<IsoformListsBean, Void> createParameterRunnable() {
                return new ChangeFragmentLengthToProbability(pd,
                       new AlignmentToReadConverter(
                       new NormalizeIsoformListsParameterRunnable(
                       forwardProcess)));
            }
        };
    }


}
