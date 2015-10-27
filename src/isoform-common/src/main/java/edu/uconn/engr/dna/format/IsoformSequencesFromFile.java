package edu.uconn.engr.dna.format;

import edu.uconn.engr.dna.util.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class IsoformSequencesFromFile extends AbstractTaggedSequences {

	private String commentMarker = "#";
	private boolean plusStrand;
	private Isoforms isoforms;
	private boolean reverseComplementMinusStrandIsoforms;

	public IsoformSequencesFromFile(String referenceFilename, Isoforms isoforms,
					boolean reverseComplementMinusStrandIsoforms) throws IOException {
		this.isoforms = isoforms;
		this.reverseComplementMinusStrandIsoforms = reverseComplementMinusStrandIsoforms;
		loadFile(new FileReader(referenceFilename));
	}

	public void loadFile(Reader reader) throws IOException {
		BufferedReader br = new BufferedReader(reader);
		String line;
		String lastSeenIsoform = "";
		while (null != (line = br.readLine())) {
			line = line.trim();
			if (line.startsWith(commentMarker)) {
				continue;
			}
			if (line.startsWith(">")) {
				lastSeenIsoform = new String(line.substring(1));
				if (reverseComplementMinusStrandIsoforms) {
					Isoform isoform = isoforms.getValue(lastSeenIsoform);
					if (isoform != null) {
						plusStrand = isoform.getStrand() == '+';
					}
				}
			} else {
				line = process(line);
				if (null != sequencesByTag.put(lastSeenIsoform, line)) {
					System.out.println("WARNING: the sequence named " + lastSeenIsoform
									+ " was found before; will be replaced with the latest!");
				}
			}
		}
		reader.close();
	}

	private String process(String line) {
		line = line.toUpperCase();
		if (!plusStrand && reverseComplementMinusStrandIsoforms) {
			line = Utils.reverseComplement(line);
		}
		return line;
	}
}
