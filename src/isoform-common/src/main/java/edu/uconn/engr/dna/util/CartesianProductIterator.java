package edu.uconn.engr.dna.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CartesianProductIterator implements Iterator<List<Object>> {

	private Collection<Object>[] sets;
	private Iterator<Object>[] iterators;
	private List<Object> next;
	private boolean done;
	private boolean nextIsComputed;

	@SuppressWarnings("unchecked")
	public CartesianProductIterator(Collection<Object>... sets) {
		this.sets = sets;
		iterators = new Iterator[sets.length];
		next = new ArrayList<Object>(sets.length);
		for (int i = 0; i < sets.length; ++i) {
			Iterator<Object> it = sets[i].iterator();
			if (it.hasNext()) {
				next.add(it.next());
			} else {
				done = true;
				break;
			}
			iterators[i] = it;
		}
		nextIsComputed = true;
	}

	@Override
	public boolean hasNext() {
		if (done) {
			return false;
		}
		if (!nextIsComputed) {
			next = computeNext();
			nextIsComputed = true;
		}
		return !done;
	}

	private List<Object> computeNext() {
		int i;
		for (i = iterators.length-1; i >= 0; --i) {
			if (iterators[i].hasNext()) {
				next.set(i, iterators[i].next());
				return next;
			} else {
				iterators[i] = sets[i].iterator();
				next.set(i, iterators[i].next());
			}
		}
		done = true;
		return null;
	}

	@Override
	public List<Object> next() {
		if (done) {
			return null;
		}
		if (!nextIsComputed) {
			next = computeNext();
		}
		nextIsComputed = false;
		return next;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
