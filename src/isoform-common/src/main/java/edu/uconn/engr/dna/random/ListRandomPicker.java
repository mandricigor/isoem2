package edu.uconn.engr.dna.random;

import java.util.List;

/**
 * User: marius
 * Date: Jul 28, 2010
 */
public class ListRandomPicker<T> implements RandomPicker<T, List<T>> {
    private RandomPicker<Integer, Integer> indexPicker;

    public ListRandomPicker(RandomPicker<Integer, Integer> indexPicker) {
        this.indexPicker = indexPicker;
    }

    @Override
    public T pick(List<T> info) {
        int index = indexPicker.pick(info.size()).intValue();
        if (index < 0)
            return null;
        else
            return info.get(index);
    }
}
