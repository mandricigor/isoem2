package edu.uconn.engr.dna.isoem.accuracy;

import edu.uconn.engr.dna.format.Cluster;
import edu.uconn.engr.dna.format.Clusters;
import edu.uconn.engr.dna.io.DefaultTwoFieldParser;
import edu.uconn.engr.dna.util.RandomAccessMap;

import java.io.FileInputStream;
import java.util.*;

public class GeneLevelWithinFivePercentFromTrueStartup {

	private static final double EPS = 0.0001;

	public static void main(final String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("Arguments: clustersFile trueFrequencyFile frequencyFile1 [frequencyFile2 ...]");
			System.exit(-1);
		}
		
		Clusters clusters = new Clusters();
		DefaultTwoFieldParser.getInvertedTwoFieldParser(clusters)
			.parse(new FileInputStream(args[0]));
		Map<String, String> isoformToClusterMap = createIsoformToClusterMap(clusters);

		String[] freqFiles = Arrays.copyOfRange(args, 1, args.length);
		List<RandomAccessMap<String, Double>> dataByIsoform = DataUtils.loadFreqMapsFromFiles(freqFiles);
		List<RandomAccessMap<String, Double>> dataByCluster = groupAllByCluster(dataByIsoform, isoformToClusterMap);
		double[][] data = DataUtils.loadFreqs(dataByCluster);
		
		double truth[] = data[0];
//		System.out.println("Max gene level expression " + DataUtils.max(truth));
		double p = 0.05;
		for (int i = 1; i < data.length; ++i) {
			double[] estimate = data[i];
			int withinp = 0;
			for (int j = 0; j < estimate.length; ++j) {
				if (Math.abs(truth[j]-estimate[j]) <= p * truth[j]
				|| (truth[j]<EPS && estimate[j]<EPS)
				) {
					++withinp;
				}
			}
			System.out.print(withinp*100.0/estimate.length + "\t");
		}
		System.out.println();
	}

	private static Map<String, String> createIsoformToClusterMap(
			Clusters clusters) {
		Map<String, String> map = new HashMap<String, String>();
		for (Cluster c : clusters.groupIterator()) {
			for (String iso : c.idIterator()) {
				map.put(iso, c.getName());
			}
		}
		return map;
	}

	private static List<RandomAccessMap<String, Double>> groupAllByCluster(
			List<RandomAccessMap<String, Double>> dataByIsoform, Map<String, String> isoformToClusterMap) {
		List<RandomAccessMap<String, Double>> result = new ArrayList<RandomAccessMap<String,Double>>();
		for (RandomAccessMap<String, Double> map : dataByIsoform) {
			result.add(groupByCluster(map, isoformToClusterMap));
		}
		return result;
	}

	private static RandomAccessMap<String, Double> groupByCluster(
			RandomAccessMap<String, Double> map, Map<String, String> isoformToClusterMap) {
		Map<String, Double> result = new HashMap<String, Double>();
		for (String isoform : map.idIterator()) {
			String cluster = isoformToClusterMap.get(isoform);
			Double s = result.get(cluster);
			if (s == null) {
				s = 0.0;
			}
			result.put(cluster, map.getValue(isoform) + s);
		}
		return DataUtils.convertToRandomAccessMap(result);
	}
}
