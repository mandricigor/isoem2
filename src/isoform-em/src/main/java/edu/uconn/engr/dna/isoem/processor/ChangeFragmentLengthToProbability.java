package edu.uconn.engr.dna.isoem.processor;

import edu.uconn.engr.dna.isoem.IsoformList;
import edu.uconn.engr.dna.isoem.IsoformListsBean;
import edu.uconn.engr.dna.probability.CumulativeProbabilityDistribution;
import edu.uconn.engr.dna.util.ParameterRunnable;


/**
 * User: marius
 * Date: Jun 22, 2010
 * Time: 3:58:03 PM
 */
public class ChangeFragmentLengthToProbability implements ParameterRunnable<IsoformListsBean, Void> {
    private CumulativeProbabilityDistribution pd;
    private ParameterRunnable<IsoformListsBean, ?> forwardProcess;

    public ChangeFragmentLengthToProbability(CumulativeProbabilityDistribution pd,
               ParameterRunnable<IsoformListsBean, ?> forwardProcess) {
        this.pd = pd;
        this.forwardProcess = forwardProcess;
    }

    @Override
    public void run(IsoformListsBean bean) {
        IsoformList[] item = bean.getIsoformsForAlignments();
//        System.out.println("ChangeFragmentLengthToProbability.run isoform list length ="+item.length);
        for (int i = 0; i < item.length; ++i) {
            IsoformList il = item[i];
            if (il == null)
                continue;
            double qualityScore = il.getQualityScore();
            for (IsoformList.Entry entry : il.entrySet()) {
                int value = (int)entry.getValue();
                double prob;
                if (value < 0)  // single reads
                    prob = pd.cumulativeLowerTail(-value);
                else
                    prob = pd.getWeight(value, 0);
                entry.setValue(prob * qualityScore);
            }
        }
        if (forwardProcess != null)
            forwardProcess.run(bean);
    }

    @Override
    public Void done() {
        if (forwardProcess != null)
            forwardProcess.done();
        return null;
    }
}
