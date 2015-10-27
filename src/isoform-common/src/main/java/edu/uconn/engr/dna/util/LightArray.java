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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author marius
 */
public class LightArray<T> implements List<T> {

	private int size;
	private T[] data;

	public LightArray() {
		this(16);
	}

	public LightArray(int initialCapacity) {
		data = (T[]) new Object[initialCapacity];
	}



	@Override
	public boolean add(T elem) {
		if (size == data.length) {
			data = Arrays.copyOf(data, data.length << 1);
		}
		data[size++] = elem;
		return true;
	}

	@Override
	public T[] toArray() {
		return (T[]) Arrays.copyOf(data, size);
	}

	@Override
	public <T> T[] toArray(T[] a) {
		if (a.length < size) // Make a new array of a's runtime type, but my contents:
		{
			return (T[]) Arrays.copyOf(data, size, a.getClass());
		}
		System.arraycopy(data, 0, a, 0, size);
		if (a.length > size) {
			a[size] = null;
		}
		return a;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public T get(int i) {
		return data[i];
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public T set(int index, T element) {
		T oldValue = (T) data[index];
		data[index] = element;
		return oldValue;
	}

	@Override
	public T remove(int index) {
		T oldValue = (T) data[index];
		int numMoved = size - index - 1;
		if (numMoved > 0) {
			System.arraycopy(data, index + 1, data, index,
							numMoved);
		}
		data[--size] = null; // Let gc do its work
		return oldValue;
	}

	@Override
	public void clear() {
		// Let gc do its work
		for (int i = 0; i < size; i++) {
			data[i] = null;
		}
		size = 0;
	}

	@Override
	public boolean contains(Object o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Iterator<T> iterator() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int indexOf(Object o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int lastIndexOf(Object o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ListIterator<T> listIterator() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
