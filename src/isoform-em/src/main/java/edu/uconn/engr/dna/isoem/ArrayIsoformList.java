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
package edu.uconn.engr.dna.isoem;

import java.util.NoSuchElementException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import static java.lang.Double.doubleToLongBits;

/**
 *
 * @author marius
 */
public class ArrayIsoformList implements IsoformList, Iterable<IsoformList.Entry> {

	private double multiplicity;
	private double qualityScore;
	private int size;
	private String[] name;
	private double[] weight;
	private int h;
        //public List<Map<String, Double>> readAlignmentMapList;
        //public List<String> readNames;
        public String readName;
        public int bootstrapId;
        //public Map<String, ArrayList<Double>> weightMap;

	public ArrayIsoformList(String[] name, double[] weight) {
		assert (name.length == weight.length);
		this.name = name;
		this.weight = weight;
		this.size = name.length;
                //readAlignmentMapList = new ArrayList<Map<String, Double>>();
                // put the first readAlignmentMap to the readAlignmentList;
                //Map<String, Double> readAlignmentMap = new HashMap<String, Double>();
                //for (int i = 0; i < name.length; i ++) {
                //    readAlignmentMap.put(name[i], weight[i]);
                //}
                //if (readAlignmentMap.size() > 0) {
                //    readAlignmentMapList.add(readAlignmentMap);
                //}
                //readNames = new ArrayList<String>();
                readName = null;
                bootstrapId = 0;
                //this.weightMap = new HashMap<String, ArrayList<Double>>();
	}


        public ArrayIsoformList(ArrayIsoformList another) {
            this.multiplicity = another.getMultiplicity();
            this.qualityScore = another.getQualityScore();
            this.size = another.size();
            this.name = new String[another.getName().length];
            System.arraycopy(another.getName(), 0, this.name, 0, another.getName().length);
            this.weight = new double[another.getWeight().length];
            System.arraycopy(another.getWeight(), 0, this.weight, 0, another.getWeight().length);
            this.readName = another.readName;
            //this.weightMap = new HashMap<String, ArrayList<Double>>();
            //for (Map.Entry<String, ArrayList<Double>> entry: another.weightMap.entrySet()) {
            //    ArrayList<Double> weights = entry.getValue();
            //    ArrayList<Double> new_weights = new ArrayList<Double>(weights);
            //    this.weightMap.put(entry.getKey(), new_weights);
            //}
            this.h = another.h;
            this.bootstrapId = another.bootstrapId;
        }


	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public void intersect(IsoformList isoformList) {
		ArrayIsoformList a = getArrayIsoformList(isoformList);
		int i = 0, j = 0, k = 0;
		while (i < name.length && j < a.name.length) {
			if (name[i] == null) {
				++i;
			} else if (a.name[j] == null) {
				++j;
			} else {
				int c = name[i].compareTo(a.name[j]);
				if (c == 0) {
					name[k] = name[i];
					weight[k++] = weight[i++] + a.weight[j++];
				} else if (c < 0) {
					++i;
				} else {
					++j;
				}
			}
		}
		size = k;
		fillNullsOrAdjustSize();
		h = 0;
	}

        /*
        public void reuniteMaps(IsoformList isoformList) {
            ArrayIsoformList aiso = (ArrayIsoformList) isoformList;
            for (Map.Entry<String, ArrayList<Double>> entry : aiso.weightMap.entrySet()) {
                ArrayList<Double> weights = weightMap.get(entry.getKey());
                if (weights != null) {
                    for (int i = 0; i < entry.getValue().size(); i ++) {
                        weights.add(entry.getValue().get(i));
                    }
                }
                else {
                    weightMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
        */


	@Override
	public void reunite(IsoformList isoformList) {
		ArrayIsoformList a = getArrayIsoformList(isoformList);
                // Igor merges the two lists
                //readNames.addAll(a.readNames);
                //readAlignmentMapList.addAll(a.readAlignmentMapList);
		int i = 0, j = 0, k = 0;
		String[] rname = name;
		double[] rweight = weight;
		while (i < name.length && j < a.name.length) {
			if (name[i] == null) {
				++i;
			} else if (a.name[j] == null) {
				++j;
			} else {
				int c = name[i].compareTo(a.name[j]);
				if (c == 0) {
					rname[k] = name[i];
					rweight[k++] = weight[i++] + a.weight[j++];
				} else if (c < 0) {
					rname[k] = name[i];
					rweight[k++] = weight[i++];
				} else {
					if (rname == name && k >= i) { // lazy copy
						int rsize = size + a.size;
						rname = Arrays.copyOf(rname, rsize);
						rweight = Arrays.copyOf(rweight, rsize);
					}
					rname[k] = a.name[j];
					rweight[k++] = a.weight[j++];
				}
			}
		}
		while (i < name.length) {
			if (name[i] != null) {
				rname[k] = name[i];
				rweight[k++] = weight[i];
			}
			++i;
		}
		while (j < a.name.length) {
			if (a.name[j] != null) {
				if (k >= rname.length) { // lazy copy
					int rsize = size + a.size;
					rname = Arrays.copyOf(rname, rsize);
					rweight = Arrays.copyOf(rweight, rsize);
				}
				rname[k] = a.name[j];
				rweight[k++] = a.weight[j];
			}
			++j;
		}
		name = rname;
		weight = rweight;
		size = k;
		fillNullsOrAdjustSize();
		h = 0;
                //reuniteMaps(isoformList);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		ArrayIsoformList a = getArrayIsoformList(obj);
		if (size != a.size) {
			return false;
		}
		for (int i = 0, j = 0; i < name.length && j < a.name.length;) {
			if (name[i] == null) {
				++i;
			} else if (a.name[j] == null) {
				++j;
			} else {
				if (!name[i].equals(a.name[j])
								|| doubleToLongBits(weight[i]) != doubleToLongBits(a.weight[j])) {
					return false;
				}
				++i;
				++j;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		if (h == 0) {
			h = 1;
			for (int i = 0; i < name.length; ++i) {
				if (name[i] != null) {
					long bits = doubleToLongBits(weight[i]);
					int doubleHash = (int) (bits ^ (bits >>> 32));
					h = 31 * h + (name[i].hashCode() ^ doubleHash);
				}
			}
		}
		return h;
	}

	@Override
	public Iterable<Entry> entrySet() {
		return this;
	}

	@Override
	public double getMultiplicity() {
		return multiplicity;
	}

	@Override
	public void setMultiplicity(double m) {
		this.multiplicity = m;
	}

	@Override
	public double getQualityScore() {
		return qualityScore;
	}

	@Override
	public void setQualityScore(double d) {
		this.qualityScore = d;
	}

	private ArrayIsoformList getArrayIsoformList(Object o) {
		if (!(o instanceof ArrayIsoformList)) {
			throw new UnsupportedOperationException("Found class "
							+ o.getClass() + " required " + ArrayIsoformList.class);
		}
		return (ArrayIsoformList) o;
	}

	@Override
	public Iterator<Entry> iterator() {
		return new EntryIterator2();
	}

	private void fillNullsOrAdjustSize() {
		if (size < name.length / 2) {
			name = Arrays.copyOf(name, size);
			weight = Arrays.copyOf(weight, size);
		} else {
			int k = size;
			while (k < name.length) {
				name[k++] = null;
			}
		}
	}

	public double[] getWeight() {
		removeNullsIfNecessary();
		return weight;
	}
	public String[] getName() {
		removeNullsIfNecessary();
		return name;
	}

	private void removeNullsIfNecessary() {
		if (size < name.length) {
			int j = 0;
			for (int i = 0; i < name.length; ++i) {
				if (name[i] != null) {
					name[j] = name[i];
					weight[j] = weight[i];
					j++;
				}
			}
			if (j != size) {
				throw new IllegalStateException("Bug! This should never happen");
			}
			name = Arrays.copyOf(name, j);
			weight = Arrays.copyOf(weight, j);
		}
	}

	@Override
	public void setExpectedMultiplicity(double e) {
		// piggyback on quality score field which is not used at this point
		this.qualityScore = e;
	}

	@Override
	public double getExpectedMultiplicity() {
		return qualityScore;
	}

	class IsoformListEntry implements IsoformList.Entry {

		int index;

		public String getKey() {
			return name[index];
		}

		public double getValue() {
			return weight[index];
		}

		public void setValue(double v) {
			weight[index] = v;
			h = 0;
		}
	}

	class EntryIterator2 implements Iterator<IsoformList.Entry> {

		private IsoformListEntry entry = null;
		private int remaining = size;

		@Override
		public boolean hasNext() {
			return remaining > 0;
		}

		@Override
		public IsoformList.Entry next() {
			if (remaining == 0) {
				throw new NoSuchElementException("Iterator is empty!");
			}
			int i;
			if (entry == null) {
				entry = new IsoformListEntry();
				i = 0;
			} else {
				i = entry.index + 1;
			}
			while (i < name.length && name[i] == null) {
				++i;
			}
			entry.index = i;
			--remaining;
			return entry;
		}

		@Override
		public void remove() {
			if (entry == null) {
				throw new IllegalStateException("Have to call next first");
			}
			name[entry.index] = null;
			--size;
			h = 0;
		}
	}

	@Override
	public Iterable<Object> keySet() {
		return new Iterable<Object>() {

			@Override
			public Iterator<Object> iterator() {
				return new Iterator<Object>() {

					private Iterator<Entry> e = entrySet().iterator();

					@Override
					public boolean hasNext() {
						return e.hasNext();
					}

					@Override
					public Object next() {
						return e.next().getKey();
					}

					@Override
					public void remove() {
						e.remove();
					}
				};
			}
		};
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		for (Entry e : entrySet()) {
			sb.append(e.getKey());
			sb.append("=");
			sb.append(e.getValue());
			sb.append("; ");
		}
		sb.append("}");
                sb.append(" " + readName + " " + multiplicity);
		return sb.toString();
	}

}
