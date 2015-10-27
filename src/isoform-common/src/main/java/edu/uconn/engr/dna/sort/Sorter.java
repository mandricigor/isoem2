package edu.uconn.engr.dna.sort;

import java.util.List;

public interface Sorter<T> {
	List<T> sort(List<T> collection);
}
