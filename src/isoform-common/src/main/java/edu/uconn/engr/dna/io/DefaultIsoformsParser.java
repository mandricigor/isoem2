package edu.uconn.engr.dna.io;

import java.io.InputStream;

import edu.uconn.engr.dna.format.Isoform;
import edu.uconn.engr.dna.format.Isoforms;
import edu.uconn.engr.dna.util.PrimitiveIntArray;


public class DefaultIsoformsParser extends AbstractParser implements IsoformsParser {

	private static final String lineFormat= 
		"name	chrom	strand	txStart	txEnd	cdsStart	cdsEnd	exonCount	exonStarts	exonEnds	proteinID	alignID";
	private static final int nameField = 0;
	private static final int chromField = 1;
	private static final int strandField = 2;
	private static final int exonCountField = 7;
	private static final int exonStartsField = 8;
	private static final int exonEndsField = 9;
	
	private Isoforms isoforms;
	

	public DefaultIsoformsParser() {
		this.isoforms = new Isoforms();
	}
	
	@Override
	public Isoforms parse(InputStream inputStream) throws Exception {
		super.read(inputStream);
		return isoforms;
	}

	
	@Override
	public void processLine(String line) {
		if (line.isEmpty()) {
			return ;
		}
		String[] parts = line.split("\\s");
		String name = getField(parts, nameField).trim().intern();
		String chromosome = getField(parts, chromField);
		chromosome = chromosome.trim().intern();
		String strand = getField(parts, strandField);
		int exonCount = Integer.parseInt(getField(parts, exonCountField));
		String exonStarts = getField(parts, exonStartsField);
		String exonEnds = getField(parts, exonEndsField);

		// zero based starts: add one
		int[] exonStartsInts = add(parseNumbers(exonStarts), 1);
		
		// one based ends: leave unchanged
		int[] exonEndsInts = parseNumbers(exonEnds);
		
		if (exonStartsInts.length != exonCount || exonEndsInts.length != exonCount) {
			throw new IllegalArgumentException("number of exon start/end positions different than expected"
					+ exonStartsInts.length + " != " + exonEndsInts.length); 
		}
		name = fresh(name);
		isoforms.add(name, new Isoform(name, 
							fresh(chromosome), 
							strand.charAt(0),  
							exonStartsInts, 
							exonEndsInts));
	}

	private int[] add(int[] numbers, int offset) {
		for (int i = 0; i < numbers.length; ++i) {
			numbers[i] = numbers[i]+1;
		}
		return numbers;
	}

	private int[] parseNumbers(String exonStarts) {
		String[] parts = exonStarts.split(",");
		PrimitiveIntArray ints = new PrimitiveIntArray();
		for (int i = 0; i < parts.length; ++i) {
			String part = parts[i].trim();
			if (!part.isEmpty()) {
				ints.add(Integer.parseInt(part));
			}
		}
		return ints.toArray();
	}

	protected void handleException(String line, Exception e) {
		System.err.println("Invalid file format! Expected: " + lineFormat);
		e.printStackTrace();
	}
}
