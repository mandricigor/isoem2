package edu.uconn.engr.dna.util;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class DefaultMultiMap<K,V> implements MultiMap<K, V> {
	
	protected Map<K, Collection<V>> map;
	private int size;
	
	public DefaultMultiMap() {
		map = createEmptyMap();
	}
	
	@Override
	public Collection<V> values() {
		return Utils.flatten(map.values());
	}
	
	public Collection<V> get(K key) {
		return map.get(key);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	public V remove(K key, V value) {
		Collection<V> col = map.get(key);
		if (col == null) {
			return null;
		}
		if (col.remove(value)) {
			--size;
			if (col.isEmpty()) {
				map.remove(key);
			}
			return value;
		}
		return null;
	};
	
	public V removeOne(K key) {
		Collection<V> col = map.get(key);
		if (col == null) {
			return null;
		}
		V value = ((Deque<V>)col).removeFirst();
		--size;
		if (col.isEmpty()) {
			map.remove(key);
		}
		return value;
	};
	
	public void put(K key, V value) {
		Collection<V> col = map.get(key);
		if (col == null) {
			col = createCollection();
			map.put(key, col);
		}
		col.add(value);
		++size;
	}

	protected Collection<V> createCollection() {
		return new LinkedList<V>();
	}

	protected Map<K, Collection<V>> createEmptyMap() {
		return new HashMap<K, Collection<V>>();
	}

	@Override
	public void clear() {
		map.clear();
		size = 0;
	}

	@Override
	public int size() {
		return size;
	}
}
