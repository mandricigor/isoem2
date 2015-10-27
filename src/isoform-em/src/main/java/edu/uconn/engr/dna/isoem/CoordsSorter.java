package edu.uconn.engr.dna.isoem;

import edu.uconn.engr.dna.sort.ItemValueGetter;
import edu.uconn.engr.dna.sort.RadixSorter;
import edu.uconn.engr.dna.sort.Sorter;

import java.util.List;

public class CoordsSorter implements Sorter<Coord> {

    private final RadixSorter<Coord> sorter;

    public CoordsSorter() {
        this.sorter = new RadixSorter<Coord>(1 << 16, 2,
                new ItemValueGetter<Coord>() {
                    @Override
                    public int getNumber(Coord item) {
                        return item.getPos();
                    }
                });
    }

    @Override
    public List<Coord> sort(List<Coord> collection) {
        return sorter.sort(collection);
    }

}
