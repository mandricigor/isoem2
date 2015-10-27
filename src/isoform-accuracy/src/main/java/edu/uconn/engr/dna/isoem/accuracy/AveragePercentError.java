package edu.uconn.engr.dna.isoem.accuracy;

public class AveragePercentError extends AbstractAccurracyMeasurement {

	public static void main(final String[] args) throws Exception {
		if (args.length < 3) {
			System.out
					.println("Arguments: isGeneLevelEstimates [clustersFile] numberOfIntervals "
							+ "startInterval0 endInterval0 endInterval1 ... endIntervalk "
							+ "trueFrequencyFile [frequencyFile1 frequencyFile2 ...]");
			System.exit(-1);
		}

		AveragePercentError e = new AveragePercentError();
		e.parseArguments(args);
		e.processData(e.loadData());
	}

	@Override
	protected String computeMeasure(double[] truth, double[] estimate) {
		double sum = 0.0;
		int count = 0;
		for (int i = 0; i < truth.length; ++i) {
			if (truth[i] > 0.00000001) {
				double error = Math.abs(truth[i] - estimate[i]);
				error /= truth[i];
				sum += error;
				count++;
			}
		}
		return "" + (sum / count + 100.0);
	}
}
