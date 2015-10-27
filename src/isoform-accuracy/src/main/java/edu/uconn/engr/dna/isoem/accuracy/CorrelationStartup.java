package edu.uconn.engr.dna.isoem.accuracy;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.math.stat.correlation.PearsonsCorrelation;

public class CorrelationStartup {

	public static void main(final String[] args) throws FileNotFoundException, IOException {
		double[][] data = DataUtils.loadFreqsFromFiles(args);
		for (int i = 1; i < args.length; ++i) {
			System.out.print(new PearsonsCorrelation().correlation(data[0], data[i]) + "\t");
		}
		System.out.println();
	}
}
