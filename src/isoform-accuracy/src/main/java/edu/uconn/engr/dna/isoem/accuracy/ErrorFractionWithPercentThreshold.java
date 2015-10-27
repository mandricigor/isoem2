package edu.uconn.engr.dna.isoem.accuracy;

import java.util.Arrays;

public class ErrorFractionWithPercentThreshold extends
	AbstractAccurracyMeasurement {
	static boolean globalNormalize = false;
    protected double pvalue;

    public ErrorFractionWithPercentThreshold(double pvalue) {
	this.pvalue = pvalue;
    }

    public static void main(final String[] args) throws Exception {
	if (args.length < 5) {
	    System.out
		    .println("Arguments: pvalue isGeneLevelEstimates [clustersFile] numberOfIntervals "
			    + "startInterval0 endInterval0 endInterval1 ... endIntervalk "
			    + "trueFrequencyFile frequencyFile1 [frequencyFile2 ...]");
	    System.exit(-1);
	}

	ErrorFractionWithPercentThreshold e = new ErrorFractionWithPercentThreshold(
		Double.parseDouble(args[0]));
	e.normalize = globalNormalize;

	e.parseArguments(Arrays.copyOfRange(args, 1, args.length));
	e.processData(e.loadData());
    }

    @Override
    protected String computeMeasure(double[] truth, double[] estimate) {
	double d = getPercentage(truth, estimate, pvalue);
	return "" + d;
    }

    protected double getPercentage(double[] truth, double[] estimate, double p) {
	int outsidep = 0;
	for (int j = 0; j < estimate.length; ++j) {
	    if (truth[j] == 0 && estimate[j] == 0) {
		if (p == 0) {
		    ++outsidep;
		}
	    } else if (Math.abs(truth[j] - estimate[j]) >= p * truth[j]) {
		++outsidep;
	    }
	}
	return (outsidep * 100.0 / estimate.length);
    }
}
