package edu.uconn.engr.dna.format;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public abstract class AbstractTaggedSequencesFromFile extends AbstractTaggedSequences {

	private String commentMarker = "#";
	
	public void loadFile(Reader reader) throws IOException {
		int tags = 0;
		long bases = 0;
		BufferedReader br = new BufferedReader(reader);
		String line;
		StringBuilder sb = null;
		String[] parts = new String[2];
		while (null != (line = br.readLine())) {
			line = line.trim();
			if (line.startsWith(commentMarker)) {
				continue;
			}
			parts[0] = null;
			parts[1] = null;
			if (splitLine(line, parts)) {
				++tags;
				String tag = parts[0].intern();
				sb = new StringBuilder(parts[1]);
				if (null != sequencesByTag.put(tag, sb)) {
					System.out.println("WARNING: the sequence named " + tag 
							+ " was found before; will be replaced with the latest!");
				}
			} else {
				sb.append(parts[0]);
			}
		}
		reader.close();
		for (Map.Entry<CharSequence, CharSequence> entry : sequencesByTag.entrySet()) {
			// transform StringBuilders to Strings
			entry.setValue(entry.getValue().toString());
			bases += entry.getValue().length();
		}
//		log.debug("read sequences; " + tags + " totaling " + bases + " bases");
	}

	
	/**
	 * This method should be implemented by subclasses to define the pattern
	 * of the lines containing the tags of the sequences. The method returns
	 * true if this line introduces a new sequence, and false if this is just 
	 * another line of a previously introduced sequence. 
	 * 
	 * <p>If the return value is true, you should also do the following:
	 * <ul>
	 * <li>set parts[0] to the name of the tag, extracted from the line
	 * <li>set parts[1] to the rest of the line containing useful data belonging
	 * to the sequence (could be the empty string, but should never be null)
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * if the return value is false, you should also do the following:
	 * <ul>
	 * <li>set parts[0] to the useful information in the line (the line itself or 
	 * some transformation of the line - e.g. reverse complement, uppercase, etc) 
	 * </ul>
	 * </p>
	 *  
	 * 
	 * @param line the line to be parsed
	 * @param parts output array used when the method returns true
	 * @return true if this line introduces a new sequence, false otherwise
	 */
	protected abstract boolean splitLine(String line, String[] parts);

}
