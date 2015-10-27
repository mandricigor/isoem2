package edu.uconn.engr.dna.isoem.accuracy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uconn.engr.dna.util.CartesianProductIterable;
import edu.uconn.engr.dna.util.Converter;

public class TabularData {
	private static final String commentPrefix = "#";
	private List<List<Object>> data;
	
	public TabularData(List<List<Object>> data) {
		this.data = data;
	}

	public TabularData convert(Converter<List<Object>, List<List<Object>>> converter) {
		List<List<Object>> newData = new ArrayList<List<Object>>();
		for (List<Object> oldRow : data) {
			List<List<Object>> newRows = converter.convert(oldRow);
			if (newRows != null) {
				newData.addAll(newRows);
			}
		}
		return new TabularData(newData);
	}

	public static TabularData parseFile(String file, Converter<String, ?> c) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		List<List<Object>> data = new ArrayList<List<Object>>();
		while (null != (line = reader.readLine())) {
			line = line.trim();
			if (line.startsWith(commentPrefix)) {
				continue;
			}
			if (line.isEmpty()) {
				continue;
			}
			List<Object> row = new ArrayList<Object>();
			for (String cell : line.split("\\s+")) {
				row.add(c.convert(cell));
			}
			data.add(row);
		}
		reader.close();
//		if (!check(data)) {
//			throw new IllegalArgumentException("All rows must have the same number of columns");
//		}
		return new TabularData(data);
	}
	
	public List<List<Object>> getRows() {
		return data;
	}
	
	public void addAll(TabularData other) {
		data.addAll(other.data);
	}

	@SuppressWarnings("unchecked")
	public Iterable<List<Object>> cartesianProductIterator(List<Integer> columns) {
		Collection<Object>[] array = new Collection[columns.size()];
		int i = 0;
		for (Integer column : columns) {
			array[i++] = getDistinctValues(column);
		}
		return new CartesianProductIterable(array);
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<T> getDistinctValues(int column) {
		Set<T> set = new LinkedHashSet<T>();
		for (List<Object> row : data) {
			set.add((T)row.get(column));
		}
		return set;
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue(Map<Integer, Object> rowValues, int querryColumn) {
		for (List<Object> row : data) {
			if (match(row, rowValues)) {
				return (T)row.get(querryColumn);
			}
		}
		return null;
	}

	private boolean match(List<Object> row, Map<Integer, Object> rowValues) {
		for (Map.Entry<Integer, Object> entry : rowValues.entrySet()) {
			if (!row.get(entry.getKey()).equals(entry.getValue())) {
				return false;
			}
		}
		return true;
	}

	public List<List<Object>> getMatchingRows(Map<Integer, Object> rowValues) {
		List<List<Object>> match = new ArrayList<List<Object>>();
		for (List<Object> row : data) {
			if (match(row, rowValues)) {
				match.add(row);
			}
		}
		return match;
	}

	public void print(PrintStream out) {
		for (List<Object> row : data) {
			for (Object o : row) {
				out.print(o);
				out.print(" ");
			}
			out.println();
		}
	}


}
