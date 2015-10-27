package edu.uconn.engr.dna.isoem.accuracy;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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

public class DotPlotStartup // extends JFrame 
{

	private static final long serialVersionUID = 1L;

	/**
	 * args[0] first freqs file
	 * args[1] second freqs file
	 * args[2] image filename
	 * args[3] clusters file
	 */
	public static void main(final String[] args) throws Exception {
		final DotPlotStartup dp = new DotPlotStartup();
		double[][] data = DataUtils.loadFreqs(args[0], args[1]);
		final double[][] trimmedData = DataUtils.trimLessOrEqualTo(0.000000001, data);
		System.out.println("Original items: " + data[0].length);
		System.out.println("After trim: " + trimmedData[0].length);
		
//		System.out.println("reading clusters... ");
//		Clusters clusters = new Clusters();
//		DefaultTwoFieldParser.getInvertedTwoFieldParser(clusters)
//			.parse(new FileInputStream(args[3]));
//		System.out.println("read " + clusters.size() + " clusters");

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
					dp.createAndShowFrame(trimmedData, args[0], args[1], args[2]);
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
		});
		
	}

	protected void createAndShowFrame(double[][] data, String xlabel, String ylabel, String outputFileName) throws FileNotFoundException, IOException {
		File outdir = new File(outputFileName).getParentFile();
		outdir.mkdirs();
		
		initFrame();		
		///
        DefaultXYDataset dataset = new DefaultXYDataset();
        dataset.addSeries("data", data);
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
        plot.setRenderer(new XYDotRenderer());
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
