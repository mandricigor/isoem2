package edu.uconn.engr.dna.isoem;

import java.io.IOException;
import java.io.Writer;

public class IsoformCoverageOptionParser extends IsoEmOptionParser {

	public static String OP_FREQ_FILE = "f";
	public static final String OP_DESCRIPTION = "description";

	public IsoformCoverageOptionParser() {
		accepts(OP_FREQ_FILE, "Isoform frequencies file").withRequiredArg().describedAs("frequency file");
		accepts(OP_DESCRIPTION, "Description to be included in track name").withRequiredArg().describedAs("string");
	}

	@Override
	public void printHelpOn(Writer sink) throws IOException {
		sink.write("Usage: isoviz [global options]* [library options]* <aligned_reads.sam>\n");
		sink.write("Mandatory global options:                                                       \n"
				+ "------------------------                                                       \n"
				+ "-G, --GTF <GTF file>                    Known genes and isoforms in GTF format \n"
				+ "-f <frequency file>                     Isoform FPKMs computed by IsoEM        \n");

		sink.write("Mandatory library options:                                                    \n"
				+ "-------------------------                                                      \n"
				+ "-m, --fragment-mean <Double>            Fragment length mean                   \n"
				+ "-d, --fragment-std-dev <Double>         Fragment length standard deviation     \n" //                   + "-a, --auto-fragment-distrib             Automatically detect fragment length   \n"
				//                   + "                                          distribution from uniquely mapping   \n"
				//                   + "                                          paired reads (DOES NOT WORK FOR      \n"
				//                   + "                                          SINGLE READS)                        \n"
				);

		sink.write("Optional global options:                                                        \n"
				+ "-----------------------                                                        \n"
				+ "-c, --gene-clusters <Cluster file>      Override isoform to gene mapping       \n"
				+ "                                          defined in the GTF file with a       \n"
				+ "                                          mapping taken from the given file.   \n"
				+ "                                          The format of each line in the file  \n"
				+ "                                          is \"isoform\tgene\"                 \n"
				+ "-g <genome fasta file>                  Genome reference sequence (needed by   \n"
				+ "                                          some library options)                \n"
				+ "-b                                      Perform hexamer bias correction        \n"
				+ "-h, --help                              Show help                              \n"
				+ "--description <string>                  Description included in track name     \n");

		sink.write("Optional library options:                                                       \n"
				+ "------------------------                                                       \n"
				+ "-s, --directional                       Dataset obtained by directed RNA-Seq   \n"
				+ "                                          (the strand of each read is          \n"
				+ "                                          deterministically chosen: for single \n"
				+ "                                          reads, the read always comes from    \n"
				+ "                                          the coding strand; for paired reads, \n"
				+ "                                          the first read always comes from the \n"
				+ "                                          coding strand, the second from the   \n"
				+ "                                          opposite strand)                     \n"
				+ "--antisense                             Directional sequencing but the reads   \n"
				+ "                                          come from the antisense              \n"
				+ "--mate-pairs                            Paired reads come from the same strand \n"
				+ "                                          (as opposed to the default behavior  \n"
				+ "                                          where the two reads in a pair are    \n"
				+ "                                          assumed to come from opposite        \n"
				+ "                                          strands)                             \n"
				+ "--max-mismatches <Integer>              Maximum number of mismatched allowed   \n"
				+ "                                          for a read. This requires the genome \n"
				+ "                                          sequence to be specified (see -g).   \n"
				+ "-q, --quality-scores                    Weigh the reads based on their quality \n"
				+ "                                          scores. This requires the genome     \n"
				+ "                                          sequence to be specified (see -g).   \n");
		sink.flush();
	}
}
