package edu.uconn.engr.dna.isoem.accuracy;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import edu.uconn.engr.dna.format.Clusters;
import edu.uconn.engr.dna.io.DefaultTwoFieldParser;
import edu.uconn.engr.dna.util.RandomAccessMap;

public class CreateGeneIsoWeights extends AbstractAccurracyMeasurement {
	
	public static void main(String[] args) throws Exception {
//		for (int i = 0; i < args.length; ++i) {
//			System.out.println(args[i]);
//		}
		if (args.length != 3) {
			System.out.println("Arguments: clustersFile iso_weights output_file" );
			System.exit(-1);
		}
		String clusterFile = args[0];
		String freqFile = args[1];
		String outFile = args[2];
		
		Clusters clusters = new Clusters();
		DefaultTwoFieldParser.getInvertedTwoFieldParser(clusters)
			.parse(new FileInputStream(clusterFile));
		
		CreateGeneIsoWeights g = new CreateGeneIsoWeights();
		Map<String, String> isoformToClusterMap = g.createIsoformToClusterMap(clusters);
		List<RandomAccessMap<String, Double>> dataByIsoform = DataUtils.loadFreqMapsFromFiles(freqFile);
		Map<String, Double> dataByCluster = g.groupByCluster(dataByIsoform.get(0), isoformToClusterMap);
		DataUtils.writeFrequencies(dataByCluster, outFile);
	}
	
	@Override
	protected String computeMeasure(double[] itruth, double[] iestimate) {
		return "";
	}

}
