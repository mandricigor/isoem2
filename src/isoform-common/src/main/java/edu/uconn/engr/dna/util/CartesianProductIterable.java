package edu.uconn.engr.dna.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CartesianProductIterable implements Iterable<List<Object>> {

	private Collection<Object>[] sets;

	public CartesianProductIterable(Collection<Object>... sets) {
		this.sets = sets;
	}

	@Override
	public Iterator<List<Object>> iterator() {
		return new CartesianProductIterator(sets);
	}

}
