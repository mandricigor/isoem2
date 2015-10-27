package edu.uconn.engr.dna.isoem;

import java.util.List;

import edu.uconn.engr.dna.sort.ItemValueGetter;
import edu.uconn.engr.dna.sort.RadixSorter;
import edu.uconn.engr.dna.sort.Sorter;
import java.util.Collections;
import java.util.Comparator;

public class IsoformCoordSorter implements Sorter<Coord> {

	private final Sorter<Coord> sorter;

	public IsoformCoordSorter() {
		this.sorter = new RadixSorter<Coord>(1 << 16, 2,
						new ItemValueGetter<Coord>() {

							@Override
							public int getNumber(Coord item) {
								// for equal coordinates
								if (item.isIntervalStart()) {
									// isoform interval starts come first
									return item.getPos() * 2;
								} else {
									// isoform interval ends come last
									return item.getPos() * 2 + 1;
								}
							}
						});
	}

	@Override
	public List<Coord> sort(List<Coord> collection) {
		/*Collections.sort(collection, new Comparator<Coord>() {

			@Override
			public int compare(Coord o1, Coord o2) {
				// TEMP: sort by first coord
				o1 = first(o1);
				o2 = first(o2);
				// TEMP
				int d = o1.getPos() - o2.getPos();
				if (d == 0) {
					if (o1.isIntervalStart()) {
						return o2.isIntervalEnd() ? -1 : 0;
					} else {
						return o2.isIntervalEnd() ? 0 : 1;
					}
				} else {
					return d;
				}
			}

			Coord first(Coord item) {
				while (item.getPreviousSignature() != null) {
					item = (Coord) item.getPreviousSignature();
				}
				return item;
			}
		});
		return collection;*/
		return sorter.sort(collection);
	}
}
