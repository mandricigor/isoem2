package edu.uconn.engr.dna.isoem.accuracy;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.encoders.KeypointPNGEncoderAdapter;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import edu.uconn.engr.dna.format.Cluster;
import edu.uconn.engr.dna.format.Clusters;
import edu.uconn.engr.dna.io.DefaultTwoFieldParser;

public class DotPlotColorCodeStartup
{

	private static final long serialVersionUID = 1L;

	/**
	 * args[0] first freqs file
	 * args[1] second freqs file
	 * args[2] image filename
	 * args[3] clusters file
	 */
	public static void main(final String[] args) throws Exception {
		System.out.println("reading clusters... ");
		Clusters clusters = new Clusters();
		DefaultTwoFieldParser.getInvertedTwoFieldParser(clusters)
			.parse(new FileInputStream(args[3]));
		System.out.println("read " + clusters.size() + " clusters");
		
		Map<String, Cluster> isoformToClusterMapping = getIsoformToClusterMapping(clusters);
		
		final DotPlotColorCodeStartup dp = new DotPlotColorCodeStartup();
		final int binSize = 10;
		Map<Integer, Double[][]> data = DataUtils.loadFreqsByClusterSize(isoformToClusterMapping,
				binSize,
				args[0], args[1]);
		DataUtils.trimLessThan(0.000000001, data);
		final Map<Integer, Double[][]> trimmedData = data; 

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
					dp.createAndShowFrame(trimmedData, binSize, args[0], args[1], args[2]);
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
		});
		
	}

	private static Map<String, Cluster> getIsoformToClusterMapping(
			Clusters clusters) {
		Map<String, Cluster> isoformToClusterMapping = new HashMap<String, Cluster>();
		for (Cluster c : clusters.groupIterator()) {
			for (String i : c.groupIterator()) {
				isoformToClusterMapping.put(i, c);
				
			}
		}
		return isoformToClusterMapping;
	}

	protected void createAndShowFrame(Map<Integer, Double[][]> data, int binSize, String xlabel, String ylabel, String outputFileName) throws FileNotFoundException, IOException {
		initFrame();		
		///
        DefaultXYDataset dataset = new DefaultXYDataset();
        XYDotRenderer renderer = new XYDotRenderer();
//        int series = 0;
        for (Map.Entry<Integer, Double[][]> entry : data.entrySet()) {
        	String range = entry.getKey()*binSize+"-"+((entry.getKey()+1)*binSize-1);
        	dataset.addSeries(range, DataUtils.unWrap(entry.getValue()));
//        	renderer.setSeriesPaint(series, new Color(Math.min(255, 20*++series), 155-20*series, 155-20*series));
        }
        JFreeChart chart = ChartFactory.createScatterPlot(
            null,
            xlabel, 
            ylabel, 
            dataset, 
            PlotOrientation.VERTICAL,
            true, 
            true, 
            false
        );
        XYPlot plot = chart.getXYPlot();
        plot.setRenderer(renderer);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setRangeAxis(createLogAxis(plot.getRangeAxis().getLabel()));
        plot.setDomainAxis(createLogAxis(plot.getDomainAxis().getLabel()));
        
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setAutoRangeIncludesZero(false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
//        setContentPane(chartPanel);

        KeypointPNGEncoderAdapter encoder = new KeypointPNGEncoderAdapter();
        encoder.setEncodingAlpha(true);
        BufferedImage image = chart.createBufferedImage(1024, 1024, 
        		BufferedImage.BITMASK, null);
        FileOutputStream os;
        encoder.encode(image, os = new FileOutputStream(outputFileName));
        os.close();
		////
//		setVisible(true);
	}

	private ValueAxis createLogAxis(String label) {
		LogarithmicAxis axis = new LogarithmicAxis(label);
		axis.setStrictValuesFlag(false);
		return axis;
	}

	private void initFrame() {
//		setTitle("DotPlot");
//		setSize(800, 600);
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
