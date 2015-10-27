package edu.uconn.engr.dna.probability;

import edu.uconn.engr.dna.io.DefaultTwoFieldParser;
import edu.uconn.engr.dna.util.GroupedRandomAccessMap;
import edu.uconn.engr.dna.util.StringToDoubleRandomAccessMap;
import edu.uconn.engr.dna.util.Utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MapProbabilityDistribution extends CumulativeProbabilityDistribution {

	private final Map<Integer, Double> map;
	private final Map<Integer, Double> cumulativeMap;
	private double mean;
	private double variance;
	private int minKey;
	private final int maxKey;

	public MapProbabilityDistribution(Map<Integer, Double> values) {
		this.map = values;
		mean = 0;
		for (Map.Entry<Integer, Double> entry : values.entrySet()) {
			mean += entry.getKey() * entry.getValue();
		}
		variance = 0;
		for (Map.Entry<Integer, Double> entry : values.entrySet()) {
			variance += square(entry.getKey() - mean) * entry.getValue();
		}
		cumulativeMap = new TreeMap<Integer, Double>();
		double sum = 0;
		int prev = -1;
		int n = 0;
		for (Map.Entry<Integer, Double> entry : new TreeMap<Integer, Double>(values).entrySet()) {
			n = entry.getKey();
			if (prev == -1) {
				minKey = n;
			} else {
				while (++prev < n) {
					cumulativeMap.put(prev, sum);
				}
			}
			sum += entry.getValue();
			cumulativeMap.put(n, sum);
			prev = n;
		}
		maxKey = n; 
	}

	private Double square(double d) {
		return d * d;
	}

	@Override
	public int generateInt() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double cumulativeLowerTail(int r) {
		if (r < minKey) {
			return 0.0;
		}
		if (r > maxKey) {
			return 1.0;
		}
		return cumulativeMap.get(r);
	}
	@Override
	public double getMean() {
		return mean;
	}

	@Override
	public double getVariance() {
		return variance;
	}
	@Override
	public double getWeight(int i, int n) {
		return asNumber(map.get(i));
	}

	private double asNumber(Double d) {
		return d == null ? 0 : d;
	}

	public static MapProbabilityDistribution loadFromFile(String fileName) throws FileNotFoundException, Exception {
		GroupedRandomAccessMap<String, String, Double> map = new StringToDoubleRandomAccessMap<String>();
		DefaultTwoFieldParser.getRegularTwoFieldParser(map)
		.parse(new FileInputStream(fileName));
		Map<Integer, Double> map2 = new HashMap<Integer, Double>();
		for (int i = 0; i < map.size(); ++i) {
			String key = map.getKey(i);
			map2.put(Integer.parseInt(key), map.getValue(key));
		}
		map2 = Utils.normalizeMap(map2);
		return new MapProbabilityDistribution(map2);
	}

}
