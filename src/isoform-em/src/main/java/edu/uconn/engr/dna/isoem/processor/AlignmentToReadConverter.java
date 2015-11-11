package edu.uconn.engr.dna.isoem.processor;

import edu.uconn.engr.dna.isoem.ArrayIsoformList;
import edu.uconn.engr.dna.isoem.IsoformList;
import edu.uconn.engr.dna.isoem.IsoformListsBean;
import edu.uconn.engr.dna.util.ParameterRunnable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jun 22, 2010
 * Time: 3:39:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlignmentToReadConverter implements ParameterRunnable<IsoformListsBean, Void> {

	private ParameterRunnable<List<IsoformList>, ?> processor;

	public static int totalReads = 0;
	private static final Object lock = new Object();
        private BitSet debugReadStarts;

	public AlignmentToReadConverter(ParameterRunnable<List<IsoformList>, ?> processor) {
		this.processor = processor;
	}

	@Override
	public void run(IsoformListsBean item) {
		IsoformList[] isosForAlignments = item.getIsoformsForAlignments();
//        System.out.println("in AlignmentToReadConverter   isos length ="+isosForAlignments.length);
		BitSet readStarts = item.getReadStarts();
                debugReadStarts = item.getReadStarts();
		List<IsoformList> result = new ArrayList<IsoformList>();
		IsoformList currentList = null;
		int counter = 0;
		double totalMultiplicity = 0;
		for (int i = 0; i < isosForAlignments.length; ++i) {
			IsoformList al = isosForAlignments[i];
//                        System.out.println("i = "+i);
			if (readStarts.get(i)) {
//                                System.out.println("in Bitset true");
				if (currentList != null && !currentList.isEmpty() && counter != 0) {
					double m = totalMultiplicity / counter;
					currentList.setMultiplicity(totalMultiplicity / counter);
					result.add(currentList);
//                                        System.out.println("in current list not empty");
//                                        System.out.println("totalMultiplicity/counter = "+totalMultiplicity+"/"+counter+" = "+totalMultiplicity/counter);
				}
				currentList = null;
				counter = 0;
				totalMultiplicity = 0;

				if (al == null || al.isEmpty()) {
//                    currentList = null;  // drop current read if one alignment doesn't match any isoform
					currentList = new ArrayIsoformList(new String[0], new double[0]);
//                                        System.out.println("in isoform list empty");
				} else {
					currentList = al;
					++counter;
					totalMultiplicity += al.getMultiplicity();
//                                        System.out.println("in isoform list not empty");
//                                        System.out.println("list multiplicity = "+al.getMultiplicity()+"totalMultiplicity = "+totalMultiplicity+" counter = "+counter);
				}
			} else {
//                                System.out.println("in Bitset false");
				if (al == null) {
//                   currentList = null;
				} else {
					if (currentList != null && !al.isEmpty()) {
						currentList.reunite(al);
						++counter;
						totalMultiplicity += al.getMultiplicity();
//                                                System.out.println("in isoform list not empty");
//                                                System.out.println("list multiplicity = "+al.getMultiplicity()+"totalMultiplicity = "+totalMultiplicity+" counter = "+counter);
					}
				}
			}
		}

		if (currentList != null && !currentList.isEmpty()) {
			result.add(currentList);
			currentList.setMultiplicity(totalMultiplicity / counter);
//                        System.out.println("in trailing current list not empty");
//                        System.out.println("totalMultiplicity/counter = "+totalMultiplicity+"/"+counter+" = "+totalMultiplicity/counter);
		}


                //for (int i = 0; i < result.size(); i ++) {
                //    ArrayIsoformList aiso = (ArrayIsoformList) result.get(i);
                //    if (aiso.readNames.get(0).equals("4WO8U:2237:550")) {
                //        System.out.println("GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG");
                //        System.out.println(result.get(i));
                //        System.out.println("GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG");
                //    }
                //}


		synchronized (lock) {
			totalReads += result.size(); //item.getReadStarts().cardinality();
		}
		if (processor != null) {
			processor.run(result);
		}
	}

	@Override
	public Void done() {
		if (processor != null) {
			processor.done();

		}
//                System.out.println("BitSet in AlignmentToReadConverter " + debugReadStarts.toString());
//                System.out.println("BitSet length in AlignmentToReadConverter " + debugReadStarts.length());
//                System.out.println("BitSet string length in AlignmentToReadConverter " + debugReadStarts.toString().length());
//		System.out.println("Reads in AlignmentToReadConverter " + totalReads);
//                System.out.print("");
		return null;
	}
}
