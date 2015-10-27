package edu.uconn.engr.dna.isoem.accuracy;

import java.util.Arrays;

public class MedianPercentError extends AbstractAccurracyMeasurement {

	static boolean globalNormalize = false;

	public static void main(final String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("Arguments: isGeneLevelEstimates [clustersFile] numberOfIntervals "
					+ "startInterval0 endInterval0 endInterval1 ... endIntervalk "
					+ "trueFrequencyFile [frequencyFile1 frequencyFile2 ...]");
			System.exit(-1);
		}

		MedianPercentError e = createInstance();
		e.parseArguments(args);
		e.processData(e.loadData());
	}

	public static MedianPercentError createInstance() {
		MedianPercentError mpe = new MedianPercentError();
		mpe.normalize = globalNormalize;
		return mpe;
	}

	@Override
	protected String computeMeasure(double[] truth, double[] estimate) {
		double[] err = new double[truth.length];
		for (int i = 0; i < truth.length; ++i) {
			double error = Math.abs(truth[i] - estimate[i]);
			if (truth[i] == 0) {
				if (estimate[i] == 0) {
					error = 0;
				} else {
					error = Double.POSITIVE_INFINITY;
				}
			} else {
				error /= truth[i];
			}
			err[i] = error * 100;
//		System.out.println(Truth " + truth[i] + " estimate " + estimate[i] + " error " + err[i]);
		}
		Arrays.sort(err);
		if (err.length == 0) {
			return "0.0";
		} else if (err.length % 2 == 1) {
			return "" + err[err.length / 2];
		} else {
			return "" + ((err[err.length / 2 - 1] + err[err.length / 2]) / 2);
		}
	}
}
