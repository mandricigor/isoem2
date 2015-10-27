package edu.uconn.engr.dna.util;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import edu.uconn.engr.dna.format.Cluster;
import edu.uconn.engr.dna.format.Clusters;
import edu.uconn.engr.dna.format.Isoform;
import edu.uconn.engr.dna.format.Isoforms;
import edu.uconn.engr.dna.io.DefaultTwoFieldParser;

public class EmUtils {

	public static Map<String, List<Isoform>> groupIsoformsByChromosome(Isoforms isoforms) {
		Map<String, List<Isoform>> r = new HashMap<String, List<Isoform>>();
		for (Isoform i : isoforms.isoformIterator()) {
			String c = i.getChromosome();
			List<Isoform> l = r.get(c);
			if (l == null) {
				r.put(c, l = new ArrayList<Isoform>());
			}
			l.add(i);
		}
		return r;
	}

	public static void normalize(double[] freq) {
		normalize(freq, 1.0);
	}

	public static void normalize(double[] freq, double rangeMax) {
		double sum = 0;
		for (int i = 0; i < freq.length; ++i) {
			sum += freq[i];
		}
		for (int i = 0; i < freq.length; ++i) {
			freq[i] = freq[i] * rangeMax / sum;
		}
	}

	public static Map<String, Double> normalize(Map<String, Double> map) {
		int n = map.size();
		double[] values = new double[n];
		int i = 0;
		for (Double val : map.values()) {
			values[i++] = val;
		}
		normalize(values);
		Map<String, Double> newMap = new HashMap<String, Double>();
		i = 0;
		for (String key : map.keySet()) {
			newMap.put(key, values[i++]);
		}
		return newMap;
	}

	public static <T> List<T> waitForResults(ExecutorService es,
					List<Future<T>> futures, final int waitMillisecondsBetweenPasses) {
		return waitForResults(es, futures, waitMillisecondsBetweenPasses, null,
						new ArrayList<T>());
	}

	public static <T> List<T> waitForResults(ExecutorService es,
					List<Future<T>> futures, final int waitMillisecondsBetweenPasses,
					final GenericHandler<Void, T> earlyProcessor) {
		List<T> results = new ArrayList<T>();
		return waitForResults(es, futures, waitMillisecondsBetweenPasses,
						earlyProcessor, results);
	}

	public static <T> List<T> waitForResults(ExecutorService es,
					List<Future<T>> futures, final int waitMillisecondsBetweenPasses,
					final GenericHandler<Void, T> earlyProcessor, final List<T> results) {
		while (!futures.isEmpty()) {
			try {
				collectFinished(futures, earlyProcessor, results);
			} catch (Exception e) {
				// error
				e.printStackTrace(System.err);
				e.printStackTrace(System.out);
				es.shutdownNow();
				return null;
			}

			if (futures.size() > 0) {
				try {
					Thread.sleep(waitMillisecondsBetweenPasses);
				} catch (InterruptedException e) {
					e.printStackTrace(System.err);
					e.printStackTrace(System.out);
				}
			}
		}
		es.shutdownNow();
		return results;
	}

	public static <T> int collectFinished(List<Future<T>> futures,
					final GenericHandler<Void, T> earlyProcessor, List<T> output)
					throws InterruptedException, ExecutionException {
		Iterator<Future<T>> iterator = futures.iterator();
		int finished = 0;
		while (iterator.hasNext()) {
			Future<T> f = iterator.next();
			T result = null;
			if (f.isDone()) {
				result = f.get();
				++finished;
				iterator.remove();
				if (earlyProcessor != null) {
					earlyProcessor.handle(result);
				}
				if (output != null) {
					output.add(result);
				}
			}
		}
		return finished;
	}

	public static void addMissingIds(Map<String, Double> freq,
					Iterable<String> ids) {
		addMissingIds(freq, ids, 0.0);
	}

	public static <T> void addMissingIds(Map<String, T> freq,
					Iterable<String> ids, T value) {
		for (String isoId : ids) {
			if (freq.get(isoId) == null) {
				freq.put(isoId, value);
			}
		}
	}

	public static char reverseStrand(char strand) {
		return strand == '+' ? '-' : '+';
	}

	public static Map<String, Double> loadMap(String fileName) throws Exception {
		GroupedRandomAccessMap<String, String, Double> map = new StringToDoubleRandomAccessMap<String>();
		DefaultTwoFieldParser.getRegularTwoFieldParser(map).parse(
						new FileInputStream(fileName));
		return convertToRegularMap(map);
	}

	public static Map<String, Double> convertToRegularMap(
					GroupedRandomAccessMap<String, String, Double> map) {
		Map<String, Double> map2 = new HashMap<String, Double>();
		for (Map.Entry<String, Double> entry : map.entrySet()) {
			map2.put(entry.getKey(), entry.getValue());
		}
		return map2;
	}

	@Deprecated
	public static Map<String, Double> fpkm(Map<String, Double> freq,
					Isoforms isoforms, double mu) {
		double sum = 0;
		for (Isoform isoform : isoforms.groupIterator()) {
			Double d = freq.get(isoform.getName());
			sum += (d == null ? 0 : d) * Math.max(0, isoform.length() - mu + 1);
		}
		Map<String, Double> fpkms = new HashMap<String, Double>();
		for (Isoform isoform : isoforms.groupIterator()) {
			Double d = freq.get(isoform.getName());
			fpkms.put(isoform.getName(), (d == null ? 0 : d) / sum
							* 1000000000.0);
		}
		return fpkms;
	}

	public static void synchronizeClustersWithIsoforms(Clusters clusters,
					Isoforms isoforms) {
		Set<String> seenIsoforms = new HashSet<String>();
		List<String> clustersToRemove = null;
		for (String clusterId : clusters.idIterator()) {
			Cluster c = clusters.getValue(clusterId);
			List<String> toRemove = null;
			for (String isoformId : c.idIterator()) {
				if (null == isoforms.getValue(isoformId)) {
					// isoform not found in known gene file
					if (toRemove == null) {
						toRemove = new ArrayList<String>();
					}
					toRemove.add(isoformId);
				} else {
					seenIsoforms.add(isoformId);
				}
			}
			if (toRemove != null) {
				for (String isoformId : toRemove) {
					c.removeValue(isoformId);
				}
				if (c.size() == 0) {
					if (clustersToRemove == null) {
						clustersToRemove = new ArrayList<String>();
					}
					clustersToRemove.add(clusterId);
				}
			}
		}
		if (clustersToRemove != null) {
			for (String cluster : clustersToRemove) {
				clusters.removeValue(cluster);
			}
		}
		List<String> isoformsWithoutClusters = null;
		for (String isoformId : isoforms.idIterator()) {
			if (!seenIsoforms.contains(isoformId)) {
				if (isoformsWithoutClusters == null) {
					isoformsWithoutClusters = new ArrayList<String>();
				}
				isoformsWithoutClusters.add(isoformId);
			}
		}
		if (isoformsWithoutClusters != null) {
			// create a dummy cluster for isoforms that are in known
			// gene but have no cluster
			Cluster c = new Cluster("UNCLUSTERED");
			for (String isoformId : isoformsWithoutClusters) {
				c.addIsoform(isoformId);
			}
			clusters.put("UNCLUSTERED", c);
		}

	}

	public static Map<String, Integer>[] createFakeKmerCount() {
		Map<String, Integer>[] kmerCount = new Map[100];
		for (int j = 0; j < kmerCount.length; ++j) {
			kmerCount[j] = new HashMap<String, Integer>() {

				@Override
				public Integer get(Object key) {
					return 1;
				}
			};
		}
		return kmerCount;
	}

	public static Collection<String> getAllChromosomes(Isoforms isoforms) {
		Set<String> chromosomes = new HashSet<String>();
		for (Isoform isoform : isoforms.groupIterator()) {
			chromosomes.add(isoform.getChromosome());
		}
		return chromosomes;
	}

	public static Map<String, Double> fpkm(Map<String, Double> map, Map<String, Double> adjustedWeights) {
		double sum = 0;
		for (Map.Entry<String, Double> freq : map.entrySet()) {
			Double d = freq.getValue();
			sum += (d == null ? 0 : d) * getNumber(adjustedWeights.get(freq.getKey()));
		}
		Map<String, Double> fpkms = new HashMap<String, Double>();
		for (Map.Entry<String, Double> freq : map.entrySet()) {
			Double d = freq.getValue();
			fpkms.put(freq.getKey(), (d == null ? 0 : d) / sum
							* 1000000000.0);
		}
		return fpkms;
	}

	private static double getNumber(Object obj) {
		if (obj == null) return 0;
		else return ((Number)obj).doubleValue();
	}
}
