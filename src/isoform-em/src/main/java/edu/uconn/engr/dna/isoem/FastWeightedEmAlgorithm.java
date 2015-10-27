package edu.uconn.engr.dna.isoem;

import edu.cornell.lassp.houle.RngPack.Ranlux;
import edu.uconn.engr.dna.util.EmUtils;
import edu.uconn.engr.dna.util.PrimitiveDoubleArray;
import edu.uconn.engr.dna.util.PrimitiveIntArray;
import edu.uconn.engr.dna.util.Utils;

import java.util.*;

public class FastWeightedEmAlgorithm implements EMAlgorithm {

	private static final Double EPS = 0.000000000001;
	private int steps;
	private final int maxRounds;
	private final Map<Object, Double> usableIsoformLenghts;
	private boolean reportCounts;
	private final Ranlux random;

	public FastWeightedEmAlgorithm(Map<Object, Double> usableIsoformLenghts,
					int maxRounds, int randSeed, boolean reportCounts) {
		this.reportCounts = reportCounts;
		this.maxRounds = maxRounds;
		this.usableIsoformLenghts = usableIsoformLenghts;
		this.random = new Ranlux(4, randSeed);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Map<Object, Double> computeFrequencies(List<IsoformList> readClasses) {
		this.steps = 0;

		PreprocData d = new PreprocData(readClasses);

		int nDistinctIsoforms = d.freq.length;
		double[] oldNormalizedFreq = Arrays.copyOf(d.freq, nDistinctIsoforms);
		EmUtils.normalize(oldNormalizedFreq);

		double[] n = new double[nDistinctIsoforms];
		double s = 1.0;
		int step;
		int totalIsoformsForAllReadClasses = d.w.length;
		for (step = 0; s > 0.00005 && step < maxRounds; ++step) {
			// E step: compute counts
			Arrays.fill(n, 0);
			for (int i = 0; i < totalIsoformsForAllReadClasses;) {
				// find read class end
				int j;
				for (j = i; d.rcStartAndSize[++j] == 0;) {
				}

				// computed weighted sum of isoforms in the current read class
				double sum = 0;
				for (int k = i; k < j; ++k) {
					sum += d.w[k] * d.freq[d.pos[k]];
				}

				// compute counts of reads per isoforms
				if (sum > EPS) {
					double sz = d.rcStartAndSize[i];
					for (int k = i; k < j; ++k) {
						n[d.pos[k]] += d.w[k] * d.freq[d.pos[k]] * sz / sum;
					}
				}
				i = j;
			}

			// M step: re-estimate frequencies
			for (int i = 0; i < nDistinctIsoforms; ++i) {
				double usableIsoformLength = d.usableIsoLen[i];
				d.freq[i] = usableIsoformLength > EPS ? n[i]
								/ usableIsoformLength : 0.0;
			}

			// Convergence test
			s = 0;
			double sum = Utils.sum(d.freq);
			for (int j = 0; j < nDistinctIsoforms; ++j) {
				double old = oldNormalizedFreq[j];
				double nev = oldNormalizedFreq[j] = d.freq[j] / sum;
				if (old > EPS) {
					double diff = Math.abs(nev - old) / old;
					if (diff > s) {
						s = diff;
					}
				}
			}
		}
		this.steps = step;

		// reuse the map of indices to report frequencies
		int i = 0;
		for (Map.Entry<Object, Number> entry : d.isoforms.entrySet()) {
			if (Math.round(n[i]) >= 1) {
				if (reportCounts) {
					entry.setValue(n[i]);
				} else {
					entry.setValue(d.freq[i]);
				}
			} else {
				entry.setValue(0.0);
			}
			i++;
		}
		return (Map) d.isoforms;
	}

	public int getSteps() {
		return steps;
	}

	class PreprocData {

		// read class boundaries and sizes
		// to save space, position i in array rcStartAndSize has value
		// 0 if isoform i is not the first in the current read class or
		// number of reads in class if position i is the beginning of a
		// read class
		final double[] rcStartAndSize;
		// weights of isoforms in classes
		final double[] w;
		// indices of read class isoforms
		final int[] pos;
		// mapping from isoforms to
		// consecutive
		// numerical indexes
		final Map<Object, Number> isoforms;
		// initial frequencies of isoforms
		final double[] freq;
		// effective lengths used for
		// normalization
		final double[] usableIsoLen;

		public PreprocData(List<IsoformList> readClasses) {
			// IDEA: concatenate the isoforms of all classes,
			// because each iteration will keep doing the same steps
			isoforms = new LinkedHashMap<Object, Number>();

			PrimitiveDoubleArray wL = new PrimitiveDoubleArray();
			PrimitiveIntArray posL = new PrimitiveIntArray();
			PrimitiveDoubleArray rcStartAndSizeL = new PrimitiveDoubleArray();
			PrimitiveDoubleArray freqL = new PrimitiveDoubleArray();
			PrimitiveDoubleArray usableIsoLenL = new PrimitiveDoubleArray();

			for (IsoformList classIsoforms : readClasses) {
				if (!classIsoforms.isEmpty()) {
					boolean firstIsoInClass = true;
					for (IsoformList.Entry entry : classIsoforms.entrySet()) {
						if (firstIsoInClass) {
							// mark read class start and size
							rcStartAndSizeL.add(classIsoforms.getMultiplicity());
							firstIsoInClass = false;
						} else {
							rcStartAndSizeL.add(0);
						}

						String isoId = entry.getKey();
						Number index = isoforms.get(isoId);
						if (index == null) {
							// new isoform
							isoforms.put(isoId, index = isoforms.size());

							// store usable length
							Double usable = usableIsoformLenghts.get(isoId);
							usableIsoLenL.add((usable == null || usable <= EPS) ? 0.0
											: usable);

							// assign nonzero random values to all frequencies
							double d = random.uniform(0.0001, 1.0);
							freqL.add(d);
						}

						// store isoform weight and isoform index (id)
						wL.add(entry.getValue());
						posL.add(index.intValue());
					}
				}
			}
			rcStartAndSizeL.add(1); // add sentinel

			w = wL.toArray();
			pos = posL.toArray();
			rcStartAndSize = rcStartAndSizeL.toArray();

			freq = freqL.toArray();
			usableIsoLen = usableIsoLenL.toArray();

		}
	}
}
