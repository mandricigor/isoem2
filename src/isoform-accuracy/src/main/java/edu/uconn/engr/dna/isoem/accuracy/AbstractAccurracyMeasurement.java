package edu.uconn.engr.dna.isoem.accuracy;

import edu.uconn.engr.dna.format.Cluster;
import edu.uconn.engr.dna.format.Clusters;
import edu.uconn.engr.dna.io.DefaultTwoFieldParser;
import edu.uconn.engr.dna.util.RandomAccessMap;
import edu.uconn.engr.dna.util.Utils;

import java.io.FileInputStream;
import java.util.*;

public abstract class AbstractAccurracyMeasurement {

	private static final String DummyClusterName = "DUMMYCLUSTER.1";
	protected boolean geneLevelEstimates;
	protected List<Double> intervals = new ArrayList<Double>();
	protected String[] frequencyFiles;
	protected String clustersFile;
	protected boolean normalize;

	public AbstractAccurracyMeasurement() {
		this(true);
	}

	public AbstractAccurracyMeasurement(boolean normalize) {
		this.normalize = normalize;
	}

	protected void processData(double[][] data) {
		double truth[] = data[0];
		if (normalize) {
			Utils.normalize(truth);
		}

		if (data.length == 1) {
			// only compute how many of the true values fall within each
			// interval
			for (int k = 1; k < intervals.size(); ++k) {
				double[] itruth = keepWithin(intervals.get(k - 1), intervals.get(k), truth, truth);
				System.out.print(itruth.length + "\t");
			}
			System.out.println(truth.length);
		} else {
			for (int i = 1; i < data.length; ++i) {
				if (normalize) {
					Utils.normalize(data[i]);
				}
				for (int k = 1; k < intervals.size(); ++k) {
					double[] itruth = keepWithin(intervals.get(k - 1),
							intervals.get(k), truth, truth);
					double[] iestimate = keepWithin(intervals.get(k - 1),
							intervals.get(k), truth, data[i]);
					String m = computeMeasure(itruth, iestimate);
					System.out.print(m + " ");
				}
				// All
				String m = computeMeasure(truth, data[i]);
				System.out.print(m + "\t");
			}
			System.out.println();
		}
	}

	protected abstract String computeMeasure(double[] itruth, double[] iestimate);

	private double[] keepWithin(Double min, Double max, double[] truth,
			double[] estimate) {
		List<Double> ok = new ArrayList<Double>();
		for (int i = 0; i < truth.length; ++i) {
			if (min < truth[i] && truth[i] <= max) {
				ok.add(estimate[i]);
			}
		}
		return DataUtils.unWrap(ok.toArray(new Double[0]));
	}

	protected double[][] loadData() throws Exception {
		if (geneLevelEstimates) {
			return loadFreqsAndGroupByClusters(clustersFile, frequencyFiles);
		} else {
			return DataUtils.loadFreqsFromFiles(frequencyFiles);
		}
	}

	protected void parseArguments(String[] args) {
		geneLevelEstimates = Boolean.parseBoolean(args[0]);
		int argpos = geneLevelEstimates ? 2 : 1;
		int nIntervals = Integer.parseInt(args[argpos++]);

		intervals = new ArrayList<Double>();
		if (nIntervals > 0) {
			for (int i = 0; i <= nIntervals; ++i) {
				intervals.add(Double.parseDouble(args[argpos++]));
			}
		}

		frequencyFiles = Arrays.copyOfRange(args, argpos, args.length);
		if (geneLevelEstimates) {
			clustersFile = args[1];
		}
	}

	protected double[][] loadFreqsAndGroupByClusters(String clustersFile,
			String[] freqFiles) throws Exception {
		Clusters clusters = new Clusters();
		DefaultTwoFieldParser.getInvertedTwoFieldParser(clusters).parse(
				new FileInputStream(clustersFile));
		Map<String, String> isoformToClusterMap = createIsoformToClusterMap(clusters);

		List<RandomAccessMap<String, Double>> dataByIsoform = DataUtils.loadFreqMapsFromFiles(freqFiles);
		List<RandomAccessMap<String, Double>> dataByCluster = groupAllByCluster(
				dataByIsoform, isoformToClusterMap);
		return DataUtils.loadFreqs(dataByCluster);
	}

	protected Map<String, String> createIsoformToClusterMap(Clusters clusters) {
		Map<String, String> map = new HashMap<String, String>();
		for (Cluster c : clusters.groupIterator()) {
			for (String iso : c.idIterator()) {
				map.put(iso, c.getName());
			}
		}
		return map;
	}

	protected List<RandomAccessMap<String, Double>> groupAllByCluster(
			List<RandomAccessMap<String, Double>> dataByIsoform,
			Map<String, String> isoformToClusterMap) {
		List<RandomAccessMap<String, Double>> result = new ArrayList<RandomAccessMap<String, Double>>();
		for (RandomAccessMap<String, Double> map : dataByIsoform) {
			result.add(DataUtils.convertToRandomAccessMap(groupByCluster(map,
					isoformToClusterMap)));
		}
		return result;
	}

	protected Map<String, Double> groupByCluster(
			RandomAccessMap<String, Double> map,
			Map<String, String> isoformToClusterMap) {
		Map<String, Double> result = new HashMap<String, Double>();
		for (String isoform : map.idIterator()) {
			String cluster = isoformToClusterMap.get(isoform);
			if (cluster == null) {
				cluster = DummyClusterName;
			}
			Double s = result.get(cluster);
			if (s == null) {
				s = 0.0;
			}
			result.put(cluster, map.getValue(isoform) + s);
		}
		result.remove(DummyClusterName);
		return result;
	}
}
