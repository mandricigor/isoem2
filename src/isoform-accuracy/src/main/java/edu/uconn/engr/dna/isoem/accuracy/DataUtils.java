package edu.uconn.engr.dna.isoem.accuracy;

import edu.uconn.engr.dna.format.Cluster;
import edu.uconn.engr.dna.io.DefaultTwoFieldParser;
import edu.uconn.engr.dna.util.DefaultRandomAccessMap;
import edu.uconn.engr.dna.util.GroupedRandomAccessMap;
import edu.uconn.engr.dna.util.RandomAccessMap;
import edu.uconn.engr.dna.util.StringToDoubleRandomAccessMap;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.KeypointPNGEncoderAdapter;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

public class DataUtils {

    public static RandomAccessMap<String, Double> loadFreqs(String fileName)
	    throws Exception {
	GroupedRandomAccessMap<String, String, Double> map = new StringToDoubleRandomAccessMap<String>();
	try {
	    DefaultTwoFieldParser.getRegularTwoFieldParser(map).parse(
		    new FileInputStream(fileName));
	} catch (Exception e) {
	    System.out.println("Error in file " + fileName);
	    throw e;
	}
	return map;
    }

    public static double[][] loadFreqs(String... fileNames) throws Exception {
	RandomAccessMap<String, Double> freqs = loadFreqs(fileNames[0]);
	int n = freqs.size();
	double[][] data = new double[fileNames.length][n];
	for (int i = 0; i < n; ++i) {
	    String key = freqs.getKey(i);
	    data[0][i] = freqs.getValue(key);
	}
	for (int j = 1; j < fileNames.length; ++j) {
	    RandomAccessMap<String, Double> nextFreqs = loadFreqs(fileNames[j]);
	    for (int i = 0; i < n; ++i) {
		String key = freqs.getKey(i);
		data[j][i] = nextFreqs.getValue(key);
	    }
	}
	return data;
    }

    public static double[][] loadFreqs(
	    List<RandomAccessMap<String, Double>> freqList)
	    throws IOException {
	RandomAccessMap<String, Double> freqs = freqList.get(0);
	int n = freqs.size();
	double[][] data = new double[freqList.size()][n];
	for (int i = 0; i < n; ++i) {
	    String key = freqs.getKey(i);
	    data[0][i] = freqs.getValue(key);
	}

	for (int j = 1; j < freqList.size(); ++j) {
	    RandomAccessMap<String, Double> nextFreqs = freqList.get(j);
	    for (int i = 0; i < n; ++i) {
		String key = freqs.getKey(i);
		Double d = nextFreqs.getValue(key);
		data[j][i] = d == null ? 0 : d;
	    }
	}
	return data;
    }

    public static Map<Integer, Double[][]> loadFreqsByClusterSize(
	    Map<String, Cluster> clusterForIsoform, int binSize,
	    String... fileNames) throws Exception {
	RandomAccessMap<String, Double> freqs = loadFreqs(fileNames[0]);
	int n = freqs.size();
	// double[][] data = new double[fileNames.length][n];
	int[] clusterSize = new int[n];
	int maxClusterSize = 0;
	for (int i = 0; i < n; ++i) {
	    String key = freqs.getKey(i);
	    Cluster cluster = clusterForIsoform.get(key);
	    clusterSize[i] = cluster == null ? 0 : cluster.size() / binSize;
	    if (clusterSize[i] > maxClusterSize) {
		maxClusterSize = clusterSize[i];
	    }
	}
	++maxClusterSize;
	int f[] = new int[maxClusterSize];
	for (int i = 0; i < n; ++i) {
	    ++f[clusterSize[i]];
	}
	Map<Integer, Double[][]> freqsByClusterSize = new HashMap<Integer, Double[][]>();
	for (int i = 0; i < maxClusterSize; ++i) {
	    if (f[i] > 0) {
		freqsByClusterSize.put(i, new Double[fileNames.length][f[i]]);
	    }
	}

	int[] dataSize = new int[maxClusterSize];
	Arrays.fill(dataSize, 0);
	for (int i = 0; i < n; ++i) {
	    String key = freqs.getKey(i);
	    int cs = clusterSize[i];
	    Double[][] data = freqsByClusterSize.get(cs);
	    data[0][dataSize[cs]++] = freqs.getValue(key);
	}
	for (int j = 1; j < fileNames.length; ++j) {
	    RandomAccessMap<String, Double> nextFreqs = loadFreqs(fileNames[j]);
	    Arrays.fill(dataSize, 0);
	    for (int i = 0; i < n; ++i) {
		String key = freqs.getKey(i);
		int cs = clusterSize[i];
		Double[][] data = freqsByClusterSize.get(cs);
		data[j][dataSize[cs]++] = nextFreqs.getValue(key);
	    }
	}
	return freqsByClusterSize;
    }

    public static Double[][] trimLessThan(double lowerLimit, Double[][] data) {
	Double[][] newData = new Double[data.length][];
	int n = data[0].length;
	for (int i = 0; i < data.length; ++i) {
	    newData[i] = new Double[n];
	}
	int s = 0;
	for (int i = 0; i < n; ++i) {
	    boolean ok = true;
	    for (int j = 0; j < data.length; ++j) {
		if (data[j][i] < lowerLimit) {
		    ok = false;
		    break;
		}
	    }
	    if (ok) {
		for (int j = 0; j < data.length; ++j) {
		    newData[j][s] = data[j][i];
		}
		++s;
	    }
	}
	for (int j = 0; j < data.length; ++j) {
	    newData[j] = Arrays.copyOf(newData[j], s);
	}
	return newData;
    }

    public static double[][] trimLessOrEqualTo(double lowerLimit,
	    double[][] data) {
	double[][] newData = new double[data.length][];
	int n = data[0].length;
	for (int i = 0; i < data.length; ++i) {
	    newData[i] = new double[n];
	}
	int s = 0;
	for (int i = 0; i < n; ++i) {
	    boolean ok = true;
	    for (int j = 0; j < data.length; ++j) {
		if (data[j][i] <= lowerLimit) {
		    ok = false;
		    break;
		}
	    }
	    if (ok) {
		for (int j = 0; j < data.length; ++j) {
		    newData[j][s] = data[j][i];
		}
		++s;
	    }
	}
	for (int j = 0; j < data.length; ++j) {
	    newData[j] = Arrays.copyOf(newData[j], s);
	}
	return newData;
    }

    public static void trimLessThan(double lowerLimit,
	    Map<Integer, Double[][]> data) {
	for (Map.Entry<Integer, Double[][]> entry : data.entrySet()) {
	    Double[][] oldValue = entry.getValue();
	    entry.setValue(trimLessThan(lowerLimit, oldValue));
	}
    }

    public static double[] unWrap(Double[] data) {
	double[] newData = new double[data.length];
	for (int i = 0; i < data.length; ++i) {
	    newData[i] = data[i];
	}
	return newData;
    }

    public static double[][] unWrap(Double[][] data) {
	double[][] newData = new double[data.length][];
	for (int i = 0; i < data.length; ++i) {
	    int n = data[i].length;
	    newData[i] = new double[n];
	    for (int j = 0; j < n; ++j) {
		newData[i][j] = data[i][j];
	    }
	}
	return newData;
    }

    public static double[] relativeError(double[] p, double[] pp) {
	double[] err = new double[p.length];
	for (int i = 0; i < p.length; ++i) {
	    err[i] = (p[i] - pp[i]) / p[i];
	}
	return err;
    }

    public static double[] squareDifference(double[] p, double[] pp) {
	double[] err = new double[p.length];
	for (int i = 0; i < p.length; ++i) {
	    err[i] = (p[i] - pp[i]) * (p[i] - pp[i]);
	}
	return err;
    }

    public static <T> List<T> waitForResults(ExecutorService es,
	    List<Future<T>> futures, final int waitMilliseconds) {
	List<T> results = new ArrayList<T>();
	while (!futures.isEmpty()) {
	    Iterator<Future<T>> iterator = futures.iterator();
	    while (iterator.hasNext()) {
		Future<T> f = iterator.next();
		T result = null;
		try {
		    result = f.get(waitMilliseconds, TimeUnit.MILLISECONDS);
		    results.add(result);
		    iterator.remove();
		} catch (TimeoutException e) {
		    // task is not done yet - come back later
		    continue;
		} catch (Exception e) {
		    // error
		    e.printStackTrace();
		    return null;
		}
	    }
	}
	es.shutdownNow();
	return results;
    }

    public static void saveToFile(String fileName, JFreeChart chart, int width,
	    int height) throws IOException {
	KeypointPNGEncoderAdapter encoder = new KeypointPNGEncoderAdapter();
	encoder.setEncodingAlpha(true);
	BufferedImage image = chart.createBufferedImage(width, height,
		BufferedImage.BITMASK, null);
	FileOutputStream os;
	encoder.encode(image, os = new FileOutputStream(fileName));
	os.close();
    }

    public static boolean hasIntersection(long start, long end, long start2,
	    long end2) {
	return (start2 <= start && start <= end2)
		|| (start2 <= end && end <= end2)
		|| (start <= start2 && start2 <= end)
		|| (start <= end2 && end2 <= end);
    }

    public static List<RandomAccessMap<String, Double>> loadFreqMapsFromFiles(
	    String... fileNames) throws IOException {
	ExecutorService es = Executors.newFixedThreadPool(Math.min(4,
		fileNames.length));
	List<Future<RandomAccessMap<String, Double>>> futures = new ArrayList<Future<RandomAccessMap<String, Double>>>();
	for (int i = 0; i < fileNames.length; ++i) {
	    final String fileName = fileNames[i];
	    futures.add(es
		    .submit(new Callable<RandomAccessMap<String, Double>>() {
			@Override
			public RandomAccessMap<String, Double> call()
				throws Exception {
			    return DataUtils.loadFreqs(fileName);
			}
		    }));
	}
	List<RandomAccessMap<String, Double>> freqList = new ArrayList<RandomAccessMap<String, Double>>();
	for (Future<RandomAccessMap<String, Double>> f : futures) {
	    try {
		freqList.add(f.get());
	    } catch (Exception e) {
		e.printStackTrace();
		es.shutdownNow();
		return null;
	    }
	}
	es.shutdownNow();
	return freqList;
    }

    public static double[][] loadFreqsFromFiles(String... fileNames)
	    throws IOException {
	List<RandomAccessMap<String, Double>> freqList = loadFreqMapsFromFiles(fileNames);
	return DataUtils.loadFreqs(freqList);
    }

    public static RandomAccessMap<String, Double> convertToRandomAccessMap(
	    Map<String, Double> map) {
	DefaultRandomAccessMap<String> rmap = new DefaultRandomAccessMap<String>();
	for (Map.Entry<String, Double> entry : map.entrySet()) {
	    rmap.add(entry.getKey(), entry.getValue());
	}
	return rmap;
    }

    public static Double max(double[] array) {
	double m = array[0];
	for (int i = 1; i < array.length; ++i) {
	    if (array[i] > m) {
		m = array[i];
	    }
	}
	return m;
    }

    public static List<Map.Entry<String, Double>> sortEntries(
	    Map<String, Double> freq) {
	List<Map.Entry<String, Double>> entries = new ArrayList<Entry<String, Double>>(
		freq.entrySet());
	Collections.sort(entries, new Comparator<Entry<String, Double>>() {
	    @Override
	    public int compare(Entry<String, Double> o1,
		    Entry<String, Double> o2) {
		return o2.getValue().compareTo(o1.getValue());
	    }
	});
	return entries;
    }

    public static void writeFrequencies(Map<String, Double> freq,
	    String outputFileName) throws IOException {
	List<Map.Entry<String, Double>> entries = sortEntries(freq);
	Writer writer = new PrintWriter(outputFileName);
	for (Map.Entry<String, Double> entry : entries) {
	    writer.write(entry.getKey());
	    writer.write("\t");
	    writer.write(entry.getValue().toString());
	    writer.write("\n");
	}
	writer.close();
    }

}
