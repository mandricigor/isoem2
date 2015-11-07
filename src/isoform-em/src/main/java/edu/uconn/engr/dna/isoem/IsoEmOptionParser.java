package edu.uconn.engr.dna.isoem;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.IOException;
import java.io.Writer;

import static java.util.Arrays.asList;

public class IsoEmOptionParser extends OptionParser {

	public static final String OP_HELP = "h";
	public static final String OP_GTF = "G";
	public static final String OP_CLUSTERS_FILE = "c";
	public static final String OP_STRAND = "s";
	public static final String OP_ANTISENSE = "antisense";
	public static final String OP_GENOME = "g";
	public static final String OP_FRAG_MEAN = "m";
	public static final String OP_FRAG_DEV = "d";
	public static final String OP_FRAG_DISTRIB_AUTO = "a";
	public static final String OP_MATE_PAIRS = "mate-pairs";
	public static final String OP_MISMATCHES = "max-mismatches";
	public static final String OP_QUALITY = "q";
	public static final String OP_BIAS = "b";
	public static final String OP_ANNOTATED_REPEATS_FILE = "r";
	public static final String OP_REPEAT_THRESHOLD = "repeat-threshold";
	public static final String OP_UNIQ = "uniq";
	public static final String OP_COUNT = "report-counts";
	public static final String OP_POLYA = "polyA";
  	public static final String OP_LIMIT_NREADS = "limit-nreads";
  	public static final String OP_OUTPUT_DIR = "o";
  	public static final String OP_OUTPUT_FILE_PREFIX = "O";
        public static final String OP_NUMBER_BOOTSTRAPS = "B"; // how many bootstraps to do if any
        public static final String OP_CONFIDENCE_VALUE = "C"; // confidence intervals; percent


	public IsoEmOptionParser() {
		acceptsAll(asList(OP_HELP, "help"), "Show help");
		acceptsAll(asList(OP_GTF, "GTF"),
						"Known genes and isoforms in GTF format").withRequiredArg().describedAs("GTF file");
		acceptsAll(
						asList(OP_CLUSTERS_FILE, "gene-clusters"),
						"Override isoform to gene mapping defined in the "
						+ "GTF file with a mapping taken from the given file. The format "
						+ "of each line in the file is \"isoform\tgene\".").withRequiredArg().describedAs("cluster file");
		acceptsAll(
						asList(OP_STRAND, "directional"),
						"Library obtained by directed RNA-Seq "
						+ "(the strand of each read is deterministically chosen: for single reads, "
						+ "the read always comes from the coding strand; for paired reads, "
						+ "the first read always comes from the coding strand, the second "
						+ "from the opposite strand)");
		accepts(OP_ANTISENSE, "Directional sequencing but the reads come from the antisense.");
		accepts(OP_GENOME, "Genome reference sequence (needed by some library options)").withRequiredArg().describedAs("genome fasta file");
		acceptsAll(asList(OP_FRAG_MEAN, "fragment-mean"),
						"Fragment length mean").withRequiredArg().ofType(Double.class);
		acceptsAll(asList(OP_FRAG_DEV, "fragment-std-dev"),
						"Fragment length standard deviation").withRequiredArg().ofType(
						Double.class);
		acceptsAll(
						asList(OP_FRAG_DISTRIB_AUTO, "auto-fragment-distrib"),
						"Automatically detect fragment length distribution from uniquely "
						+ "mapping paired reads (DOES NOT WORK FOR SINGLE READS)");
		accepts(OP_MATE_PAIRS, "Paired reads come from the same strand (as opposed to the "
						+ "default behavior where the two reads in a pair are assumed to come from "
						+ "opposite strands)");
		accepts(OP_MISMATCHES, "Maximum number of mismatched allowed for a read. "
						+ String.format("This requires the genome sequence to be specified (see -%s).", OP_GENOME)
						+ " Default: no limit.").withRequiredArg().ofType(Integer.class);
		acceptsAll(asList(OP_QUALITY, "quality-scores"), "Weigh the reads based on their quality scores. "
						+ String.format("This requires the genome sequence to be specified (see -%s).", OP_GENOME));
		accepts(OP_BIAS, "Perform hexamer bias correction");
		accepts(OP_ANNOTATED_REPEATS_FILE, "Drop alignments falling withing annotated repeats").withRequiredArg().describedAs("Repeats GTF");
		accepts(OP_NUMBER_BOOTSTRAPS, "Number of bootstrap iterations").withRequiredArg().ofType(Integer.class).describedAs("Number of bootstrap iterations");
		accepts(OP_CONFIDENCE_VALUE, "Confidence value (default 95)").withRequiredArg().ofType(Integer.class).describedAs("Confidence value");

		accepts(OP_REPEAT_THRESHOLD,
						"Drop all reads that have more than this many bases inside annotated repeats. Default: 20.").withRequiredArg().describedAs("nbases");
		accepts(OP_UNIQ, "Infer frequencies from uniquely mapped reads only");
		accepts(OP_COUNT, "Report read counts instead of isoform frequencies");
		accepts(OP_POLYA,
						"Reads have been generated from mRNAs with polyA tails of "
						+ "approximately this number of bases"
						).withRequiredArg().ofType(Integer.class).describedAs("nbases");
  		accepts(OP_LIMIT_NREADS, "Discard all reads after this many have been read"
				).withRequiredArg().ofType(Integer.class).describedAs("nreads");
		accepts(OP_OUTPUT_DIR, "Use this as the name of the output directory").withRequiredArg().describedAs("prefix");
		accepts(OP_OUTPUT_FILE_PREFIX, "Use this as the filename prefix").withRequiredArg().describedAs("prefix");
	}

	@Override
	public void printHelpOn(Writer sink) throws IOException {
		sink.write("Usage: isoem [global options]* [library options]* <aligned_reads.sam>\n");
		sink.write("Mandatory global options:                                                             \n"
						+ "------------------------                                                       \n"
						+ "-G, --GTF <GTF file>                    Known genes and isoforms in GTF format \n");

		sink.write("Mandatory library options: either -a or both -m and -d must be present:               \n"
						+ "-------------------------                                                      \n"
						+ "-m, --fragment-mean <Double>            Fragment length mean                   \n"
						+ "-d, --fragment-std-dev <Double>         Fragment length standard deviation     \n"
						+ "-a, --auto-fragment-distrib             Automatically detect fragment length   \n"
						+ "                                          distribution from uniquely mapping   \n"
						+ "                                          paired reads (DOES NOT WORK FOR      \n"
						+ "                                          SINGLE READS)                        \n");

		sink.write("Optional global options:                                                              \n"
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
						+ "-r <Repeats GTF>                        Drop alignments falling within         \n"
						+ "                                          annotated repeats                    \n");

		sink.write("Optional library options:                                                             \n"
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
						+ "                                          sequence to be specified (see -g).   \n"
						+ "--repeat-threshold <nbases>             Drop all reads that have more than     \n"
						+ "                                          this many bases inside annotated     \n"
						+ "                                          repeats. Default: 20.                \n"
						+ "--polyA <nbases>                        Reads have been generated from mRNAs   \n"
                        + "                                          with polyA tails of approximately    \n"
                        + "                                          the given number of bases            \n");
		sink.flush();
	}

	public static void main(String[] args) throws IOException {
		new IsoEmOptionParser().printHelpOn(System.out);
	}

	public OptionSet parse(String[] arguments) {
		return super.parse(arguments);
	}
}
