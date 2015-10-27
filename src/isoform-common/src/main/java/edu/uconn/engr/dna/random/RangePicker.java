package edu.uconn.engr.dna.random;

import edu.uconn.engr.dna.util.RandomAccessMap;

import java.util.Arrays;
import java.util.Random;

public class RangePicker<K, V extends RandomAccessMap<K, ?>> 
	implements RandomPicker<Integer, Void> {

		double[] partialSums;
		private double totalSum;
		private Random random;

		@SuppressWarnings("unchecked")
		public RangePicker(RandomAccessMap<K, Double> rangeWithWeights,
				int randomNumberGeneratorSeed) {
			this((V)rangeWithWeights, 
					rangeWithWeights, randomNumberGeneratorSeed);
		}
		public RangePicker(V range, 
				RandomAccessMap<K, Double> weights, 
				int randomNumberGeneratorSeed) {
			this.partialSums = new double[range.size()];
			this.partialSums[0] = weights.getValue(range.getKey(0));
			for (int i = 1; i < range.size(); ++i) {
				this.partialSums[i] = this.partialSums[i-1] 
				     + (Double)weights.getValue(range.getKey(i));
			}
			this.totalSum = partialSums[range.size()-1];
			this.random = new Random(randomNumberGeneratorSeed);
		}
		
		@Override
		public Integer pick(Void info) {
			double r = random.nextDouble();
			r *= totalSum;
			int pos = Arrays.binarySearch(partialSums, 0, 
					partialSums.length, r);
			if (pos < 0) {
				pos = -pos - 1;
			}
			return pos;
		}
		
}

