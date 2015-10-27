package edu.uconn.engr.dna.isoem;

import java.util.*;

import edu.uconn.engr.dna.util.EmUtils;

@Deprecated
public class WeightedEMAlgorithm implements EMAlgorithm {

	private static final Double EPS = 0.0000000000000001;
	private Random random;
	private int steps;
	private final int maxRounds;
	private EMAlgorithm emForInitialFrequencies;
	private final Map<String, Double> usableIsoformLenghts;

	public WeightedEMAlgorithm(Map<String, Double> usableIsoformLenghts,
					int maxRounds, int randSeed) {
		this.random = new Random(randSeed);
		this.maxRounds = maxRounds;
		this.usableIsoformLenghts = usableIsoformLenghts;
	}

	public WeightedEMAlgorithm(Map<String, Double> usableIsoformLenghts,
					int maxRounds, Map<String, Double> initialIsoformFrequencies) {
		this.emForInitialFrequencies = createFakeEm(initialIsoformFrequencies);
		this.maxRounds = maxRounds;
		this.usableIsoformLenghts = usableIsoformLenghts;
	}

	public WeightedEMAlgorithm(Map<String, Double> usableIsoformLenghts,
					int maxRounds, EMAlgorithm emForInitialFrequencies) {
		this.emForInitialFrequencies = emForInitialFrequencies;
		this.maxRounds = maxRounds;
		this.usableIsoformLenghts = usableIsoformLenghts;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<Object, Double> computeFrequencies(List<IsoformList> readClasses) {
		this.steps = 0;

		Map<String, Integer> isoforms = getIsoformToIndexMap(readClasses);

		double[] freq = new double[isoforms.size()];
		double[] n = new double[isoforms.size()];

		if (random != null) {
			// assign random values to all frequencies
			for (int i = 0; i < freq.length; ++i) {
				// ensure nonzero initial frequencies
				freq[i] = getNonZeroDouble(random, 0.1, 0.9);
			}
		} else {
			Map<Object, Double> initialIsoformFrequencies = emForInitialFrequencies.computeFrequencies(readClasses);
			int i = 0;
			Random localRandomForNullFreqs = new Random(1);
			for (String isoform : isoforms.keySet()) {
				Double d = initialIsoformFrequencies.get(isoform);
				if (d == null || d < EPS) {
					freq[i] = getNonZeroDouble(localRandomForNullFreqs, 0.0001,
									0.2);
				} else {
					freq[i] = d;
				}
				++i;
			}
		}

		double s = 1.0;
		int step;
		double[] oldNormalizedFreq = Arrays.copyOf(freq, freq.length);
		EmUtils.normalize(oldNormalizedFreq);
		double[] normalizedFreq = new double[freq.length];
		for (step = 0; s > 0.00005 && step < maxRounds; ++step) {
			Arrays.fill(n, 0);
			for (IsoformList rc : readClasses) {
				if (rc.getMultiplicity() == 0) {
					continue;
				}
				double sum = 0;
				IsoformList classIsoforms = rc;
				if (!classIsoforms.isEmpty()) {
					for (IsoformList.Entry entry : classIsoforms.entrySet()) {
						double w = entry.getValue();
						int pos = isoforms.get(entry.getKey());
						sum += w * freq[pos];
					}
					for (IsoformList.Entry entry : classIsoforms.entrySet()) {
						double w = entry.getValue();
						int pos = isoforms.get(entry.getKey());
						if (sum >= EPS) {
							n[pos] += w * freq[pos] * rc.getMultiplicity() / sum;
						}
					}
				}
			}
			int pos = 0;
			for (String isoformId : isoforms.keySet()) {
				Double usableIsoLen = usableIsoformLenghts.get(isoformId);
				double usableIsoformLength = usableIsoLen == null ? 0.0
								: usableIsoLen;
				freq[pos] = usableIsoformLength > EPS ? n[pos]
								/ usableIsoformLength : 0.0;
				++pos;
			}

			System.arraycopy(freq, 0, normalizedFreq, 0, freq.length);
			EmUtils.normalize(normalizedFreq);
			s = 0;
			for (int j = 0; j < freq.length; ++j) {
				double old = oldNormalizedFreq[j];
				if (old > 0.00000000001) {
					double diff = Math.abs(normalizedFreq[j] - old) / old;
					if (diff > s) {
						s = diff;
					}
				}
			}
			double[] tmp = oldNormalizedFreq;
			oldNormalizedFreq = normalizedFreq;
			normalizedFreq = tmp;
		}
		this.steps = step;

		Map<String, Double> map = new HashMap<String, Double>();
		int i = 0;
		for (String isoformId : isoforms.keySet()) {
			if (Math.round(n[i]) >= 1) {
				map.put(isoformId, freq[i]);
			} else {
				map.put(isoformId, 0.0);
			}
			i++;
		}
		return (Map)map;
	}

	protected Map<String, Integer> getIsoformToIndexMap(
					List<IsoformList> readClasses) {
		Map<String, Integer> isoforms = new LinkedHashMap<String, Integer>();
		for (IsoformList rc : readClasses) {
			for (Object isoId : rc.keySet()) {
				if (!isoforms.containsKey((String)isoId)) {
					isoforms.put((String)isoId, isoforms.size());
				}
			}
		}
		return isoforms;
	}

	private double getNonZeroDouble(Random random, double min, double range) {
		return min + random.nextDouble() * range;
	}

	public int getSteps() {
		return steps;
	}

	private EMAlgorithm createFakeEm(
					final Map<String, Double> initialIsoformFrequencies2) {
		return new EMAlgorithm() {

			@Override
			public int getSteps() {
				return 0;
			}

			@Override
			public Map<Object, Double> computeFrequencies(
							List<IsoformList> readClasses) {
				return (Map)initialIsoformFrequencies2;
			}
		};
	}
}
