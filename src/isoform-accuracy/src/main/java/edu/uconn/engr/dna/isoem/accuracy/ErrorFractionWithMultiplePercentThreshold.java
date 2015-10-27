package edu.uconn.engr.dna.isoem.accuracy;

public class ErrorFractionWithMultiplePercentThreshold extends
	ErrorFractionWithPercentThreshold {
    protected double pvalue;
    private int nPoints;

    public ErrorFractionWithMultiplePercentThreshold() {
	super(0);
    }

    public static void main(final String[] args) throws Exception {
	if (args.length < 4) {
	    System.out
		    .println("Arguments: isGeneLevelEstimates [clustersFile] numberOfPoints "
			    + "trueFrequencyFile frequencyFile1 [frequencyFile2 ...]");
	    System.exit(-1);
	}

	ErrorFractionWithMultiplePercentThreshold e = new ErrorFractionWithMultiplePercentThreshold();
	e.parseArguments(args);
	e.processData(e.loadData());
    }

    @Override
    protected void parseArguments(String[] args) {
	geneLevelEstimates = Boolean.parseBoolean(args[0]);
	int argpos = geneLevelEstimates ? 2 : 1;
	nPoints = Integer.parseInt(args[argpos]);
	args[argpos] = "0";
	super.parseArguments(args);
    }

    @Override
    protected String computeMeasure(double[] truth, double[] estimate) {
	double increment = 1.0 / (nPoints - 1);
	double p = 0.0;
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < nPoints; ++i, p += increment) {
	    double d = getPercentage(truth, estimate, p);
	    sb.append(d);
	    sb.append(" ");
	}
	return sb.toString();
    }

}
