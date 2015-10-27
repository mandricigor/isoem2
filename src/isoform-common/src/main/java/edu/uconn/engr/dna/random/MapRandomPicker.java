package edu.uconn.engr.dna.random;

import java.util.Map;

/**
 * User: marius
 * Date: Jul 28, 2010
 */
public class MapRandomPicker<K, T, V> implements RandomPicker<T, V> {

    private RandomPicker<K, V> idRandomPicker;
    private Map<K, T> keyToObjectMapping;

    public MapRandomPicker(RandomPicker<K, V> idRandomPicker,
                           Map<K, T> keyToObjectMapping) {
        this.keyToObjectMapping = keyToObjectMapping;
        this.idRandomPicker = idRandomPicker;
    }

    @Override
    public T pick(V group) {
        K itemId = idRandomPicker.pick(group);
        return keyToObjectMapping.get(itemId);
    }
}
