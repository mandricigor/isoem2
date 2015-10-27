package edu.uconn.engr.dna.isoem;

import edu.uconn.engr.dna.format.Clusters;
import edu.uconn.engr.dna.format.Isoforms;
import edu.uconn.engr.dna.io.GTFParser;
import edu.uconn.engr.dna.io.GenesAndIsoformsParser;
import edu.uconn.engr.dna.util.ChainConverter;
import edu.uconn.engr.dna.util.Pair;
import edu.uconn.engr.dna.util.StreamConverter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

@Deprecated
// sahar
// method not used and makes calls to methods whose interface will change; deprecate.
// instead -  entry point to converter is ConvertSamFromIsoformToGenomeCoord
public class SingleThreadedConvertSamFromIsoformToGenomeCoord {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
	if (args.length < 1 || args.length > 3) {
	    System.out
		    .println("Usage: isoformsGTF [samInIsoCoords [outputSam]]");
	    System.exit(1);
	}

	GenesAndIsoformsParser giParser = new GTFParser();
	Pair<Clusters, Isoforms> p = giParser
		.parse(new FileInputStream(args[0]));
	Isoforms isoforms = p.getSecond();

	InputStream samInIsoCoord;
	if (args.length > 1) {
	    String samReadsFile = args[1];
	    samInIsoCoord = new FileInputStream(samReadsFile);
	} else {
	    samInIsoCoord = System.in;
	}

	OutputStream samInGenomeCoord;
	if (args.length == 3) {
	    samInGenomeCoord = new FileOutputStream(args[2]);
	} else {
	    samInGenomeCoord = System.out;
	}

	SamIsoformToGenomeConverter isoToGenomeConverter = new SamIsoformToGenomeConverter(
		isoforms, false);
	DuplicatesRemovingConverter duplicatesRemovingConverter = new DuplicatesRemovingConverter();
	StreamConverter converter = new StreamConverter(
		new ChainConverter<String>(isoToGenomeConverter,
			duplicatesRemovingConverter));
	converter.convert(samInIsoCoord, samInGenomeCoord);
    }
}