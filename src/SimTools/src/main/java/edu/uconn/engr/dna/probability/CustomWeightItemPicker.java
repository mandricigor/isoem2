package edu.uconn.engr.dna.probability;

import edu.uconn.engr.dna.common.RandomAccessMap;
import java.util.Random;

public class CustomWeightItemPicker<T> implements RandomPicker<T, Void> {

	private RangePicker<T, RandomAccessMap<T, Double>> rangePicker;
	private RandomAccessMap<T, Double> weights;

	public CustomWeightItemPicker(RandomAccessMap<T, Double> weights, 
//			int randomNumberGeneratorSeed) {
			Random randomNumberGenerator) {
		this.rangePicker = new RangePicker<T, RandomAccessMap<T,Double>>(
//				weights, randomNumberGeneratorSeed);
				weights, randomNumberGenerator);
		this.weights = weights;
		
	}
	
	@Override
	public T pick(Void info) {
		int pos = rangePicker.pick(info);
		return weights.get(pos); 
	}
	
}
