package edu.uconn.engr.dna.util;

import java.util.Collection;
import java.util.Vector;

public class EnhancedVector<T> extends Vector<T> {
	
	private static final long serialVersionUID = 1L;

	public EnhancedVector(int initialCapacity, int capacityIncrement) {
		super(initialCapacity, capacityIncrement);
	}
	
	public EnhancedVector(int initialCapacity) {
		super(initialCapacity);
	}
	
	public EnhancedVector(Collection<T> data) {
		super(data);
	}
	
	/**
	 * Makes removeRange public
	 */
	@Override
	public synchronized void removeRange(int fromIndex, int toIndex) {
		super.removeRange(fromIndex, toIndex);
	}

}
