package edu.uconn.engr.dna.format;

import edu.uconn.engr.dna.io.GTFParser;
import edu.uconn.engr.dna.io.GenesAndIsoformsParser;
import edu.uconn.engr.dna.util.Pair;

import java.io.FileInputStream;
import java.io.PrintWriter;


public class GenerateKnownGeneSequencesExtended {

	public static void main(String[] args) throws Exception {
		if (args.length != 4) {
			System.out.println("Arguments: knownGenesGTF genomeFastaFile prefix outputFile");
			System.out.println("knownGenesGTF - file containing the coordinates of the known isoforms in GTF format");
			System.out.println("genomeFastaFile - a fasta file containing a list of \">chromosome sequence\" pairs");
            System.out.println("prefix - prefix to be used before isoform names in the output fasta file");
			System.out.println("outputFile - the name of the file where to write the isoforms and their sequences");
			return;
		}

		System.out.println("Parsing GTF file...");
		GenesAndIsoformsParser giParser = new GTFParser();
		Pair<Clusters, Isoforms> p = giParser.parse(new FileInputStream(args[0]));
		Clusters clusters = p.getFirst();
		Isoforms isoforms = p.getSecond();
		System.out.println("Found " + isoforms.size() + " isoforms and " + clusters.size() + " genes" );

		System.out.println("reading genome from " + args[1] + "...");
		TaggedSequences ts = new ChromosomeSequences(args[1]);

		ts = new IsoformSequencesFromGenome(ts, isoforms);
		System.out.println("Isoforms found " + ts.getAllTags().size());

        String prefix = args[2];

		System.out.println("writing isoform sequences to " + args[3] + "...");
		PrintWriter pw = new PrintWriter(args[3]);
		for (Isoform isoform : isoforms.isoformIterator()) {
			CharSequence isoSequence = ts.getSequence(isoform.getName());
			if (isoSequence == null) {
				continue;
			}
			pw.write(String.format(">%s%s range=%s:%d-%d 5'pad=0 3'pad=0 strand=%s repeatMasking=none\n",
                    prefix,
                    isoform.getName(),
                    isoform.getChromosome(),
                    isoform.getExons().getStart(),
                    isoform.getExons().getEnd(),
                    isoform.getStrand()));
            pw.write(isoSequence.toString());
            pw.write('\n');
//            for (int i = 0; i < isoSequence.length(); i += 50) {
//			    pw.write(isoSequence.subSequence(i, Math.min(i+50, isoSequence.length())).toString());
//    			pw.write('\n');
//            }
		}
		pw.close();
		System.out.println("done.");
	}

}