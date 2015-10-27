package edu.uconn.engr.dna.format;

import java.util.ArrayList;
import java.util.Arrays;
import edu.uconn.engr.dna.io.GTFParser;
import edu.uconn.engr.dna.io.GenesAndIsoformsParser;
import edu.uconn.engr.dna.util.Pair;

import java.io.FileInputStream;
import java.io.PrintWriter;

import static java.lang.System.*;

public class GenerateKnownGeneSequences {

    public static void main(String[] args) throws Exception {
        if (args.length != 4 && args.length != 5) {
            out.println("Arguments: knownGenesGTF genomeFastaFile outputFile lineLength [polyALength]");
            out.println("knownGenesGTF - file containing the coordinates of the known isoforms in GTF format");
            out.println("genomeFastaFile - a fasta file containing a list of \">chromosome sequence\" pairs");
            out.println("outputFile - the name of the file where to write the isoforms and their sequences");
            out.println("lineLength - the number of bases to be placed on each line in the output file\n"
						+ "\t use -1 to  print the whole isoform sequence on one line (no splitting)");
			out.println("polyALength - (optional) add a polyA tail of this length to each isoform sequence (default: 0)");
            return;
        }
        int basesPerLine = Integer.parseInt(args[3]);
		int polyALength = args.length == 5? Integer.parseInt(args[4]) : 0;
		if (polyALength < 0) {
			out.println("Invalid polyALength " + polyALength + "!");
			return;
		}
		char[] polyAArray = new char[polyALength];
		Arrays.fill(polyAArray, 'A');
		String polyA = new String(polyAArray);

        System.out.println("Parsing GTF file...");
        GenesAndIsoformsParser giParser = new GTFParser();
        Pair<Clusters, Isoforms> p = giParser.parse(new FileInputStream(args[0]));
        Clusters clusters = p.getFirst();
        Isoforms isoforms = p.getSecond();
        System.out.println("Found " + isoforms.size() + " isoforms ");

        System.out.println("reading genome from " + args[1] + "...");
        TaggedSequences ts = new MemoryMapChromosomeSequences(args[1]);
//        TaggedSequences ts = new ChromosomeSequences(args[1]);

        ts = new IsoformSequencesFromGenome(ts, isoforms);
//        System.out.println("Isoforms found " + ts.getAllTags().size());

        String outputFileName = args[2];
        System.out.println("writing isoform sequences to " + outputFileName + "...");
        PrintWriter pw = new PrintWriter(outputFileName);
                ArrayList<Integer> isoformsIndex = randomizeIsoforms(isoforms.size());
                for (int k = 0; k < isoformsIndex.size(); k++) {
//		for (Isoform isoform : isoforms.isoformIterator()) {
                  int index = isoformsIndex.get(k);
                  Isoform isoform = isoforms.getValueForIndex(index);
            CharSequence isoSequence = ts.getSequence(isoform.getName());
            if (isoSequence == null) {
                continue;
            }
			if (!polyA.isEmpty()) {
				isoSequence = isoSequence + polyA;
			}
            pw.write('>');
            pw.write(isoform.getName());
            pw.write('\n');

            if (basesPerLine == -1) {
                pw.write(isoSequence.toString());
                pw.write('\n');
            } else {
                for (int i = 0; i < isoSequence.length(); i += basesPerLine) {
                    pw.write(isoSequence.subSequence(i, Math.min(i + basesPerLine, isoSequence.length())).toString());
                    pw.write('\n');
                }
            }
        }
        pw.close();


//        TaggedSequences ts2 = new IsoformSequencesFromFile(outputFileName, isoforms);
//        for (CharSequence tag : ts.getAllTags()) {
//            String s = ts.getSequence(tag).toString();
//            String s2 = ts2.getSequence(tag).toString();
//            if (!s.equals(s2)) {
//                System.out.println("ERROR for " + tag);
//                System.out.println("FROM GENOME: " + s);
//                System.out.println("FROM ISFILE: " + s2);
//                String sr = Utils.reverseComplement(s);
//                System.out.println("REV COMPL 1: " + sr);
//                if (sr.equals(s2)) {
//                    System.out.println("REV COMPL EQUALS GENOME SEQ!");
//                }
//                throw new IllegalStateException("UPS!");
//            }
//        }
        System.out.println("done.");
    }
private static ArrayList<Integer> randomizeIsoforms(int numberOfIsoforms) {
    ArrayList<Integer> index = new ArrayList<Integer>();

    for (int i = 0; i < numberOfIsoforms; i++)
        index.add((Integer)i);
    for (int i = 0; i < numberOfIsoforms/2; i++){
        double j = Math.random()*numberOfIsoforms;
        double k = Math.random()*numberOfIsoforms;
        Integer temp = index.get((int)j);
        index.set((int)j, index.get((int)k));
        index.set((int)k, temp);
    }
    return index;
}

}