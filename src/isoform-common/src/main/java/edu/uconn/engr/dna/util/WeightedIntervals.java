package edu.uconn.engr.dna.util;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jul 20, 2010
 * Time: 1:21:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class WeightedIntervals {

	public void scaleBy(double factor) {
		for (int i = 0; i < w.length; ++i) {
			w[i] *= factor;
		}
	}

	public interface WeightedIntervalHandler {

		void handle(int start, int end, double weight);
	}
	private double[] w;

	public WeightedIntervals(int length) {
		this.w = new double[length];
	}

	public void add(int start, int end, double count) {
		for (int i = start; i < end;) {
			w[i++] += count;
		}
	}

	public void merge(WeightedIntervals b) {
		if (b.w.length != w.length) {
			throw new IllegalStateException("Intervals should have equal lengths ("
					+ w.length
					+ " != "
					+ b.w.length
					+ ")");
		}

		for (int i = 0; i < w.length; ++i) {
			w[i] += b.w[i];
		}
	}

	public void traverseIntervals(WeightedIntervalHandler h, double differenceThreshold) {
		int start = 0;
		double intervalSum = w[0];
		for (int i = 1; i < w.length; ++i) {
			if (Math.abs(w[i] - w[start]) > differenceThreshold) {
				h.handle(start, i, intervalSum / (i - start));
				start = i;
				intervalSum = w[i];
			} else {
				intervalSum += w[i];
			}
		}
		h.handle(start, w.length, intervalSum / (w.length - start));
	}
}
