package edu.uconn.engr.dna.probability;

import edu.uconn.engr.dna.common.RandomAccessMap;

/**
 * A default class for picking items from groups.
 * To identify the objects we need two parameters:
 * <ul>
 * <li> a mapping between the String id's of the items and the items themselves 
 * <li> the group from which to pick - which is a collection of ids, a subset of 
 * the items in the previous mapping
 * </ul>
 * 
 * If the probability distribution comes from a file, then the
 * file should contain a weight for each possible items that may appear in any group.
 * When an item is to be selected from a group, the relative weights of the items
 * in that group are used to influence the random picking process.
 *   
 * @author marius
 *
 * @param <T>
 * @param <V>
 */
public class DefaultRandomPicker<T, V extends RandomAccessMap<String, ?>> 
	implements RandomPicker<T, V> {

	private RandomAccessMap<String, T> keyToObjectMapping;
	private RandomPicker<Integer, V> customWeightPicker;

	public DefaultRandomPicker(RandomPicker<Integer, V> customWeightPicker,
			RandomAccessMap<String, T> keyToObjectMapping) {
		this.keyToObjectMapping = keyToObjectMapping;
		this.customWeightPicker = customWeightPicker;
	}
	
	@Override
	public T pick(V group) {
		Integer itemNr = customWeightPicker.pick(group);
		String itemId = group.get(itemNr);
		return keyToObjectMapping.get(itemId);
	}

}
