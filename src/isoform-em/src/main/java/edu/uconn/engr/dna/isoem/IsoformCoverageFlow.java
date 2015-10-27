package edu.uconn.engr.dna.isoem;

import edu.uconn.engr.dna.isoem.processor.CoordSorterParameterRunnable;
import edu.uconn.engr.dna.isoem.processor.CoordToIsoformCoverageParameterRunnable;
import edu.uconn.engr.dna.isoem.processor.CoordToIsoformListForAlignmentsParameterRunnable2;
import edu.uconn.engr.dna.probability.CumulativeProbabilityDistribution;
import edu.uconn.engr.dna.util.*;

import java.io.Reader;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jul 20, 2010
 * Time: 2:23:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class IsoformCoverageFlow {

	private IsoEMFlowTool t;
	private CumulativeProbabilityDistribution pd;
	private Map<String, Double> isoformFrequencies;

	public IsoformCoverageFlow(
					IsoEMFlowTool tool,
					CumulativeProbabilityDistribution pd,
					Map<String, Double> isoformFrequencies) {
		this.t = tool;
		this.pd = pd;
		this.isoformFrequencies = isoformFrequencies;
	}

	public Map<String, WeightedIntervals> computeCoverage(Reader inputFile) throws Exception {
		return Utils.reduce(
						t.parse(inputFile, t.getNThreads(), createFromSamToCoverageRunnableFactory(pd)),
						UniformBinaryOperator.<String, WeightedIntervals>mapMerger(true, new WeightedIntervalsMerger()));
	}

	private ParameterRunnableFactory<List<String>, Map<String, WeightedIntervals>> createFromSamToCoverageRunnableFactory(
					final CumulativeProbabilityDistribution pd) {
		return new ParameterRunnableFactory<List<String>, Map<String, WeightedIntervals>>() {

			@Override
			public ParameterRunnable<List<String>, Map<String, WeightedIntervals>> createParameterRunnable() {
				return (ParameterRunnable) t.createSamLinesToCoord2Runnable(
								createCoordToIsoformCoverageRunnable(t.createCoordToIsoformListRunnable2(pd, null)));
			}
		};
	}

	protected ParameterRunnable<ReadCoordinatesBean2, ?> createCoordToIsoformCoverageRunnable(
					ParameterRunnable<ReadCoordinatesBean2, IsoformList[]> coordToIsoformListRunnable) {
		return new CoordToIsoformCoverageParameterRunnable(isoformFrequencies, t.getIsoforms(),
						coordToIsoformListRunnable);
	}


	public int getNumberOfReads() {
		return CoordToIsoformCoverageParameterRunnable.nReads;
	}

	void scale(Map<String, WeightedIntervals> coverage, double factor) {
		for (WeightedIntervals wi : coverage.values()) {
			wi.scaleBy(factor);
		}
	}
}
