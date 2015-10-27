package edu.uconn.engr.dna.isoem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import edu.uconn.engr.dna.format.Isoforms;

public class GenomeCoordConverterReader extends BufferedReader {
	
	private SamIsoformToGenomeConverter converter;

	public GenomeCoordConverterReader(Isoforms isoforms, Reader original) {
		super(original);
		this.converter = new SamIsoformToGenomeConverter(isoforms, false);
	}
	
	@Override
	public String readLine() throws IOException {
		String line;
		while (null != (line = super.readLine())) {
			if (line.startsWith("@")) {
				return line;
			}
			line = converter.convert(line);
			if (line != null) {
				break;
			}
		}
		return line;
	}

}
