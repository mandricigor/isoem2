package edu.uconn.engr.dna.io;

import edu.uconn.engr.dna.util.GroupedRandomAccessMap;
import edu.uconn.engr.dna.util.RandomAccessMap;

import java.io.InputStream;

public class DefaultTwoFieldParser<R> extends AbstractParser implements
	Parser<RandomAccessMap<String,R>> {

    private final String lineFormat;
    private final int fieldOneIndex;
    private final int fieldTwoIndex;
    // private static final Logger log =
    // Logger.getLogger(DefaultTwoFieldParser.class);

    private final GroupedRandomAccessMap<String, String, R> output;

    public DefaultTwoFieldParser(String lineFormat, int firstFieldIndex,
	    int secondFieldIndex,
	    GroupedRandomAccessMap<String, String, R> output) {
	this.output = output;
	this.lineFormat = lineFormat;
	this.fieldOneIndex = firstFieldIndex;
	this.fieldTwoIndex = secondFieldIndex;
    }

    /**
     * Creates a new Parser for the format "field1 field2"
     * 
     * @return a new ClustersParser
     */
    public static <R> DefaultTwoFieldParser<R> getRegularTwoFieldParser(
	    GroupedRandomAccessMap<String, String, R> output) {
	return new DefaultTwoFieldParser<R>("clusterId	transcript", 0, 1,
		output);
    }

    /**
     * Creates a new ClustersParser for the format "transcriptId clusterId"
     * 
     * @return a new ClustersParser
     */
    public static <R> DefaultTwoFieldParser<R> getInvertedTwoFieldParser(
	    GroupedRandomAccessMap<String, String, R> output) {
	return new DefaultTwoFieldParser<R>("transcript clusterId", 1, 0,
		output);
    }

    @Override
    public RandomAccessMap<String, R> parse(InputStream clusterInputStream) throws Exception {
	super.read(clusterInputStream);
	return output;
    }

    @Override
    public void processLine(String line) {
	if (line.isEmpty()) {
	    return;
	}
	String[] parts = line.split("\\s");
	String fieldOne = getField(parts, fieldOneIndex);
	String fieldTwo = getField(parts, fieldTwoIndex);
	output.add(fieldOne, fresh(fieldTwo));
    }

    @Override
    protected void handleException(String line, Exception e) throws Exception {
	System.out.println("ErrorLine " + line);
	System.err.println("Invalid file format! Expected: " + lineFormat
		+ "\nIn line: " + line);
	e.printStackTrace();
	throw e;
    }

}
