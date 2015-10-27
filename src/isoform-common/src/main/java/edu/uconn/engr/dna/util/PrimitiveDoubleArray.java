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
public class PrimitiveDoubleArray {

	private int size;
	private double[] data = new double[16];

	public void add(double elem) {
		if (size > 1000000) {
			size = size + 1 - 1;
		}
		if (size == data.length) {
			data = Arrays.copyOf(data, data.length << 1);
		}
		data[size++] = elem;
	}

	public double[] toArray() {
		return Arrays.copyOf(data, size);
	}

	public int size() {
		return size;
	}

	public double get(int i) {
		return data[i];
	}
}
