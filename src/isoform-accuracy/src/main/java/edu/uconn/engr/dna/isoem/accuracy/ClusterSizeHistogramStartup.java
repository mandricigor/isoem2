package edu.uconn.engr.dna.isoem.accuracy;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;

import edu.uconn.engr.dna.format.Cluster;
import edu.uconn.engr.dna.format.Clusters;
import edu.uconn.engr.dna.io.DefaultClustersParser;
import edu.uconn.engr.dna.io.DefaultTwoFieldParser;
import edu.uconn.engr.dna.io.Parser;

public class ClusterSizeHistogramStartup {

	public static void main(String[] args) throws Exception {
		String clusterFile = args[0];
		String outputFileName;
		if (args.length > 1) {
			outputFileName = args[1];
		} else {
			outputFileName = clusterFile + ".histogram.png";
		}
		
		Clusters clusters;

		if (args.length <= 2) { 
			// EM clusters
			Parser<Clusters> cp = new DefaultClustersParser();
			clusters = cp.parse(new FileInputStream(clusterFile));
		} else { 
			// original UCSC clusters
			clusters = new Clusters();
			DefaultTwoFieldParser.getInvertedTwoFieldParser(clusters)
				.parse(new FileInputStream(clusterFile));
		}

		
		double[] data = new double[clusters.size()];
		int i = 0;
		PrintWriter pw = new PrintWriter(clusterFile + ".clusterSizes");
		for (Cluster c : clusters.groupIterator()) {
			data[i++] = c.size();
			pw.write(""+c.size());
			pw.write("\n");
		}
		pw.close();
		
		HistogramDataset dataset = new HistogramDataset();
		dataset.addSeries("Clusters", data, 20);
		
		JFreeChart chart = ChartFactory.createHistogram("Cluster size histogram", 
				"Cluster size", "Number of clusters", 
				dataset, PlotOrientation.VERTICAL, true, false, false);
		LogarithmicAxis yaxis = new LogarithmicAxis("Number of clusters");
		yaxis.setStrictValuesFlag(false);
		yaxis.setAllowNegativesFlag(false);

		((XYPlot)chart.getPlot()).setRangeAxis(yaxis);
		
		File f = new File(outputFileName);
		f.getParentFile().mkdirs();
		DataUtils.saveToFile(outputFileName, chart, 800, 800);
	}
}
