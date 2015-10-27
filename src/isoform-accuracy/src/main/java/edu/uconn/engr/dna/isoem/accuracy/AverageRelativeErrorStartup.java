package edu.uconn.engr.dna.isoem.accuracy;

import java.io.FileNotFoundException;
import java.io.IOException;

public class AverageRelativeErrorStartup {

	/**
	 * args[0] first freqs file 
	 * args[1] second freqs file 
	 */
	public static void main(final String[] args) throws Exception {
		double[][] data = DataUtils.loadFreqs(args);
//		System.out.println("Original items: " + data[0].length);
		data = DataUtils.trimLessOrEqualTo(0.000000001, data);
//		System.out.println("After trim: " + data[0].length);
		for (int i = 1; i < args.length; ++i) {
			double[] err = DataUtils.relativeError(data[0], data[i]);
			System.out.print(average(absolute(err)) + "\t");
		}
		System.out.println();
	}

	private static double[] absolute(double[] err) {
		double[] absolute = new double[err.length];
		for (int i = 0; i < err.length; ++i) {
			absolute[i] = Math.abs(err[i]);
		}
		return absolute; 
	}

	private static double average(double[] d) {
		double sum = 0;
		for (int i = 0; i < d.length; ++i) {
			sum += d[i];
		}
		return sum / d.length;
	}

}
