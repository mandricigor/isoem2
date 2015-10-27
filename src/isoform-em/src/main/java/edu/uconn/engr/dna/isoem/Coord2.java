package edu.uconn.engr.dna.isoem;

public class Coord2 {

	private final Object id;
	private final int[] coords;

	public Coord2(Object id, int[] coords) {
		this.id = id;
		this.coords = coords;
	}

	public int[] getCoords() {
		return coords;
	}

	public Object getId() {
		return id;
	}

	public int getStart() {
		return coords[0];
	}
	
	public int getEnd() {
		return coords[coords.length-1];
	}
}
