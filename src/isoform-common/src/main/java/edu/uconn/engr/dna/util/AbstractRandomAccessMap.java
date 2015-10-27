package edu.uconn.engr.dna.util;

import java.util.*;

public abstract class AbstractRandomAccessMap<T, V, R>
				implements GroupedRandomAccessMap<T, V, R> {

	protected Map<T, R> groupById;
	protected List<T> idList;
	protected List<R> valueList;
	protected Map<T, Integer> indexById;
	private boolean dirty;

	public AbstractRandomAccessMap() {
		groupById = new LinkedHashMap<T, R>();
		indexById = new HashMap<T, Integer>();
		idList = new ArrayList<T>();
		valueList = new ArrayList<R>();
	}

	public R removeValue(T key) {
		R result = groupById.remove(key);
		int index = indexById.remove(key);
		idList.set(index, null);
		valueList.set(index, null);
		dirty = true;
		return result;
	}

	private void sync() {
		int n = idList.size();
		int removed = 0;
		for (int i = 0; i < n; ++i) {
			T item = idList.get(i);
			R val = valueList.get(i);
			if (item == null) {
				removed++;
			} else if (removed > 0) {
				int newPos = i - removed;
				idList.set(newPos, item);
				valueList.set(newPos, val);
				indexById.put(item, newPos);
			}
		}
		while (removed-- > 0) {
			idList.remove(--n);
			valueList.remove(n);
		}
		dirty = false;
	}

	public int size() {
		return groupById.size();
	}

	public T getKey(int number) {
		if (dirty) {
			sync();
		}
		return idList.get(number);
	}

	public Integer getIndexOf(T key) {
		if (dirty) {
			sync();
		}
		return indexById.get(key);
	}

	@Override
	public R getValue(T key) {
		return groupById.get(key);
	}

	public R getValueForIndex(int index) {
		if (dirty) {
			sync();
		}
		return valueList.get(index);
	}
	
	@Override
	public void add(T key, V value) {
		if (dirty) {
			sync();
		}
		R cluster = groupById.get(key);
		if (cluster == null) {
			cluster = addToExistingGroupOrCreateNew(key, cluster, value);
			indexById.put(key, idList.size());
			idList.add(key);
			valueList.add(cluster);
			groupById.put(key, cluster);
		} else {
			R nc = addToExistingGroupOrCreateNew(key, cluster, value);
			if (nc != cluster) {
				groupById.put(key, nc);
			}
		}
	}

	public R put(T key, R value) {
		if (dirty) {
			sync();
		}
		if (indexById.containsKey(key)) {
			return groupById.put(key, value);
		} else {
			indexById.put(key, idList.size());
			idList.add(key);
			valueList.add(value);
			return groupById.put(key, value);
		}
	}

	/**
	 * Called for each new value to add. Receives the existing group
	 * associated with the current key.
	 * <p/>
	 * If the group is null, a new one should be created and returned. If
	 * the group is not null, the value should be added to the group.
	 * <p/>
	 * This can be used in many creative ways, for example to transform the input
	 * values in a new type - e.g. if value is a String you can convert it
	 * to Double and return it for the group; this is equivalent to Doubles being
	 * groups of only one element. You'll end up with a mapping from keys to Doubles,
	 * even though the added values were Strings.
	 *
	 * @param key
	 * @param cluster
	 * @param value
	 * @return
	 */
	protected abstract R addToExistingGroupOrCreateNew(T key, R cluster, V value);

	public Iterable<T> idIterator() {
		return groupById.keySet();
	}

	public Iterable<R> groupIterator() {
		return groupById.values();
	}

	@Override
	public Set<Map.Entry<T, R>> entrySet() {
		return groupById.entrySet();
	}

	@Override
	public boolean isEmpty() {
		return groupById.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return groupById.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return groupById.containsValue((R) value);
	}

	@Override
	public R get(Object key) {
		return getValue((T) key);
	}

	@Override
	public R remove(Object key) {
		return removeValue((T) key);
	}

	@Override
	public void putAll(Map<? extends T, ? extends R> m) {
		for (Map.Entry<? extends T, ? extends R> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	@Override
	public void clear() {
		groupById.clear();
		idList.clear();
		valueList.clear();
		indexById.clear();
		dirty = false;
	}

	@Override
	public Set<T> keySet() {
		return groupById.keySet();
	}

	@Override
	public Collection<R> values() {
		return groupById.values();
	}
}
