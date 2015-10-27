package edu.uconn.engr.dna.isoem.accuracy;

import java.awt.BasicStroke;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.uconn.engr.dna.format.DoubleConverter;
import edu.uconn.engr.dna.format.NullConverter;
import edu.uconn.engr.dna.util.Converter;


public class LinePlotsStartup {

	private enum Column {
		nr,rl,fragLenDistrib,isoDistrib,firstReadOrigin,
		paired,/*alignment,*/algo,correlation,nBases;
	}
	private final Column xaxis = Column.nBases;  
	private final Column yaxis = Column.correlation;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException, InterruptedException, InvocationTargetException {
		if (args.length < 2) {
			System.out.println("Arguments: correlationFile plotsFileDescription [outputDir]");
			System.exit(-1);
		}
		String correlationFile = args[0];
		String plotsFile = args[1];
		final String outputDir;
		if (args.length >=3) {
			outputDir = args[2];
		} else {
			outputDir = ".";
		}
		
		// parse correlation
		final LinePlotsStartup s = new LinePlotsStartup();
		TabularData data = TabularData.parseFile(correlationFile, 
				new ChainConverter<String, Object>(new LongConverter(), 
						new DoubleConverter(), new NullConverter<String>()));
		data = data.convert(s.new ExpandRowsConverter());
//		data.print(System.out);

		final TabularData plots = TabularData.parseFile(plotsFile, 
				new EnumConverter<Column>(Column.class));
		final TabularData fData = data;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					s.generatePlots(fData, plots, outputDir, Arrays.asList(Column.nr.ordinal()
//						,Column.rl.ordinal()
							//,Column.paired.ordinal()
							));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		});

		
	}

	private void generatePlots(TabularData data, TabularData plots, String outputDir, List<Integer> ignoredColumns) throws InterruptedException, InvocationTargetException {
		for (List<Object> p : plots.getRows()) {
			List<Integer> plottedColumns = getColumnList(p);
			String plotType = getPlotType(p);
			List<Integer> nonPlottedColumns = getColumnList(findNonPlottedColumns(p));
			nonPlottedColumns.removeAll(ignoredColumns);
			
			for (List<Object> nonPlotCombination : data.cartesianProductIterator(nonPlottedColumns)) {
				Map<Integer, Object> nonPlottedValues = map(nonPlottedColumns, nonPlotCombination);
				String plotName = createSeriesName(nonPlottedValues);
				XYSeriesCollection  dataset = new XYSeriesCollection();
				for (List<Object> plotCombination : data.cartesianProductIterator(plottedColumns)) {
					Map<Integer, Object> rowValues = map(plottedColumns, plotCombination);
					String seriesName = createSeriesName(rowValues);
					XYSeries series = new XYSeries(seriesName);
					rowValues.putAll(nonPlottedValues);
//					System.out.println("series " + seriesName);
					List<List<Object>> match = data.getMatchingRows(rowValues);
					for (List<Object> row : match) {
						Number x = (Number)getValue(row, xaxis);
						Number y = (Number)getValue(row, yaxis);
						series.add(x, y);
//						System.out.println(x + " " + y);
					}
//					for (Number x : data.<Number>getDistinctValues(xaxis.ordinal())) {
//						rowValues.put(xaxis.ordinal(), x);
//						Number y = data.getValue(rowValues, yaxis.ordinal());
//						if (y != null) {
//							series.add(x, y);
//							System.out.println(x + " " + y);
//						}
//					}
					if (series.getItems().size()>0) {
						dataset.addSeries(series);
					}
				}

				JFreeChart chart = ChartFactory.createXYLineChart(plotName, 
						"NBases", "Correlation", dataset, PlotOrientation.VERTICAL, 
						true, false, false);
				XYPlot plot = (XYPlot)chart.getPlot();
				NumberAxis rangeAxis = ((NumberAxis)plot.getRangeAxis());
				rangeAxis.setAutoRange(true);
				rangeAxis.setAutoRangeIncludesZero(false);
				((XYLineAndShapeRenderer)plot.getRenderer()).setAutoPopulateSeriesStroke(false);
				((XYLineAndShapeRenderer)plot.getRenderer()).setBaseStroke(new BasicStroke(2.54f));
				
//				showWindow(chart);
//				return;
				String fileDir = outputDir + File.separator + plotType + File.separator;
				String fileName = fileDir + plotName+".png";
				try {
					File f = new File(fileDir);
					f.mkdirs();
					DataUtils.saveToFile(fileName, chart, 800, 800);
					System.out.println("done file " + fileName);
					
//					System.exit(0);
				} catch (Exception e) {
					System.err.println("Error while creating chart " + fileName);
					e.printStackTrace();
				}
			}
		}
	}

	private String getPlotType(List<Object> p) {
		StringBuilder sb = new StringBuilder();
		for (Object col : p) {
			if (sb.length() > 0) {
				sb.append(" x ");			
			}
			sb.append(col);
		}
		return sb.toString();
	}

	private String createSeriesName(Map<Integer, Object> rowValues) {
		StringBuilder sb = new StringBuilder();
		for (Column c : Column.values()) {
			Object value = rowValues.get(c.ordinal());
			if (value != null) {
				sb.append(c);
				sb.append('=');
				sb.append(value);
				sb.append(';');
			}
		}
		return sb.toString();
	}

	private Map<Integer, Object> map(List<Integer> plottedColumns,
			List<Object> plotCombination) {
		if (plottedColumns.size() != plotCombination.size()) {
			throw new IllegalStateException("Number of columns different from number of values!");
		}
		Map<Integer, Object> map = new LinkedHashMap<Integer, Object>();
		for (int i = 0; i < plotCombination.size(); ++i) {
			map.put(plottedColumns.get(i), plotCombination.get(i));
		}
		return map;
	}

	private List<Integer> getColumnList(List<?> p) {
		List<Integer> plottedColumns = new ArrayList<Integer>();
		for (Object o : p) {
			plottedColumns.add(((Column)o).ordinal());
		}
		return plottedColumns;
	}

	private List<Column> findNonPlottedColumns(List<Object> plottedColumns) {
		List<Column> nonPlotted = new ArrayList<Column>();
		for (Column c : Column.values()) {
			if (c != xaxis && c != yaxis && !plottedColumns.contains(c)) {
				nonPlotted.add(c);
			}
		}
		return nonPlotted;
	}

	private long getIntValue(List<Object> row, Column column) {
		return (Long)getValue(row, column);
	}
	
	private Object getValue(List<Object> row, Column column) {
		return row.get(column.ordinal());
	}

	class ExpandRowsConverter implements Converter<List<Object>, List<List<Object>>> {

		@SuppressWarnings("unchecked")
		@Override
		public List<List<Object>> convert(List<Object> oldRow)
				throws IllegalStateException {
			List<Object> newRow = new ArrayList<Object>(oldRow);
			// add number of bases column
			newRow.add(getIntValue(newRow, Column.nr) 
					* getIntValue(newRow, Column.rl)
					* getIntValue(newRow, Column.paired));
			return Arrays.asList(newRow);
		}

	}

}

