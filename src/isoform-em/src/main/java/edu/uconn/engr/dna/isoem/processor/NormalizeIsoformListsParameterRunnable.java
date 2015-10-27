package edu.uconn.engr.dna.isoem.processor;

import edu.uconn.engr.dna.isoem.IsoformList;
import edu.uconn.engr.dna.util.ParameterRunnable;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jun 19, 2010
 * Time: 9:26:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class NormalizeIsoformListsParameterRunnable implements ParameterRunnable<List<IsoformList>, Void> {
    private ParameterRunnable<List<IsoformList>, ?> processor;

//	private static Object lock = new Object();
//	private static int totalReads;

    public NormalizeIsoformListsParameterRunnable(ParameterRunnable<List<IsoformList>, ?> processor) {
        this.processor = processor;
    }

    @Override
    public void run(List<IsoformList> isoformLists) {
//		synchronized (lock) {
//			totalReads += isoformLists.size();
//		}

//debug
//        System.out.println("in NormalizeIsoformListsParameterRunnable  isos length =");

        for (IsoformList il : isoformLists)
            if (il.size() == 1)
                il.entrySet().iterator().next().setValue(1.0);
            else {
                double sum = 0;
                for (IsoformList.Entry e : il.entrySet())
                    sum += e.getValue();
                for (IsoformList.Entry e : il.entrySet())
                    e.setValue(e.getValue() / sum);
            }
        if (processor != null) {
            processor.run(isoformLists);
		}
    }

    @Override
    public Void done() {
        if (processor != null)
            processor.done();
//		System.out.println("Total reads in NormalizeIsoformLists " + totalReads);
        return null;
    }
}
