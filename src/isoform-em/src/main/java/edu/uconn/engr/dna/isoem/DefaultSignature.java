package edu.uconn.engr.dna.isoem;

import java.util.ArrayList;

public class DefaultSignature extends ArrayList<Integer> implements Signature {

	public static int count;
	
	private static final long serialVersionUID = 1L;
	private final Object objectId;


	public DefaultSignature(Object objectId) {
		super(4);
		this.objectId = objectId;
		count++;
	}

	public Gap getLastGap() {
		int n = getEndPositionOfLastGap();
		return getGap(n);
	}

	public int getCoordBeforeLastGap() {
		return get(Math.max(0, getEndPositionOfLastGap()-2));
	}

	private int getEndPositionOfLastGap() {
		int n = size();
		if (n % 2 == 1) { 
			// size is odd
			return n-1;
		} else {
			// size is even
			return n-2;
		}
	}

	private Gap getGap(int n) {
		return new Gap(
				get(Math.max(0, n-1)), 
				get(Math.max(0, n)));
	}

	@Override
	public int getLastCoord() {
		return get(size()-1);
	}

	public boolean hasGaps() {
		return size() > 2;
	}
	
	@Override
	public boolean hasExactlyOneGap() {
		int n = size();
		return n == 3 || n == 4;
	}

	public final Object getObjectId() {
		return objectId;
	}
	
	@Override
	public Signature getPreviousSignature() {
		return this;
	}
}