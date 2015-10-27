package edu.uconn.engr.dna.isoem.accuracy;

import java.io.FileNotFoundException;
import java.io.IOException;

public class WithinFivePercentStartup {

	private static final double EPS = 0.00002;

	public static void main(final String[] args) throws FileNotFoundException, IOException {
		double[][] data = DataUtils.loadFreqsFromFiles(args);
		double truth[] = data[0];
//		System.out.println("Max isoform level expression " + DataUtils.max(truth));
		double p = 0.05;
		for (int i = 1; i < args.length; ++i) {
			double[] estimate = data[i];
			int withinp = 0;
			int notwithin=0;
			double sumt=0;
			double sume=0;
			for (int j = 0; j < estimate.length; ++j) {
				if (Math.abs(truth[j]-estimate[j]) <= p * truth[j]
				|| (truth[j]<EPS && estimate[j]<EPS)
				) {
					++withinp;
				} else {
					sumt+=truth[j];
					sume+=estimate[j];
					notwithin++;
				}
			}
//			System.out.println("Number of isoforms not within5p " + notwithin);
//			System.out.println("Truth average for not within5p: " + (sumt/notwithin));
//			System.out.println("Estimate average for not within5p: " + (sume/notwithin));
			System.out.print(withinp*100.0/estimate.length + "\t");
		}
		System.out.println();
	}
	
}
