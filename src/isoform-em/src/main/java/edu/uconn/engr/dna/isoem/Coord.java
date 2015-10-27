package edu.uconn.engr.dna.isoem;

@Deprecated
public class Coord implements Signature {

	private final int pos;
	private final Coord previous;

	public static Coord newNamedInstanceWithStoredLastGap(int pos, Object name,
					Coord previousCoord, CoordType coordType) {
		return new NamedCoordWithStoredLastGap(pos, name, previousCoord, coordType);
	}

	public static Coord newNamedInstance(int pos, Object name,
					Coord previousCoord, CoordType coordType) {
		return new NamedCoord(pos, name, previousCoord, coordType);
	}

	public static Coord newUnnamedInstance(int pos, Coord previousCoord,
					CoordType coordType) {
		return new Coord(pos, previousCoord, coordType);
	}
	private final CoordType coordType;

	protected Coord(int pos, Coord previousCoord, CoordType coordType) {
		this.coordType = coordType;
		this.pos = pos;
		this.previous = previousCoord;
	}

	public boolean isIntervalStart() {
		return coordType == CoordType.START;
	}

	public boolean isIntervalEnd() {
		return coordType == CoordType.END;
	}

	public int getPos() {
		return pos;
	}

	@Override
	public int getLastCoord() {
		return getPos();
	}

	@Override
	public Gap getLastGap() {
		if (previous == null) {
			int p = getPos();
			return new Gap(p, p);
		} else {
			return new Gap(previous.getPos(), getPos());
		}
	}

	@Override
	public int getCoordBeforeLastGap() {
		if (previous == null) {
			return getPos();
		}
		if (previous.previous == null) {
			return previous.getPos();
		}
		return previous.previous.getPos();
	}

	@Override
	public Signature getPreviousSignature()
					throws UnsupportedOperationException {
		return previous;
	}

	@Override
	public boolean hasGaps() {
		return previous != null && previous.previous != null;
	}

	@Override
	public boolean hasExactlyOneGap() {
		return previous != null && previous.previous != null
						&& previous.previous.previous == null;
	}

	public Object getObjectId() {
		throw new IllegalStateException("This is not happening");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Coord other = (Coord) obj;
		if (this.pos != other.pos) {
			return false;
		}
		if (this.coordType != other.coordType) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		return hash;
	}

}

class NamedCoord extends Coord {

	private final Object name;

	public NamedCoord(int pos, Object name, Coord previousCoord,
					CoordType coordType) {
		super(pos, previousCoord, coordType);
		this.name = name;
	}

	@Override
	public Object getObjectId() {
		return name;
	}
}

class NamedCoordWithStoredLastGap extends NamedCoord {

	private Gap lastGap;

	public NamedCoordWithStoredLastGap(int pos, Object name, Coord previousCoord,
					CoordType coordType) {
		super(pos, name, previousCoord, coordType);
		this.lastGap = super.getLastGap();
	}

	@Override
	public Gap getLastGap() {
		return lastGap;
	}
}
