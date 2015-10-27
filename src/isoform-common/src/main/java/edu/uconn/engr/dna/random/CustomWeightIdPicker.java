package edu.uconn.engr.dna.random;

import edu.uconn.engr.dna.util.RandomAccessMap;

import java.util.HashMap;
import java.util.Map;

public class CustomWeightIdPicker<K, V extends RandomAccessMap<K, ?>>
	implements RandomPicker<K, V> {

	private int randomSeed;
	private RandomAccessMap<K, Double> map;

	private Map<Object, RandomPicker<Integer, Void>> pickersByRange;

	public CustomWeightIdPicker(RandomAccessMap<K, Double> weights,
			int randomNumberGeneratorSeed) {
		this.map = weights;
		this.randomSeed = randomNumberGeneratorSeed;
		this.pickersByRange = new HashMap<Object, RandomPicker<Integer,Void>>();
	}

	public K pick(V range) {
		RandomPicker<Integer, Void> picker = pickersByRange.get(range);
		if (picker == null) {
			picker = new RangePicker<K, V>(range, map, randomSeed);
			pickersByRange.put(range, picker);
		}
		return range.getKey(picker.pick(null));
	}

}
