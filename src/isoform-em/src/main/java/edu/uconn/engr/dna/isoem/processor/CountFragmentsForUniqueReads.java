package edu.uconn.engr.dna.isoem.processor;

import edu.uconn.engr.dna.isoem.IsoformList;
import edu.uconn.engr.dna.isoem.IsoformListsBean;
import edu.uconn.engr.dna.util.ParameterRunnable;
import edu.uconn.engr.dna.util.UniformBinaryOperator;
import edu.uconn.engr.dna.util.Utils;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jun 22, 2010
 * Time: 2:02:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class CountFragmentsForUniqueReads implements ParameterRunnable<IsoformListsBean, Void> {

    private static final Logger log = Logger.getLogger(CountFragmentsForUniqueReads.class);
    private static final Integer NonUniqueReadFlag = -1;
    private Map<Integer, Integer> fragmentCounts;
    private List<IsoformListsBean> isosForAlignments;

    private Map<Integer, Integer> outputProbMap;
    private List<IsoformListsBean> outputIsoformsForAlignmentsList;

    public CountFragmentsForUniqueReads(Map<Integer, Integer> outputProbMap,
                List<IsoformListsBean> outputIsoformsForAlignmentsList) {
        this.outputProbMap = outputProbMap;
        this.outputIsoformsForAlignmentsList = outputIsoformsForAlignmentsList;
        this.fragmentCounts = new HashMap<Integer, Integer>();
        this.isosForAlignments = new ArrayList<IsoformListsBean>();
    }

    @Override
    public void run(IsoformListsBean item) {
        IsoformList[] isos = item.getIsoformsForAlignments();
        int[] fragmentLengths = item.getFragmentLengths();
        BitSet readStarts = item.getReadStarts();
        Object isoformForCurrentRead = null;
        Integer fragmentForCurrentRead = 0;
//        System.out.println("in run CountFragmentForUniqueReads isos length ="+isos.length);
//        System.out.println("in run CountFragmentForUniqueReads isos length = "+fragmentLengths.length);
        for (int i = 0; i < isos.length; ++i) {
            if (readStarts.get(i)) {
                // see if the previous read was unique
                if (fragmentForCurrentRead != NonUniqueReadFlag && isoformForCurrentRead != null)
                    increaseCount(fragmentForCurrentRead);

                isoformForCurrentRead = null;
                fragmentForCurrentRead = 0;
            }

            if (fragmentForCurrentRead == NonUniqueReadFlag
                    || isos[i] == null
                    || isos[i].isEmpty())
                continue;

            if (isos[i].size() > 1) {
                // read is not unique - mark it so that we ignore next alignments
                fragmentForCurrentRead = NonUniqueReadFlag;
                continue;
            }

            IsoformList.Entry e = isos[i].entrySet().iterator().next();
            if (isoformForCurrentRead == null) {
                isoformForCurrentRead = e.getKey();
                fragmentForCurrentRead = fragmentLengths[i];
            } else if (!(isoformForCurrentRead.equals(e.getKey())))
                fragmentForCurrentRead = NonUniqueReadFlag;
        }

        if (fragmentForCurrentRead != NonUniqueReadFlag
                && fragmentForCurrentRead > 0
                && isoformForCurrentRead != null)
            increaseCount(fragmentForCurrentRead);

        isosForAlignments.add(item);
    }

    private void increaseCount(Integer fragmentForCurrentRead) {
        Integer count = fragmentCounts.get(fragmentForCurrentRead);
        if (count == null)
            count = 1;
        else
            ++count;
        fragmentCounts.put(fragmentForCurrentRead, count);
    }

    @Override
    public Void done() {
        synchronized (outputProbMap) {
            Utils.mergeMaps(outputProbMap, fragmentCounts,
                    UniformBinaryOperator.IntegerSum);
        }
        synchronized (outputIsoformsForAlignmentsList) {
            outputIsoformsForAlignmentsList.addAll(isosForAlignments);
        }
        return null;
    }
}

