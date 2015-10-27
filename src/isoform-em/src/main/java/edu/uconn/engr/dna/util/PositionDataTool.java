/*
 *  Copyright 2010 marius.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package edu.uconn.engr.dna.util;

import java.util.Collections;
import edu.uconn.engr.dna.isoem.IsoformList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;
import edu.uconn.engr.dna.format.Isoform;
import edu.uconn.engr.dna.format.Isoforms;
import edu.uconn.engr.dna.io.GTFParser;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static java.lang.System.out;

/**
 *
 * @author marius
 */
public class PositionDataTool {

//	static int maxLength = 0;
//	static int totalLength = 0;
//	static int lengthsCounted = 0;
//	static List<Integer> allLengths = new ArrayList<Integer>();
//	static Object lock = new Object();

	public static void main(String[] args) throws Exception {
		String gtf = "../isoform-em/knownGeneGnfAtlas2-polyA250.gtf";
		out.println("Parsing gtf");
		Isoforms isoforms = new GTFParser().parse(new FileInputStream(gtf)).getSecond();
		out.println("Computing data for positions");
		final PositionDataTool t = new PositionDataTool();
		Map<String, Pair<int[], PositionData[]>> r = t.computeDataForChromosomes(2, isoforms);
		Pair<int[], PositionData[]> p = r.get("chr14");
		int[] read = new int[]{90180283, 90180305, 90183048, 90183049};
		int pos = Arrays.binarySearch(p.getFirst(), read[0]);
		if (pos < 0) {
			pos = -pos - 1;
		}
		IsoformList il = p.getSecond()[pos].findMatches(read);
		out.println(il);

	}
	private final Comparator<Pair<Isoform, Integer>> pairComparator;

	public PositionDataTool() {
		pairComparator = new Comparator<Pair<Isoform, Integer>>() {

			public int compare(Pair<Isoform, Integer> o1, Pair<Isoform, Integer> o2) {
				return getPos(o1) - getPos(o2);
			}
		};
	}

	public Map<String, Pair<int[], PositionData[]>> computeDataForChromosomes(int threads, final Isoforms isoforms) {
		Map<String, List<Isoform>> isosPerChromosome = EmUtils.groupIsoformsByChromosome(isoforms);
		final Map<String, Pair<int[], PositionData[]>> dataForChromosome = new HashMap<String, Pair<int[], PositionData[]>>();
		ExecutorService es = Executors.newFixedThreadPool(1);
//		ExecutorService es = Executors.newFixedThreadPool(threads);
		for (final Map.Entry<String, List<Isoform>> p : isosPerChromosome.entrySet()) {
			es.submit(new Runnable() {

				@Override
				public void run() {
					Pair<int[], PositionData[]> positionData = compute(isoforms, p.getValue());
					dataForChromosome.put(p.getKey(), positionData);
					/*
					int nc = positionData.getFirst().length;
					out.println("Coords: " + nc);
					int firstCoord = positionData.getFirst()[0];
					PositionData pd = positionData.getSecond()[0];
					int[] readSig = new int[]{firstCoord, firstCoord + 1};
					int[] isos = pd.findIsoformsMatchingReadCoords(readSig);
					out.print("isos matching ");
					for (int c : readSig) {
					out.print(c + " ");
					}
					out.println(":");
					for (int i : isos) {
					out.print(i);
					}
					out.println();
					 */
				}
			});
		}
		es.shutdown();
		while (!es.isTerminated()) {
			try {
				es.awaitTermination(100, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
		}
//		Collections.sort(allLengths);
//		System.out.println("Max length " + maxLength
//						+ " mean length " + (totalLength * 1.0 / lengthsCounted)
//						+ " median " + allLengths.get((int)(allLengths.size()*0.5))
//						+ " 75% " +  allLengths.get((int)(allLengths.size()*0.75))
//						+ " 90% " +  allLengths.get((int)(allLengths.size()*0.9))
//						+ " 95% " +  allLengths.get((int)(allLengths.size()*0.95))
//						+ " 99% " +  allLengths.get((int)(allLengths.size()*0.99))
//						);

		return dataForChromosome;
	}

	private int getPos(Pair<Isoform, Integer> pair) {
		return pair.getFirst().getExons().getCoord(pair.getSecond());
	}

	public Pair<int[], PositionData[]> compute(final Isoforms allIsoforms, Iterable<Isoform> isoforms) {
		Pair<Isoform, Integer>[] c = coords(isoforms);
		Arrays.sort(c, pairComparator);
		Pair<int[], Pair<Isoform[], int[]>[]> groupedCoords = group(c);
		int[] coordValues = groupedCoords.getFirst();
		Pair<Isoform[], int[]>[] coordData = groupedCoords.getSecond();

		int m = coordValues.length;
		int[] isoformForPosition = new int[m + 1];  // leave space for sentinels
		PositionData[] dataForPosition = new PositionData[m + 1];
		isoformForPosition[m] = Integer.MAX_VALUE;  // sentinel
		dataForPosition[m] = null;

		final Map<String, Integer> activeIsoforms = new HashMap<String, Integer>();
		for (int n = 0; n < coordValues.length; ++n) {
			int pos = coordValues[n];
			Pair<Isoform[], int[]> posCoord = coordData[n];
			Isoform[] isos = posCoord.getFirst();
			int[] exonIndices = posCoord.getSecond();

			for (int i = 0; i < isos.length; ++i) {
				int exonIndex = exonIndices[i];
				if (exonIndex % 2 == 0) {
					Isoform iso = isos[i];
					activeIsoforms.put(iso.getName(), exonIndex + 1);
				}
			}

			String[] sortedIsos = activeIsoforms.keySet().toArray(new String[activeIsoforms.size()]);
			Arrays.sort(sortedIsos, new Comparator<String>() {

				public int compare(String i1, String i2) {
					Intervals e1 = allIsoforms.get(i1).getExons();
					Intervals e2 = allIsoforms.get(i2).getExons();
					int n = e1.getNCoords();
					int m = e2.getNCoords();
					int i = activeIsoforms.get(i1);
					int j = activeIsoforms.get(i2);
					while (i < n && j < m && e1.getCoord(i) == e2.getCoord(j)) {
						i += 1;
						j += 1;
					}
					if (i == n) {
						return j - m;
					} else if (j == m) {
						return 1;
					} else {
						return e1.getCoord(i) - e2.getCoord(j);
					}
				}
			});
			int[] isoExonIndexes = new int[sortedIsos.length];
//			Integer[] isoIndexes = new Integer[sortedIsos.length];
			for (int i = 0; i < isoExonIndexes.length; ++i) {
				isoExonIndexes[i] = activeIsoforms.get(sortedIsos[i]);
//				isoIndexes[i] = allIsoforms.getIndexOf(sortedIsos[i]);
			}
//			synchronized (lock) {
//				if (sortedIsos.length > maxLength) {
//					maxLength = sortedIsos.length;
//				}
//				totalLength += sortedIsos.length;
//				lengthsCounted++;
//				allLengths.add(sortedIsos.length);
//			}

			for (int i = 0; i < isos.length; ++i) {
				int exonIndex = exonIndices[i];
				if (exonIndex % 2 == 1) {
					Isoform iso = isos[i];
					activeIsoforms.remove(iso.getName());
				}
			}

			isoformForPosition[n] = pos;
			dataForPosition[n] = new PositionData(allIsoforms, sortedIsos, isoExonIndexes);
		}
		return new Pair<int[], PositionData[]>(isoformForPosition, dataForPosition);
	}

	private Pair<Isoform, Integer>[] coords(Iterable<Isoform> isoforms) {
		List<Pair<Isoform, Integer>> coords = new ArrayList<Pair<Isoform, Integer>>();
		for (Isoform i : isoforms) {
			Intervals exons = i.getExons();
			for (int j = 0; j < exons.size(); ++j) {
				coords.add(new Pair<Isoform, Integer>(i, 2 * j));
				coords.add(new Pair<Isoform, Integer>(i, 2 * j + 1));
			}
		}
		return coords.toArray(new Pair[coords.size()]);
	}

	private Pair<int[], Pair<Isoform[], int[]>[]> group(Pair<Isoform, Integer>[] s) {

		int nCoords = 1;
		int currentC = getPos(s[0]);
		for (int i = 1; i < s.length; ++i) {
			int c = getPos(s[i]);
			if (c != currentC) {
				++nCoords;
				currentC = c;
			}
		}
		int[] position = new int[nCoords];
		Pair<Isoform[], int[]>[] data = new Pair[nCoords];
		int k = 0;

		int currentCoord = getPos(s[0]);
		int currentStart = 0;

		int c = -1;
		for (int i = 1; i <= s.length; ++i) {
			if (i == s.length || currentCoord != (c = getPos(s[i]))) {
				position[k] = currentCoord;
				int n = i - currentStart;
				Isoform[] isosForPos = new Isoform[n];
				int[] exonIndexForPos = new int[n];
				for (int j = 0; j < n; ++j) {
					Pair<Isoform, Integer> p = s[currentStart + j];
					isosForPos[j] = p.getFirst();
					exonIndexForPos[j] = p.getSecond();
				}
				data[k] = new Pair<Isoform[], int[]>(isosForPos, exonIndexForPos);
				++k;

				currentCoord = c;
				currentStart = i;
			}
		}
		return new Pair<int[], Pair<Isoform[], int[]>[]>(position, data);
	}
}
