package edu.uconn.engr.dna.format;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.Map;

import edu.uconn.engr.dna.io.DefaultIsoformsParser;
import edu.uconn.engr.dna.io.DefaultTwoFieldParser;
import edu.uconn.engr.dna.io.IsoformsParser;
import edu.uconn.engr.dna.util.Intervals;
import edu.uconn.engr.dna.util.Utils;

public class GenerateGff {

	public static void main(String[] args) throws Exception {
		if (args.length != 4) {
			System.out.println("Arguments: knownGenesFile clustersFile outputFile source");
			System.out.println("knownGenesFile - file containing the coordinates of the known isoforms");
			System.out.println("clustersFile - a file containing a list of \"isoform gene\" pairs");
			System.out.println("outputFile - the name of the file where to write the isoforms and their sequences");
			System.out.println("source - string that will appear as the second field in all lines");
			return;
		}

		System.out.print("reading known isoforms from " + args[0] + "... ");
		IsoformsParser ip = new DefaultIsoformsParser();
		Isoforms isoforms = ip.parse(new FileInputStream(args[0]));
		System.out.println("found " + isoforms.size() + " isoforms");

		System.out.print("reading genes for isoforms from " + args[1] + "... ");
		Clusters clusters = new Clusters();
		DefaultTwoFieldParser.getInvertedTwoFieldParser(clusters)
			.parse(new FileInputStream(args[1]));
		System.out.println("found " + clusters.size() + " clusters");
		
		Map<String, String> isoformsToClustersMap = Utils.createIsoformToClusterMap(isoforms, clusters);

		System.out.print("writing GFF to " + args[2] + "...");
		String source = args[3];
		PrintWriter pw = new PrintWriter(args[2]);
		for (Isoform isoform : isoforms.isoformIterator()) {
			Intervals exons = isoform.getExons();
			for (int i = 0; i < exons.size(); ++i) {
				pw.write(isoform.getChromosome());
				pw.write('\t');
				pw.write(source);
				pw.write('\t');
				pw.write("exon");
				pw.write('\t');
				pw.write(String.valueOf(exons.getStart(i)));
				pw.write('\t');
				pw.write(String.valueOf(exons.getEnd(i)));
				pw.write('\t');
				pw.write("0.000000");
				pw.write('\t');
				pw.write(isoform.getStrand());
				pw.write('\t');
				pw.write('.');
				pw.write('\t');
				pw.write("gene_id \"");
				pw.write(isoformsToClustersMap.get(isoform.getName()));
				pw.write("\"; transcript_id \"");
				pw.write(isoform.getName());
				pw.write("\";\n");
			}
		}
		pw.close();
		System.out.println("done.");
	}
}
