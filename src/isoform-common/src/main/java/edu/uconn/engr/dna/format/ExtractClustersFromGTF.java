package edu.uconn.engr.dna.format;

import edu.uconn.engr.dna.io.GTFParser;
import edu.uconn.engr.dna.io.GenesAndIsoformsParser;
import edu.uconn.engr.dna.util.Pair;

import java.io.FileInputStream;
import java.io.PrintWriter;


public class ExtractClustersFromGTF {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Arguments: knownGenesGTF outputFile");
            System.out.println("knownGenesGTF - file containing the coordinates of the known isoforms in GTF format");
            System.out.println("outputFile - the name of the file where to write the genes to isoforms mapping");
            return;
        }

        System.out.println("Parsing GTF file...");
        GenesAndIsoformsParser giParser = new GTFParser();
        Pair<Clusters, Isoforms> p = giParser.parse(new FileInputStream(args[0]));
        Clusters clusters = p.getFirst();
        Isoforms isoforms = p.getSecond();
        System.out.println("Found " + isoforms.size() + " isoforms and " + clusters.size() + " genes");

        System.out.println("writing genes to isoforms mapping to " + args[1] + "...");
        PrintWriter pw = new PrintWriter(args[1]);
        for (Cluster cluster : clusters.groupIterator())
            for (String isoform : cluster.idIterator()) {
                pw.write(cluster.getName());
                pw.write('\t');
                pw.write(isoform);
                pw.write('\n');
            }

        pw.close();
        System.out.println("done.");
    }

}