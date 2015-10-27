/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uconn.engr.dna.probability;

import java.util.Arrays;
import java.util.Random;

import edu.uconn.engr.dna.common.RandomAccessMap;

public class RangePicker<K, V extends RandomAccessMap<K, ?>>
	implements RandomPicker<Integer, Void> {

		double[] partialSums;
		private double totalSum;
		private Random random;

		@SuppressWarnings("unchecked")
		public RangePicker(RandomAccessMap<K, Double> rangeWithWeights,
//				int randomNumberGeneratorSeed) {
				Random randomNumberGenerator) {
			this((V)rangeWithWeights,
//					rangeWithWeights, randomNumberGeneratorSeed);
					rangeWithWeights, randomNumberGenerator);
		}
		public RangePicker(V range,
				RandomAccessMap<K, Double> weights,
//				int randomNumberGeneratorSeed) {
				Random randomNumberGenerator) {
			this.partialSums = new double[range.size()];
			this.partialSums[0] = weights.get(range.get(0));
			for (int i = 1; i < range.size(); ++i) {
				this.partialSums[i] = this.partialSums[i-1]
				     + (Double)weights.get(range.get(i));
			}
			this.totalSum = partialSums[range.size()-1];
//			this.random = new Random(randomNumberGeneratorSeed);
			this.random = randomNumberGenerator;
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
