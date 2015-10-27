package edu.uconn.engr.dna.random;

import edu.uconn.engr.dna.util.RandomAccessMap;

import java.util.HashMap;
import java.util.Map;

public class CustomWeightRandomPicker<K, V extends RandomAccessMap<K, ?>>
	implements RandomPicker<Integer, V> {

	private int randomSeed;
	private RandomAccessMap<K, Double> map;
	
	private Map<Object, RandomPicker<Integer, Void>> pickersByRange;

	public CustomWeightRandomPicker(RandomAccessMap<K, Double> weights,
			int randomNumberGeneratorSeed) {
		this.map = weights;
		this.randomSeed = randomNumberGeneratorSeed;
		this.pickersByRange = new HashMap<Object, RandomPicker<Integer,Void>>();
	}

	public Integer pick(V range) {
		RandomPicker<Integer, Void> picker = pickersByRange.get(range);
		if (picker == null) {
			picker = new RangePicker<K, V>(range, map, randomSeed);
			pickersByRange.put(range, picker);
		}
		return picker.pick(null);
	}
	
}
