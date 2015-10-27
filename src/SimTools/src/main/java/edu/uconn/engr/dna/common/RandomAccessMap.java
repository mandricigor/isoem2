/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uconn.engr.dna.common;

import java.util.Map;
import java.util.Set;

public interface RandomAccessMap<T, R> {

	int size();

	R get(T key);

	T get(int index);

	R remove(T key);

	void put(T key, R value);

	int getIndexOf(T key);

	Iterable<T> idIterator();

	Iterable<R> groupIterator();

	Set<Map.Entry<T, R>> entrySet();


}
