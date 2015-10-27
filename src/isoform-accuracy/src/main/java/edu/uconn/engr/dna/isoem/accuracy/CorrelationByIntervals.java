package edu.uconn.engr.dna.isoem.accuracy;

import org.apache.commons.math.stat.correlation.PearsonsCorrelation;

public class CorrelationByIntervals extends AbstractAccurracyMeasurement {

    public static void main(final String[] args) throws Exception {
	if (args.length < 3) {
	    System.out
		    .println("Arguments: isGeneLevelEstimates [clustersFile] numberOfIntervals "
			    + "startInterval0 endInterval0 endInterval1 ... endIntervalk "
			    + "trueFrequencyFile [frequencyFile1 frequencyFile2 ...]");
	    System.exit(-1);
	}

	CorrelationByIntervals e = new CorrelationByIntervals();
	e.parseArguments(args);
	e.processData(e.loadData());
    }

    @Override
    protected String computeMeasure(double[] truth, double[] estimate) {
	return "" + new PearsonsCorrelation().correlation(truth, estimate);
    }
}
