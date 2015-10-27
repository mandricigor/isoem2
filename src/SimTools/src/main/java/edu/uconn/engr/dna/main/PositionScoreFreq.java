/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uconn.engr.dna.main;

import edu.uconn.engr.dna.common.RandomAccessMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author sahar
 */
public class PositionScoreFreq <T,R> implements RandomAccessMap <T,R>{
	protected Map<T, R> groupById;
	protected List<T> idList;
	protected Map<T, Integer> indexById;
	private boolean dirty;

	public PositionScoreFreq() {
		groupById = new LinkedHashMap<T, R>();
		indexById = new HashMap<T, Integer>();
		idList = new ArrayList<T>();
	}

	public R remove(T key) {
		R result = groupById.remove(key);
		int index = indexById.remove(key);
		idList.set(index, null);
		dirty = true;
		return result;
	}

	private void sync() {
		int n = idList.size();
		int removed = 0;
		for (int i = 0; i < n; ++i) {
			T item = idList.get(i);
			if (item == null) {
				removed++;
			} else if (removed > 0) {
				int newPos = i - removed;
				idList.set(newPos, item);
				indexById.put(item, newPos);
			}
		}
		while (removed-- > 0) {
			idList.remove(--n);
		}
		dirty = false;
	}

	public int size() {
		if (dirty) {
			sync();
		}
		return idList.size();
	}

	public T get(int number) {
		if (dirty) {
			sync();
		}
		return idList.get(number);
	}

	@Override
	public R get(T key) {
		if (dirty) {
			sync();
		}
		return groupById.get(key);
	}

	public int getIndexOf(T key) {
		if (dirty) {
			sync();
		}
		return indexById.get(key);
	}


	public void put(T key, R value) {
		if (dirty) {
			sync();
		}
		indexById.put(key, idList.size());
		idList.add(key);
		groupById.put(key, value);
	}
	public Iterable<T> idIterator() {
		if (dirty) {
			sync();
		}
		return idList;
	}

	public Iterable<R> groupIterator() {
		if (dirty) {
			sync();
		}
		return groupById.values();
	}

	public Set<Map.Entry<T, R>> entrySet() {
		if (dirty) {
			sync();
		}
		return groupById.entrySet();
	}

}
