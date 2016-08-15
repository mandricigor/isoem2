package edu.uconn.engr.dna.isoem;

import edu.uconn.engr.dna.isoem.processor.AlignmentToReadConverter;
import edu.uconn.engr.dna.isoem.processor.CoordToIsoformListForAlignmentsParameterRunnable2;
import edu.uconn.engr.dna.isoem.processor.NormalizeIsoformListsParameterRunnable;
import edu.uconn.engr.dna.isoem.processor.SamLinesToCoordParameterRunnable2;
import edu.uconn.engr.dna.probability.CumulativeProbabilityDistribution;
import edu.uconn.engr.dna.util.EmUtils;
import edu.uconn.engr.dna.util.ParameterRunnable;
import edu.uconn.engr.dna.util.ParameterRunnableFactory;
import edu.uconn.engr.dna.util.SingleBatchThreadPoolExecutor;
import org.apache.log4j.Logger;

import java.io.Reader;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jun 21, 2010
 * Time: 10:35:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class IsoEmFlowWithKnownProbabilityDistribution implements IsoEMFlow {
	private static final Logger log = Logger.getLogger(IsoEmFlowWithKnownProbabilityDistribution.class);
	private CumulativeProbabilityDistribution pd;
	private IsoEMFlowTool t;

	public IsoEmFlowWithKnownProbabilityDistribution(IsoEMFlowTool tool, CumulativeProbabilityDistribution pd) {
		this.t = tool;
		this.pd = pd;
	}

	public List<List<IsoformList>> computeClusters(Reader inputFile) throws Exception {

		//long start = System.currentTimeMillis();
		SingleBatchThreadPoolExecutor<List<IsoformList>, List<List<IsoformList>>> cluster = t.getClusterProcess();

		t.parse(inputFile, t.getNThreads() - 1, // leave one thread for clustering
						createFromSamToClustersRunnableFactory(pd, cluster));
		return cluster.waitForTermination();
        }



        public List<Map<String, Double>> computeFpkms(List<List<IsoformList>> clusters, int nrBootIterations) throws Exception {
		//log.debug("Parser time " + (System.currentTimeMillis() - start));
		log.debug("Reads after compacting: "
						+ t.countReads2(clusters) + " in "
						+ t.countReadClasses(clusters) + " readClasses;"
						+ " clusters " + clusters.size());

		System.out.println("Reads in the sam file: " + SamLinesToCoordParameterRunnable2.globalCount);
		System.out.println("Reads after processing quality scores: " + CoordToIsoformListForAlignmentsParameterRunnable2.totalNReads);
		System.out.println("Reads after computing compatibilities: " + AlignmentToReadConverter.totalReads);
		System.out.println("Reads that go into EM (possibly scaled if bias correction is enabled): " + t.countReads2(clusters));
		log.debug("Uniq Reads " + t.countUniqReads(clusters));

		long start2 = System.currentTimeMillis();
		Map<String, Double> adjustedWeights =  t.createAdjustedIsoLengths(pd);
		List<Map<String, Double>> map = t.runEM(clusters, adjustedWeights, nrBootIterations);
		List<Map<String, Double>> result;
		if (!t.isReportCounts()) {
                    result = new ArrayList<Map<String, Double>> ();
                    for (Map<String, Double> bMap: map) {
                        Map<String, Double> bootResult = EmUtils.fpkm(bMap, adjustedWeights);
                        result.add(bootResult);
                    }
		} else {
			result = map;
		}
		log.debug("EM time " + (System.currentTimeMillis() - start2));
		return result;
	}

	private ParameterRunnableFactory<List<String>, Object> createFromSamToClustersRunnableFactory(
					final CumulativeProbabilityDistribution pd,
					final SingleBatchThreadPoolExecutor<List<IsoformList>, List<List<IsoformList>>> cluster) {
		return new ParameterRunnableFactory<List<String>, Object>() {


			@Override
			public ParameterRunnable<List<String>, Object> createParameterRunnable() {
				ParameterRunnable<List<String>, Object> r = t.createSamLinesToCoord2Runnable(
								t.createCoordToIsoformListRunnable2(pd,
								new AlignmentToReadConverter(
								new NormalizeIsoformListsParameterRunnable(
								cluster))));
				return r;
//				SamLinesToCoordParameterRunnable r = t.createSamLinesToCoordRunnable(
//								new CoordSorterParameterRunnable(
//								t.createCoordToIsoformListRunnable(pd,
//								new AlignmentToReadConverter(
//								new NormalizeIsoformListsParameterRunnable(
//								cluster)))));
//				return r;

			}
		};
	}
}

