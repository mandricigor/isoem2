package edu.uconn.engr.dna.isoem.accuracy;

public class ErrorFractionWithPercentThresholdNorm {

	public static void main(final String[] args) throws Exception {
		boolean b = ErrorFractionWithPercentThreshold.globalNormalize;
		ErrorFractionWithPercentThreshold.globalNormalize = true;
		ErrorFractionWithPercentThreshold.main(args);
		ErrorFractionWithPercentThreshold.globalNormalize = b;
	}
}
