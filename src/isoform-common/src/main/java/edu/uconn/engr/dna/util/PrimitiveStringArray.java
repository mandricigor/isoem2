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

/**
 *
 * @author marius
 */
public class PrimitiveStringArray {

	private int size;
	private String[] data;

	public void add(String elem) {
		ensureCapacity(size + 1);
		data[size++] = elem;
	}

	public String[] toArray() {
		if (data == null) {
			return new String[0];
		}
		String[] output = new String[size];
		System.arraycopy(data, 0, output, 0, size);
		return output;
	}

	public int size() {
		return size;
	}

	public String get(int i) {
		return data[i];
	}

	void addAll(PrimitiveStringArray other) {
		ensureCapacity(size + other.size);
		System.arraycopy(other.data, 0, data, size, other.size);
		size += other.size;
	}

	private void ensureCapacity(int c) {
		if (data == null) {
			int k = 8;
			while (k < c) {
				k <<= 1;
			}
			data = new String[k];
		} else if (data.length < c) {
			int k = data.length;
			while (k < c) {
				k <<= 1;
			}
			String[] n = new String[k];
			System.arraycopy(data, 0, n, 0, size);
			data = n;
		}
	}

	public PrimitiveStringArray plus(PrimitiveStringArray other) {
		PrimitiveStringArray c = new PrimitiveStringArray();
		c.data = new String[this.size + other.size];
		c.addAll(this);
		c.addAll(other);
		return c;
	}
}
