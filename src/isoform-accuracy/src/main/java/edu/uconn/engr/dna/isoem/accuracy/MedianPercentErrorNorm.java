package edu.uconn.engr.dna.isoem.accuracy;

public class MedianPercentErrorNorm {

	public static void main(final String[] args) throws Exception {
		boolean b = MedianPercentError.globalNormalize;
		MedianPercentError.globalNormalize = true;
		MedianPercentError.main(args);
		MedianPercentError.globalNormalize = b;
	}
}
