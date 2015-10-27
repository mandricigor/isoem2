package edu.uconn.engr.dna.isoem.accuracy;

import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math.stat.regression.SimpleRegression;

public class RSquareByIntervals extends AbstractAccurracyMeasurement {

	static boolean globalNormalize = false;

    public static void main(final String[] args) throws Exception {
	if (args.length < 3) {
	    System.out
		    .println("Arguments: isGeneLevelEstimates [clustersFile] numberOfIntervals "
			    + "startInterval0 endInterval0 endInterval1 ... endIntervalk "
			    + "trueFrequencyFile [frequencyFile1 frequencyFile2 ...]");
	    System.exit(-1);
	}

	RSquareByIntervals e = new RSquareByIntervals();
	e.normalize = globalNormalize;
	e.parseArguments(args);
	e.processData(e.loadData());
    }

    @Override
    protected String computeMeasure(double[] truth, double[] estimate) {
	return "" + new RSquare().rsquare(truth, estimate);
    }
}

class RSquare extends PearsonsCorrelation {
    public double rsquare(final double[] xArray, final double[] yArray)
	    throws IllegalArgumentException {
	SimpleRegression regression = new SimpleRegression();
	if (xArray.length == yArray.length && xArray.length > 1) {
	    for (int i = 0; i < xArray.length; i++) {
		regression.addData(xArray[i], yArray[i]);
	    }
	    return regression.getRSquare();
	} else {
	    throw MathRuntimeException
		    .createIllegalArgumentException(
			    "invalid array dimensions. xArray has size {0}; yArray has {1} elements",
			    xArray.length, yArray.length);
	}
    }

}