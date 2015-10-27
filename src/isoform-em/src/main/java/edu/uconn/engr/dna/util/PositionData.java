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

import edu.uconn.engr.dna.format.Isoforms;
import edu.uconn.engr.dna.isoem.ArrayIsoformList;
import edu.uconn.engr.dna.isoem.IsoformList;
import java.util.Arrays;

/**
 *
 * @author marius
 */
public class PositionData {

	private static int[] maxPowerOfTwoBelow = new int[]{0, 1};
	private static int precomputedPowers = 2;
	private static final Double one = 1.0;
	private String[] isoforms;
	private Intervals[] isoformExons;
	private int[] startIndex;

	public PositionData(Isoforms allIsos, String[] isoforms, int[] startIndex) {
		this.isoformExons = new Intervals[isoforms.length];
		this.isoforms = isoforms;
		for (int i = 0; i < isoforms.length; ++i) {
			this.isoformExons[i] = allIsos.get(isoforms[i]).getExons();
		}
		this.startIndex = startIndex;

		if (isoforms.length >= precomputedPowers) {
			synchronized (PositionData.class) {
				while (isoforms.length >= precomputedPowers) {
					doublePrecomputedPowers();
				}
			}
		}

	}

	public IsoformList findMatches(int[] readCoord) {
		int lo = 0;
		int hi = isoforms.length;
		int level = 0;
                boolean afterDeletion = false;
		// ignore first coord of the read (assumed to be covered)
		// leave last coordinate for later processing
		int end = readCoord.length - 1;
                int prev_key = -100;
		for (int i = 1; i < end; ++i, ++level) {
			int key = readCoord[i];
                        if (afterDeletion) {
                            level--;
                            afterDeletion = false;
//                            System.out.println("turning afterDeletion off");
                        }
//                        System.out.printf("i %d i mod 2 %d key %d prev_key %d\n",i,i%2,key,prev_key);
                        if ((i%2 == 0) && (key - prev_key) < 10) {//deletion: short and  index of gap end coordinate is odd
                                level--; //stay in the same level
                                afterDeletion = true; //raise flag to stay in the same level for one more iteration
//                                System.out.println("turning afterDeletion on");
                        }
			lo = findLeftMostGreaterOrEqual(level, key, lo, hi);
//                        System.out.println("readCoord[i] level leftMostGreaterOrEqual "+key+" "+level+" "+lo);
                        if ((lo == hi)) {// && ((key - prev_key) > 10)) {
			//if (lo == hi || isoformExons[lo].getCoord(level + startIndex[lo]) != key) {
                                //if (isoformExons[lo].getCoord(level + startIndex[lo]) != key) {
                                //    System.out.println("return null because isoformExons[lo].getCoord(level + startIndex[lo]) != key");
                                //    System.out.println("startIndex[lo] = "+startIndex[lo]);
                                //    System.out.println("isoformExons[lo].getCoord(level + startIndex[lo]) = "+isoformExons[lo].getCoord(level + startIndex[lo]));
                                //    }
                                if (lo == hi)
//                                    System.out.println("return null because lo == hi");
				return null;

			}
			hi = findRightMostLessOrEqual(level, key, lo, hi) + 1;
//                        System.out.println("readCoord[i] level rightMostLessOrEqual "+key+" "+level+" "+hi);
                        prev_key = key;
                        //if (lo == hi) // above condition did not return because it a deletion
                        //    level--;
		}

		// handle last coordinate: greater or equal coordinates are sufficient
//               System.out.println("afterDeletion ="+afterDeletion);
                if (afterDeletion)
                    level--;
		lo = findLeftMostGreaterOrEqual(level, readCoord[end], lo, hi);
//                System.out.println("readCoord[i] level leftMostgreterOrEqual of last coord"+readCoord[end]+" "+level+" "+lo);
		if ((lo == hi)){// && ((readCoord[end] - prev_key) > 10)) {
//                    System.out.println("return null because lo == hi");
                    return null;
		}

		String[] isos = sort(isoforms, lo, hi);
		return new ArrayIsoformList(isos,
						Utils.arrayOf(hi - lo, 1.0));
	}

	private String[] sort(String[] isoforms, int lo, int hi) {
//		int l = hi - lo;
//		if (l < 7) { // insert sort
//			Integer[] a = new Integer[l];
//			for (int i = lo; i < hi; ++i) {
//				Integer k = isoforms[i];
//				int j = i - lo;
//				while (j > 0 && k.compareTo(a[j - 1]) < 0) {
//					a[j] = a[--j];
//				}
//				a[j] = k;
//			}
//			return a;
//		} else {
			String[] a = Arrays.copyOfRange(isoforms, lo, hi);
			Arrays.sort(a);
			return a;
//		}
	}

	/**
	 * Returns the leftmost index in the interval [lo, hi) which has a value on
	 * the given level greater or equal to key. Returns hi if all the values in
	 * the interval are strictly less than the key
	 * @param level
	 * @param key
	 * @param lo
	 * @param hi
	 * @return
	 */
	private int findLeftMostGreaterOrEqual(int level, int key, int lo, int hi) {
		int k = hi;
//                System.out.printf("findLeftMostGreaterOrEqual\n");
//                System.out.printf("input level %d key %d lo %d hi %d\n",level,key,lo,hi);
		for (int step = maxPowerOfTwoBelow[hi - lo]; step > 0; step >>= 1) {
			int nk = k - step;
//                        System.out.printf("k %d step %d nk %d\n",k,step,nk);
			if (nk >= lo) {
				int index = level + startIndex[nk];
//                                System.out.printf("index %d startIndex[nk] %d isoformExons[nk].getNCoords() %d\n",index,startIndex[nk],isoformExons[nk].getNCoords());
				if (index < isoformExons[nk].getNCoords()) {
					int nv = isoformExons[nk].getCoord(index);
					if (nv >= key) {
						k = nk;
					}
//                                        System.out.printf("isoformExons[nk].getCoord(index)) %d\n",isoformExons[nk].getCoord(index));
				}
			}
		}
//                System.out.printf("k %d\n",k);
		return k;
	}

	private int findRightMostLessOrEqual(int level, int key, int lo, int hi) {
		int k = lo;
//                System.out.printf("findRightMostLessOrEqual\n");
		for (int step = maxPowerOfTwoBelow[hi - lo]; step > 0; step >>= 1) {
			int nk = k + step;
//                        System.out.printf("k %d step %d nk %d\n",k,step,nk);
			if (nk < hi) {
				int index = level + startIndex[nk];
//                                System.out.printf("index %d startIndex[nk] %d isoformExons[nk].getNCoords() %d\n",index,startIndex[nk],isoformExons[nk].getNCoords());
				if (index < isoformExons[nk].getNCoords()) {
					int nv = isoformExons[nk].getCoord(index);
					if (nv <= key) {
						k = nk;
					}
//                                        System.out.printf("isoformExons[nk].getCoord(index)) %d\n",isoformExons[nk].getCoord(index));

				}
			}
		}
//                System.out.printf("k %d\n",k);
		return k;
	}

	private static void doublePrecomputedPowers() {
		int n = precomputedPowers << 1;
		maxPowerOfTwoBelow = Arrays.copyOf(maxPowerOfTwoBelow, n);
		int p = maxPowerOfTwoBelow[precomputedPowers - 1] * 2;
		for (int i = precomputedPowers; i < n; ++i) {
			maxPowerOfTwoBelow[i] = p;
		}
		precomputedPowers = n;
	}
}
