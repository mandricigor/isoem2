package edu.uconn.engr.dna.util;

import edu.uconn.engr.dna.format.Cluster;
import edu.uconn.engr.dna.format.Clusters;
import edu.uconn.engr.dna.format.Isoform;
import edu.uconn.engr.dna.format.Isoforms;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;


public class Utils {

	public static final double INFINITY = 1.5e100;
	public static final Double EPS = 0.0000000000000001;
	private static final String DummyClusterName = "DUMMYCLUSTER.1";
	private static double[] phredProb = new double[100]; // only 93 are actually used

	static {
		for (int phredScore = 0; phredScore < phredProb.length; ++phredScore) {
			phredProb[phredScore] = Math.pow(10, -phredScore / 10.0);
		}
	}

	public static void appendIntervals(StringBuilder sb, Intervals intervals) {
		sb.append(intervals.size());
		sb.append('\t');
		for (int i = 0; i < intervals.size(); ++i) {
			sb.append(intervals.getStart(i));
			sb.append(',');
		}
		sb.append('\t');
		for (int i = 0; i < intervals.size(); ++i) {
			sb.append(intervals.getEnd(i));
			sb.append(',');
		}
	}

	public static byte[] arrayOf(int length, byte value) {
		byte[] array = new byte[length];
		Arrays.fill(array, value);
		return array;
	}

	public static double[] arrayOf(int length, double value) {
		double[] array = new double[length];
		Arrays.fill(array, value);
		return array;
	}

	public static String stringOf(int length, char c) {
		char[] array = new char[length];
		Arrays.fill(array, c);
		return new String(array);
	}

	public static void reverse(char[] array) {
		int n = array.length;
		for (int i = (n - 1) / 2; i >= 0; --i) {
			char t = array[i];
			array[i] = array[n - i - 1];
			array[n - i - 1] = t;
		}
	}

	public static String reverse(CharSequence sequence) {
		int n = sequence.length();
		char[] array = new char[n];
		for (int i = n - 1; i >= 0; --i) {
			array[i] = sequence.charAt(n - i - 1);
		}
		return new String(array);
	}

	public static String reverse(String sequence) {
		int n = sequence.length();
		char[] array = new char[n];
		sequence.getChars(0, n, array, 0);
		for (int i = (n - 1) / 2; i >= 0; --i) {
			char t = array[i];
			array[i] = array[n - i - 1];
			array[n - i - 1] = t;
		}
		return new String(array);
	}

	public static void reverseComplement(char[] array) {
		int n = array.length;
		for (int i = (n - 1) / 2; i >= 0; --i) {
			char t = complement[array[i]];
			array[i] = complement[array[n - i - 1]];
			array[n - i - 1] = t;
		}
	}

	public static String reverseComplement(String sequence) {
		int n = sequence.length();
		char[] array = new char[n];
		sequence.getChars(0, n, array, 0);
		for (int i = (n - 1) / 2; i >= 0; --i) {
			char t = complement[array[i]];
			array[i] = complement[array[n - i - 1]];
			array[n - i - 1] = t;
		}
		return new String(array);
	}

	public static String reverseComplement(CharSequence sequence) {
		int n = sequence.length();
		char[] array = new char[n];
		for (int i = n - 1; i >= 0; --i) {
			array[i] = complement[sequence.charAt(n - i - 1)];
		}
		return new String(array);
	}

	public static String complement(CharSequence sequence) {
		char[] array = new char[sequence.length()];
		for (int i = 0; i < sequence.length(); ++i) {
			array[i] = complement[sequence.charAt(i)];
		}
		return new String(array);
	}

	public static CharSequence strandCheck(char strand, CharSequence sequence) {
		if (strand == '+') {
			return sequence;
		} else {
			return Utils.reverseComplement(sequence);
		}
	}
	private static char[] complement = new char[256];

	static {
		Arrays.fill(complement, 'N');
		complement['a'] = 't';
		complement['t'] = 'a';
		complement['c'] = 'g';
		complement['g'] = 'c';
		complement['A'] = 'T';
		complement['T'] = 'A';
		complement['C'] = 'G';
		complement['G'] = 'C';
	}

	/**
	 * Selects only those isoforms which appear in the given cluster. Preserves
	 * the order of the isoforms as in the bigger collection.
	 *
	 * @param isoforms a list of known isoforms
	 * @param cluster
	 * @return a list of isoforms whose names appear in the cluster
	 */
	public static Collection<Isoform> select(Isoforms isoforms, Cluster cluster) {
		Map<Integer, Isoform> map = new TreeMap<Integer, Isoform>();
		for (String isoformId : cluster.idIterator()) {
			if (isoforms.containsKey(isoformId)) {
				map.put(isoforms.getIndexOf(isoformId), isoforms.getValue(isoformId));
			}
		}
		return map.values();
	}

	public static List<Integer> createList(int start, int end) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = start; i <= end; ++i) {
			list.add(i);
		}
		return list;
	}

	public static void printArray(int[] perm) {
		for (int i = 0; i < perm.length; ++i) {
			System.out.print(perm[i] + " ");
		}
		System.out.println();
	}

	public static void printMatrix(double[][] best) {
		for (int i = 0; i < best.length; ++i) {
			for (int j = 0; j < best[i].length; ++j) {
				System.out.print(best[i][j] + " ");
			}
			System.out.println();
		}
	}

	public static <V> Collection<V> flatten(Collection<? extends Collection<V>> cols) {
		if (cols.size() == 0) {
			return Collections.emptyList();
		} else if (cols.size() == 1) {
			return cols.iterator().next();
		} else {
			Collection<V> result = new ArrayList<V>();
			for (Collection<V> c : cols) {
				for (V v : c) {
					result.add(v);
				}
			}
			return result;
		}
	}

	public static void normalize(double[] freq) {
		double sum = 0;
		for (int i = 0; i < freq.length; ++i) {
			sum += freq[i];
		}
		for (int i = 0; i < freq.length; ++i) {
			freq[i] = freq[i] / sum;
		}
	}

	public static RandomAccessMap<String, Double> normalize(
			RandomAccessMap<String, Double> map) {
		int n = map.size();
		double[] values = new double[n];
		int i = 0;
		for (Double val : map.groupIterator()) {
			values[i++] = val;
		}
		normalize(values);
		DefaultRandomAccessMap<String> newMap = new DefaultRandomAccessMap<String>();
		for (i = 0; i < n; ++i) {
			newMap.add(map.getKey(i), values[i]);
		}
		return newMap;
	}

	public static RandomAccessMap<String, Double> sort(
			RandomAccessMap<String, Double> map, boolean ascending) {
		List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(
				map.entrySet());
		Comparator<Entry<String, Double>> comparator;
		if (ascending) {
			comparator = new Comparator<Map.Entry<String, Double>>() {

				@Override
				public int compare(Entry<String, Double> o1,
						Entry<String, Double> o2) {
					return o1.getValue().compareTo(o2.getValue());
				}
			};
		} else {
			comparator = new Comparator<Map.Entry<String, Double>>() {

				@Override
				public int compare(Entry<String, Double> o1,
						Entry<String, Double> o2) {
					return o2.getValue().compareTo(o1.getValue());
				}
			};
		}
		Collections.sort(list, comparator);
		DefaultRandomAccessMap<String> newMap = new DefaultRandomAccessMap<String>();
		for (Map.Entry<String, Double> entry : list) {
			newMap.add(entry.getKey(), entry.getValue());
		}
		return newMap;
	}

	public static int intValue(boolean b) {
		return b ? 1 : 0;
	}

	public static <T, V extends Number> Map<T, Double> normalizeMap(
			Map<T, V> map) {
		double sum = 0;
		for (Number val : map.values()) {
			sum += val.doubleValue();
		}
		Map<T, Double> newMap = new HashMap<T, Double>();
		for (Map.Entry<T, V> entry : map.entrySet()) {
			newMap.put(entry.getKey(), entry.getValue().doubleValue() / sum);
		}
		return newMap;
	}

	public static int parseInt(CharSequence s, int radix, int startPos,
			int endPos) throws NumberFormatException {
		if (s == null) {
			throw new NumberFormatException("null");
		}

		if (radix < Character.MIN_RADIX) {
			throw new NumberFormatException("radix " + radix
					+ " less than Character.MIN_RADIX");
		}

		if (radix > Character.MAX_RADIX) {
			throw new NumberFormatException("radix " + radix
					+ " greater than Character.MAX_RADIX");
		}

		int result = 0;
		boolean negative = false;
		int i = startPos, max = endPos;
		int limit;
		int multmin;
		int digit;

		if (max > 0) {
			if (s.charAt(i) == '-') {
				negative = true;
				limit = Integer.MIN_VALUE;
				i++;
			} else {
				limit = -Integer.MAX_VALUE;
			}
			multmin = limit / radix;
			if (i < max) {
				digit = Character.digit(s.charAt(i++), radix);
				if (digit < 0) {
					throw nfe(s, startPos, endPos);
				} else {
					result = -digit;
				}
			}
			while (i < max) {
				// Accumulating negatively avoids surprises near MAX_VALUE
				digit = Character.digit(s.charAt(i++), radix);
				if (digit < 0) {
					throw nfe(s, startPos, endPos);
				}
				if (result < multmin) {
					throw nfe(s, startPos, endPos);
				}
				result *= radix;
				if (result < limit + digit) {
					throw nfe(s, startPos, endPos);
				}
				result -= digit;
			}
		} else {
			throw nfe(s, startPos, endPos);
		}
		if (negative) {
			if (i > 1) {
				return result;
			} else { /* Only got "-" */
				throw nfe(s, startPos, endPos);
			}
		} else {
			return -result;
		}
	}

	private static NumberFormatException nfe(CharSequence string, int startPos,
			int endPos) {
		return new NumberFormatException("For input string: \""
				+ string.subSequence(startPos, endPos) + "\"");
	}

	public static boolean regionMatches(CharSequence a, int i, CharSequence b,
			int j, int len) {
		if (i + len > a.length() || j + len > b.length() || i < 0 || j < 0
				|| len < 0) {
			return false;
		}
		while (len-- > 0) {
			if (a.charAt(i++) != b.charAt(j++)) {
				return false;
			}
		}
		return true;
	}

	public static Map<String, String> createIsoformToClusterMap(
			Isoforms isoforms, Clusters clusters) {
		Map<String, String> map = new HashMap<String, String>();
		for (Cluster c : clusters.groupIterator()) {
			for (String iso : c.idIterator()) {
				map.put(iso, c.getName());
			}
		}
		for (String isoform : isoforms.idIterator()) {
			if (!map.containsKey(isoform)) {
				String fakeCluster = DummyClusterName;
				map.put(isoform, fakeCluster);
				clusters.add(fakeCluster, isoform);
			}
		}
		return map;
	}

	public static boolean equals(int[] a, int[] b, int length) {
		for (int i = 0; i < length; ++i) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}

	public static boolean equals(long[] a, long[] b, int length) {
		for (int i = 0; i < length; ++i) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}

	public static Map<String, Double> groupByCluster(Map<String, Double> map,
			Map<String, String> isoformToClusterMap) {
		Map<String, Double> result = new HashMap<String, Double>();
		for (String isoform : map.keySet()) {
			String cluster = isoformToClusterMap.get(isoform);
			if (cluster == null) {
				cluster = DummyClusterName;
			}
			Double s = result.get(cluster);
			if (s == null) {
				s = 0.0;
			}
			result.put(cluster, map.get(isoform) + s);
		}
		return result;
	}
	public static Map<String, Double> fpkmWeigthedGeneLengths(Map<String, Double> fpkms,
			Isoforms isoforms, Map<String, String> isoformToClusterMap) {
                Map<String, Double> weightedIsoformLengths = fpkmWeigthedIsoformLengths(fpkms, isoforms);
		Map<String, Integer> IsoCount = new HashMap<String, Integer>();
		for (String isoform : fpkms.keySet()) {
			String cluster = isoformToClusterMap.get(isoform);
			if (cluster == null) {
				cluster = DummyClusterName;
			}
			Integer count = IsoCount.get(cluster);
			if (count == null) {
				count = 0;
			}
			IsoCount.put(cluster,count+1);
		}
		Map<String, Double> result = new HashMap<String, Double>();
		for (String isoform : weightedIsoformLengths.keySet()) {
			String cluster = isoformToClusterMap.get(isoform);
			if (cluster == null) {
				cluster = DummyClusterName;
			}
			Double s = result.get(cluster);
			if (s == null) {
				s = 0.0;
			}
			result.put(cluster, weightedIsoformLengths.get(isoform)/Math.max(1,IsoCount.get(cluster)) + s);
	  	       //System.out.println("gene " + cluster + " count " + IsoCount.get(cluster) + " weightedLength/coutn " + (weightedIsoformLengths.get(isoform)/Math.max(1,IsoCount.get(cluster)))  + "weighted length " + (weightedIsoformLengths.get(isoform)/Math.max(1,IsoCount.get(cluster)) + s));
		}
		return result;
	}
	public static Map<String, Double> fpkmWeigthedIsoformLengths(Map<String, Double> fpkms,
			Isoforms isoforms) {

            Map<String, Double> result = new HashMap<String, Double>();
	    for (String isoform : fpkms.keySet()) {
                Isoform i = isoforms.getValue(isoform);
                Double fpkm = fpkms.get(isoform);
                Double fpkmWeightdLength = Math.max(0.001,fpkm) * i.length();
                result.put(isoform, fpkmWeightdLength);
		  //System.out.println("fpkm " + fpkm + " length " + i.length() + " weightedLength " + fpkmWeightdLength );
	   }
		return result;
	}

	public static double sum(double[] a) {
		double s = 0;
		for (int i = 0; i < a.length; ++i) {
			s += a[i];
		}
		return s;
	}

	public static double[] toDoubleArray(List<? extends Number> a) {
		double[] d = new double[a.size()];
		for (int i = 0; i < d.length; ++i) {
			d[i] = a.get(i).doubleValue();
		}
		return d;
	}

	public static int[] toIntArray(List<Integer> a) {
		int[] d = new int[a.size()];
		for (int i = 0; i < d.length; ++i) {
			d[i] = a.get(i);
		}
		return d;
	}

	public static <R, T> List<T> map(Iterable<R> col, Converter<R, T> converter) {
		List<T> result = new ArrayList<T>();
		for (R r : col) {
			result.add(converter.convert(r));
		}
		return result;
	}

	public static <R, V> void mergeMaps(Map<R, V> dest, Map<R, V> src, UniformBinaryOperator<V> mergeOperation) {
		for (Map.Entry<R, V> entry : src.entrySet()) {
			R key = entry.getKey();
			V srcValue = entry.getValue();
			V destValue = dest.get(key);
			if (destValue == null) {
				dest.put(key, srcValue);
			} else {
				dest.put(key, mergeOperation.compute(destValue, srcValue));
			}
		}
	}

	public static <T, I extends Iterable<T>> T reduce(I items, UniformBinaryOperator<T> operator) {
		Iterator<T> iterator = items.iterator();
		if (!iterator.hasNext()) {
			return null;
		}
		T i = iterator.next();
		while (iterator.hasNext()) {
			i = operator.compute(i, iterator.next());
		}
		return i;
	}

	/**
	 * Convert a single printable ASCII FASTQ format phred score to binary phred score.
	 *
	 * @param ch Printable ASCII FASTQ format phred score.
	 * @return Binary phred score.
	 */
	public static int fastqToPhred(final char ch) {
		if (ch < 33 || ch > 126) {
			throw new IllegalArgumentException("Invalid fastq character: " + ch);
		}
		return (ch - 33);
	}

	public static double phredProbability(int phredScore) {
		return phredProb[phredScore];
	}

	public static <T extends Number, R> T get(Map<R, T> map, R key, T defaultValue) {
		T val = map.get(key);
		return val == null ? defaultValue : val;
	}

	public static List<Map.Entry<String, Double>> sortEntriesDesc(Map<String, Double> freq) {
		List<Map.Entry<String, Double>> entries = new ArrayList<Map.Entry<String, Double>>(
				freq.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, Double>>() {

			@Override
			public int compare(Map.Entry<String, Double> o1,
					Map.Entry<String, Double> o2) {
                                return o1.getKey().compareTo(o2.getKey());
			}
		});
		return entries;
	}


	public static List<Map.Entry<String, Double>> sortEntriesById(Map<String, Double> freq) {
		List<Map.Entry<String, Double>> entries = new ArrayList<Map.Entry<String, Double>>(
				freq.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, Double>>() {

			@Override
			public int compare(Map.Entry<String, Double> o1,
					Map.Entry<String, Double> o2) {
                                return o1.getKey().compareTo(o2.getKey());
				//return o2.getValue().compareTo(o1.getValue());
			}
		});
		return entries;
	}


	public static <K, V> void writeValues(Collection<Map.Entry<K, V>> entries,
			String outputFileName) throws IOException {
                String dirName = null;
                int lastIndexOfSlash = outputFileName.lastIndexOf("/");
                if (lastIndexOfSlash != -1) {
                    dirName = outputFileName.substring(0, lastIndexOfSlash);
                }
                File f = new File(dirName);
                f.mkdirs();
		Writer writer = new PrintWriter(outputFileName);
		for (Map.Entry<?, ?> entry : entries) {
			writer.write(String.valueOf(entry.getKey()));
			writer.write("\t");
			writer.write(String.valueOf(entry.getValue()));
			writer.write("\n");
		}
		writer.close();
	}


        public static void removeDir(String dirName) {
            File directory = new File(dirName);
            try {
                deleteFile(directory);
            } catch(Exception e) {
                System.err.println("Could not remove safely directory: " + dirName);
            }
        }


        private static void deleteFile(File file) {
    	    if (file.isDirectory()) {
    	        if (file.list().length == 0) {
    	            file.delete();
    	        }
                else {
                    String files[] = file.list();
                    for (String temp : files) {
                        File fileDelete = new File(file, temp);
                        deleteFile(fileDelete);
                    }
                    if (file.list().length == 0) {
                        file.delete();
                    }
    	        }
    	    }
            else {
    	        file.delete();
    	    }
        }


//sahar adding output directory to the boostrap.gz. needed by isoDE
        public static void createTarGZ(String dirPath[], String tarGzPath) throws FileNotFoundException, IOException {
            FileOutputStream fOut = null;
            BufferedOutputStream bOut = null;
            GzipCompressorOutputStream gzOut = null;
            TarArchiveOutputStream tOut = null;
            try{
                fOut = new FileOutputStream(new File(tarGzPath));
                bOut = new BufferedOutputStream(fOut);
                gzOut = new GzipCompressorOutputStream(bOut);
                tOut = new TarArchiveOutputStream(gzOut);
//sahar adding output directory to the boostrap.gz. needed by isoDE
		  for (int k=0; k < dirPath.length; k++) {
	                addFileToTarGz(tOut, dirPath[k], "");
//sahar debug
//                System.err.println("In paths loop");
		  }
            } finally {
                tOut.finish();
                tOut.close();
                gzOut.close();
                bOut.close();
                fOut.close();
            }

        }


        private static void addFileToTarGz(TarArchiveOutputStream tOut, String path, String base) throws IOException {
            File f = new File(path);
            String entryName = base + f.getName();
            TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);
            tOut.putArchiveEntry(tarEntry);

            if (f.isFile()) {
                IOUtils.copy(new FileInputStream(f), tOut);
                tOut.closeArchiveEntry();
            } else {
                tOut.closeArchiveEntry();
                File[] children = f.listFiles();
                if (children != null){
                    for (File child : children) {
                        addFileToTarGz(tOut, child.getAbsolutePath(), entryName + "/");
                    }
                }
            }
        }


	public static <T> void compact(List<T> list) {
		int j = 0;
		int n = list.size();
		for (int i = 0; i < n; ++i) {
			T t = list.get(i);
			if (t != null) {
				list.set(j++, t);
			}
		}
		while (n > j) {
			list.remove(--n);
		}
	}

	public static void addFakePolyAToExons(Isoforms isoforms, int polyAlength) {
		int fakeExonStart = 0;
		for (Isoform isoform : isoforms.isoformIterator()) {
			fakeExonStart = Math.max(fakeExonStart, isoform.getExons().getEnd());
		}
		fakeExonStart += 10;
		int fakeExonEnd = fakeExonStart + polyAlength - 1;
//sahar debug
//                System.err.println("In addFakePolyA");

		for (Isoform isoform : isoforms.isoformIterator()) {
//sahar debug
//		  	if (isoform.toString().equals("NR_003363") || isoform.toString().equals("NM_001177550"))
//	                	System.err.println("In fake polyA tail exons length for "+isoform.toString());
			if (isoform.getStrand() == '+') {
				isoform.addExon(fakeExonStart, fakeExonEnd);
			} else {
				isoform.addExon(-polyAlength, -1);
//			  	if (isoform.toString().equals("NR_003363") || isoform.toString().equals("NM_001177550"))
//		                	System.err.println("In fake polyA after Adding for fake polyA tail exons length of  "+isoform.toString()+" "+isoform.getExons().size());
			}
		}
	}

	public static void takeLog(double[] truth) {
		for (int i = 0; i < truth.length; ++i) {
			if (truth[i] != 0) {
				truth[i] = Math.log(truth[i]);
			}
		}
	}
        public static int clippedBases(String cigar) {
            	int i = 0;
		int n = cigar.length();
                int clipped= 0;
		do {
			int k = i;
			while (i < n && Character.isDigit(cigar.charAt(i))) {
				++i;
			}
			if (i == n) {
				throw new IllegalArgumentException(
						"Malformed CIGAR string: " + cigar);
			}
			int elemLen = Utils.parseInt(cigar, 10, k, i);

			char cigarSymbol = cigar.charAt(i);
                        if (cigarSymbol == 'S')
                            clipped += elemLen;
        		++i;
		} while (i < n);
         return clipped;

        }
        public static String clipSequence(String cigar, String sequence) {
            	int i = 0;
		int n = cigar.length();
                int clipped= 0;
                int firstClip = 0;
                int lastClip = 0;
                boolean firstElement = true;

		do {
			int k = i;
			while (i < n && Character.isDigit(cigar.charAt(i))) {
				++i;
			}
			if (i == n) {
				throw new IllegalArgumentException(
						"Malformed CIGAR string: " + cigar);
			}
			int elemLen = Utils.parseInt(cigar, 10, k, i);

			char cigarSymbol = cigar.charAt(i);
                        if (cigarSymbol == 'S') {
                            if (firstElement)
                                firstClip = elemLen;
                            else
                                lastClip = elemLen;
                        }
                        firstElement = false;
        		++i;
		} while (i < n);
         return sequence.substring(firstClip,sequence.length()-lastClip);

        }
}
