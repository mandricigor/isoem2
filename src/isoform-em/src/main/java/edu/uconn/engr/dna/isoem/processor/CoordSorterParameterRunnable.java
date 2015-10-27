package edu.uconn.engr.dna.isoem.processor;

import edu.uconn.engr.dna.isoem.Coord;
import edu.uconn.engr.dna.isoem.ReadCoordinatesBean;
import edu.uconn.engr.dna.util.ParameterRunnable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jun 19, 2010
 * Time: 7:49:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class CoordSorterParameterRunnable implements ParameterRunnable<ReadCoordinatesBean, Object> {

	private ParameterRunnable<ReadCoordinatesBean, ?> processor;
	private Comparator<Coord> c;

	public CoordSorterParameterRunnable(ParameterRunnable<ReadCoordinatesBean, ?> processor) {
		this.c = new Comparator<Coord>() {

			@Override
			public int compare(Coord o1, Coord o2) {
				return o1.getPos() - o2.getPos();
			}

			Coord first(Coord item) {
				while (item.getPreviousSignature() != null) {
					item = (Coord) item.getPreviousSignature();
				}
				return item;
			}
		};
		this.processor = processor;
	}

	@Override
	public void run(ReadCoordinatesBean item) {
//        System.out.println("in CoordSorterParameterRunnable ");
		for (Map.Entry<String, List<Coord>> entry : item.getCoordinates().entrySet()) {
			Collections.sort(entry.getValue(), c);
		}
		if (processor != null) {
			processor.run(item);
		}
	}

	@Override
	public Object done() {
		if (processor != null) {
			return processor.done();
		}
		return null;
	}
}
