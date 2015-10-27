package edu.uconn.engr.dna.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;


public class StreamConverter {
		
	private Converter<String, String> converter;

	public StreamConverter(Converter<String, String> lineConverter) {
		this.converter = lineConverter;
	}

	public void convert(InputStream in, OutputStream out) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
    	// skip header
        String line;
        while (null != (line = reader.readLine()) && line.startsWith("@")) {
    	}
        
        do {
        	String newLine = converter.convert(line);
        	if (newLine != null) {
        		writer.write(newLine);
        		writer.write('\n');
        		writer.flush();
        	}
        } while (null != (line = reader.readLine()));
        writer.close();
        reader.close();
	}

}
