package edu.uconn.engr.dna.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class DefaultSortedMultiMap<K, V> extends DefaultMultiMap<K, V>
        implements SortedMultiMap<K, V> {

    public DefaultSortedMultiMap() {
    }

    public Collection<V> getAllValuesForKeysSmallerOrEqualTo(K key) {
        Collection<V> col = null;
        for (Map.Entry<K, Collection<V>> entry : map.entrySet()) {
            if (((Comparable) entry.getKey()).compareTo(key) > 0) {
                break;
            }
            if (col == null)
                col = new ArrayList<V>();
            col.addAll(entry.getValue());
        }
        return col;
    }

    @Override
    protected Map<K, Collection<V>> createEmptyMap() {
        return new TreeMap<K, Collection<V>>();
    }
}
