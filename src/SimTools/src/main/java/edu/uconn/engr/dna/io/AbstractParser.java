package edu.uconn.engr.dna.io;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class AbstractParser {

	private String commentMarker = "#";

	public AbstractParser() {
	}

	public void read(InputStream is) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		while (null != (line = br.readLine())) {
			line = line.trim();
			if (line.startsWith(commentMarker )) {
				// ignore comments
				continue;
			}
			try {
				processLine(line);
			} catch (Exception e) {
				handleException(line, e);
			}
		}
		br.close();
	}
	
	protected String getField(String[] parts, int field) throws  IllegalArgumentException {
		if (field >= parts.length) {
			throw new IllegalArgumentException("Insufficient number of fields!");
		}
		return parts[field];
	}
	

	/**
	 * To be implemented by subclasses for processing lines
	 * 
	 * @param line
	 */
	protected abstract void processLine(String line) throws Exception;
	
	/**
	 * To be implemented by subclasses for exception handling
	 * 
	 * @param e
	 */
	protected abstract void handleException(String line, Exception e) throws Exception;
	
	public String getCommentMarker() {
		return commentMarker;
	}
	
	public void setCommentMarker(String commentMarker) {
		this.commentMarker = commentMarker;
	}
	
	// avoid memory leaks caused by String#substring
	protected String fresh(String string) {
		return new String(string);
	}

}
