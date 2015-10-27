package edu.uconn.engr.dna.isoem;

import edu.uconn.engr.dna.format.*;
import edu.uconn.engr.dna.io.DefaultTwoFieldParser;
import edu.uconn.engr.dna.io.GTFParser;
import edu.uconn.engr.dna.io.GenesAndIsoformsParser;
import edu.uconn.engr.dna.probability.CumulativeProbabilityDistribution;
import edu.uconn.engr.dna.probability.NormalProbabilityDistribution;
import edu.uconn.engr.dna.util.*;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

import static edu.uconn.engr.dna.isoem.IsoEmOptionParser.*;
import static edu.uconn.engr.dna.util.Utils.sortEntriesDesc;
import static edu.uconn.engr.dna.util.Utils.writeValues;

/**
 * Entry point of the EM algorithm: reads SAM input, applies EM, outputs
 * frequencies
 *
 * @author marius
 */
public class Startup {

	private static Logger log = Logger.getLogger(Startup.class);
	private static final int KMER_LENGTH = 6;

	enum EmAlgoType {
		olduniq, uniq, olduniq1em, uniq1em, em, uniqem
	}

	public Startup() {
	}

	public static void main(String[] argsList) throws Exception {
//		for (int i = 0; i < argsList.length; ++i) {
//			System.out.println(argsList[i]);
//		}

		OptionParser parser = new IsoEmOptionParser();
		OptionSet options;
		try {
			options = parser.parse(argsList);
		} catch (Exception e) {
			e.printStackTrace();
			parser.printHelpOn(System.out);
			System.exit(1);
			return;
		}

		List<String> samFiles = options.nonOptionArguments();
                
                if (samFiles.size() < 1) {
                    // add option to read from stdin
                    if (!options.has(OP_OUTPUT_FILE_PREFIX)) { // if you read from stdin, provide output file prefix
			parser.printHelpOn(System.out);
			System.exit(1);
                    }
                    else {
                        samFiles = new ArrayList<String>();
                        samFiles.add("stdin");
                    }
                }

                /*
		if (!options.has(OP_GTF) || samFiles.size() < 1 || options.has(OP_HELP)) {
			parser.printHelpOn(System.out);
			System.exit(1);
		}
                */

		if ((options.has(OP_MISMATCHES) || options.has(OP_QUALITY))
						&& !options.has(OP_GENOME)) {
			System.out.printf(
							"Genome sequence not specified (option -%s missing). Run with -%s to see he help screen.\n",
							OP_GENOME, OP_HELP);
			System.exit(1);
		}

		CumulativeProbabilityDistribution pd = null;
		double fragmentLengthMean = -1;
		if (options.has(OP_FRAG_MEAN) && options.has(OP_FRAG_DEV)) {
			fragmentLengthMean = (Double) options.valueOf(OP_FRAG_MEAN);
			pd = new NormalProbabilityDistribution(fragmentLengthMean, (Double) options.valueOf(OP_FRAG_DEV), 0);
		} else if (!options.has(OP_FRAG_DISTRIB_AUTO)) {
			System.out.println("Fragment length distribution unknown!");
			System.out.println("You must specify the fragment length distribution either as mean and std-dev,\n"
							+ "or ask to be detected automatically by using the option "
							+ OP_FRAG_DISTRIB_AUTO);
			System.out.println("Type isoem -h to print help information");
			System.exit(1);
		}

		TimingTool timer = new TimingTool();
		timer.start(null);

		System.out.println("Parsing GTF file...");
		GenesAndIsoformsParser giParser = new GTFParser();
		Pair<Clusters, Isoforms> p = giParser.parse(new FileInputStream(options.valueOf(OP_GTF).toString()));
		Clusters clusters = p.getFirst();
		final Isoforms isoforms = p.getSecond();
		if (options.has(OP_CLUSTERS_FILE)) {
			System.out.println("Parsing clusters file...");
			clusters = new Clusters();
			DefaultTwoFieldParser.getInvertedTwoFieldParser(clusters).parse(
							new FileInputStream(options.valueOf(OP_CLUSTERS_FILE).toString()));
			EmUtils.synchronizeClustersWithIsoforms(clusters, isoforms);
		}
        int polyALength = -1;
		if (options.has(OP_POLYA)) {
            polyALength = (Integer) options.valueOf(OP_POLYA);
			if (fragmentLengthMean > 0 && polyALength > fragmentLengthMean) {
				polyALength = (int)Math.ceil(fragmentLengthMean);
			}
            Utils.addFakePolyAToExons(isoforms, polyALength);
		}
		System.out.println("Found " + isoforms.size() + " isoforms and "
						+ clusters.size() + " genes");


		Map<String, List<Integer>> annotatedRepeatsStartsMap = null;
		Map<String, List<Integer>> annotatedRepeatsEndsMap = null;
		if (options.has(OP_ANNOTATED_REPEATS_FILE)) {
			String repeatsGTF = options.valueOf(OP_ANNOTATED_REPEATS_FILE).toString();
			System.out.println("Parsing repeats file... " + repeatsGTF);
			Pair<Map<String, List<Integer>>, Map<String, List<Integer>>> repeatStartsAndEnds = getRepeats(repeatsGTF);
			annotatedRepeatsStartsMap = repeatStartsAndEnds.getFirst();
			annotatedRepeatsEndsMap = repeatStartsAndEnds.getSecond();
		}


		final TaggedSequences genome;
		boolean qualityScores = options.has(OP_QUALITY);
		int mismatches = Integer.MAX_VALUE;
		if (options.has(OP_MISMATCHES)) {
			mismatches = (Integer) options.valueOf(OP_MISMATCHES);
		}
		if (qualityScores || mismatches != Integer.MAX_VALUE) {
			timer.start("Loading genome sequence...");
//            genome = new ChromosomeSequences(options.valueOf(OP_GENOME)
//                    .toString());
			genome = (TaggedSequences) new MemoryMapChromosomeSequences(options.valueOf(OP_GENOME).toString());
//			timer.stopAndPrintTime();
//			System.out.println("Memory used " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024) + "k");
		} else {
			genome = null;
		}

		final boolean isFirstReadFromCodingStrand = options.has(OP_STRAND);
		final boolean isAntisense = options.has(OP_ANTISENSE);
  
		log.debug("FirstReadFromCS? " + isFirstReadFromCodingStrand);
		int n = samFiles.size();

		int readRepeatExclusionThreshold = 20;
		if (options.has(OP_REPEAT_THRESHOLD)) {
			readRepeatExclusionThreshold = Integer.parseInt(options.valueOf(OP_REPEAT_THRESHOLD).toString());
		}

		int readLimit = -1;
		if (options.has(OP_LIMIT_NREADS)) {
			readLimit = (Integer)options.valueOf(OP_LIMIT_NREADS);
		}
		boolean runUniq = options.has(OP_UNIQ);
		boolean reportCounts = options.has(OP_COUNT);
		for (int i = 0; i < n; ++i) {
			try {
				String samReadsFile = samFiles.get(i);
				boolean matePairs = options.has(IsoEmOptionParser.OP_MATE_PAIRS);
//sahar debug
                                System.err.println("before new IsoEMFlowTool with KmerCount structure = null");
				IsoEMFlowTool tool = new IsoEMFlowTool(isoforms,
								isFirstReadFromCodingStrand, isAntisense,
								matePairs, genome, qualityScores, mismatches, 
								KMER_LENGTH, null, annotatedRepeatsStartsMap,
								annotatedRepeatsEndsMap, readRepeatExclusionThreshold,
								runUniq, reportCounts, polyALength, readLimit);

				if (options.has(OP_BIAS)) {
					System.out.println("Detecting biases...");
					tool.detectKmerCount(EmUtils.getSamReader(samReadsFile));
				} else {
//                    tool.setKmerCount(EmUtils.createFakeKmerCount());
				}

				IsoEMFlow flow;
				if (pd == null) {
					flow = new IsoEMFlowAutoProbabilityDistribution(tool);
				} else {
					flow = new IsoEmFlowWithKnownProbabilityDistribution(tool, pd);
				}

				System.out.println("Parsing reads file... " + samReadsFile);
				Map<String, Double> freq = flow.computeFpkms(EmUtils.getSamReader(samReadsFile));
				EmUtils.addMissingIds(freq, isoforms.idIterator());
				String outputFileNamePrefix =  null;
				if (options.has(OP_OUTPUT_FILE_PREFIX)) {
					outputFileNamePrefix = options.valueOf(OP_OUTPUT_FILE_PREFIX).toString();
				} else {
					outputFileNamePrefix = samReadsFile.replace(".sam.gz", "");
					outputFileNamePrefix = outputFileNamePrefix.replace(".sam", "");
					if (readLimit != -1) {
						outputFileNamePrefix += "_" + readLimit;
					}
				}
				String isoOutputFileName = outputFileNamePrefix + ".iso_estimates";
				System.out.println("Writing isoform FPKMs to "
								+ isoOutputFileName);
				writeValues(sortEntriesDesc(freq), isoOutputFileName);

				String geneOutputFileName = outputFileNamePrefix + ".gene_estimates";
				Map<String, String> isoformToClusterMap = Utils.createIsoformToClusterMap(isoforms, clusters);
                            Map<String, Double> fpkm_weigthedGeneLengths= Utils.fpkmWeigthedGeneLengths(freq,isoforms, isoformToClusterMap);
				freq = Utils.groupByCluster(freq, isoformToClusterMap);
				EmUtils.addMissingIds(freq, clusters.idIterator());
				System.out.println("Writing gene FPKMs to "
								+ geneOutputFileName);
				writeValues(sortEntriesDesc(freq), geneOutputFileName);
//				System.out.println("Writing FPKM weightd gene lengths to "
//								+ "fpkm_whgted_avg_gene_lengths.txt");
//				writeValues(sortEntriesDesc(fpkm_weigthedGeneLengths), "fpkm_whgted_avg_gene_lengths.txt");

				timer.stop();
				log.debug("Total time " + timer.getGlobalTime());
				System.out.printf("Done. (%.2fs)\n",
								timer.getGlobalTime() / 1000.0);
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
	}

	private static Pair<Map<String, List<Integer>>, Map<String, List<Integer>>> getRepeats(String repeatsGTF) throws Exception {
		GTFParser repeatsParser = new GTFParser(true);
		Pair<Clusters, Isoforms> pair = repeatsParser.parse(new FileInputStream(repeatsGTF));
		Isoforms repeatsIsoforms = pair.getSecond();

		Map<String, List<Pair<Integer, Integer>>> annotatedRepeatsIntervalsMap = new HashMap<String, List<Pair<Integer, Integer>>>();
		for (Isoform iso : repeatsIsoforms.groupIterator()) {
			List<Pair<Integer, Integer>> list = annotatedRepeatsIntervalsMap.get(iso.getChromosome());
			if (list == null) {
				annotatedRepeatsIntervalsMap.put(iso.getChromosome(),
								list = new ArrayList<Pair<Integer, Integer>>());
			}
			Intervals exons = iso.getExons();
			for (int i = 0; i < exons.size(); ++i) {
				list.add(new Pair<Integer, Integer>((int) exons.getStart(i), (int) exons.getEnd(i)));
			}
		}
		Map<String, List<Integer>> annotatedRepeatsStartsMap = new HashMap<String, List<Integer>>();
		Map<String, List<Integer>> annotatedRepeatsEndsMap = new HashMap<String, List<Integer>>();
		int total = 0;
		for (Map.Entry<String, List<Pair<Integer, Integer>>> e : annotatedRepeatsIntervalsMap.entrySet()) {
			List<Pair<Integer, Integer>> l = e.getValue();
			Collections.sort(l, new Comparator<Pair<Integer, Integer>>() {

				@Override
				public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
					return o1.getFirst().compareTo(o2.getFirst());
				}
			});
			annotatedRepeatsStartsMap.put(e.getKey(), Utils.map(l,
							new Converter<Pair<Integer, Integer>, Integer>() {

								@Override
								public Integer convert(Pair<Integer, Integer> item) throws IllegalStateException {
									return item.getFirst();
								}
							}));
			annotatedRepeatsEndsMap.put(e.getKey(), Utils.map(l,
							new Converter<Pair<Integer, Integer>, Integer>() {

								@Override
								public Integer convert(Pair<Integer, Integer> item) throws IllegalStateException {
									return item.getSecond();
								}
							}));

			//System.out.println("Found " + l.size() + " repeats intervals for chromosome " + e.getKey());
			total += l.size();
		}
		System.out.println("Founds " + total + " total repeat intervals");
		return new Pair(annotatedRepeatsStartsMap, annotatedRepeatsEndsMap);
	}

	private static void gcAndPrintMem() {
		System.out.println("=================================");
		System.gc();
		System.out.println("AvailableProcs "
						+ Runtime.getRuntime().availableProcessors());
		long occupiedMemBytes = Runtime.getRuntime().totalMemory()
						- Runtime.getRuntime().freeMemory();
		System.out.println("Memory " + (occupiedMemBytes / 0x100000) + "M ("
						+ occupiedMemBytes + " bytes)");
		System.out.println("=================================");
	}

// sahar
// method not used and makes calls to methods whose interface will change; deprecate.
@Deprecated
	public Pair<String, Reader> getSamReader(String samReadsFile,
					Isoforms isoforms, boolean convertToGenomeCoord) throws IOException {
		Reader samReader = new BufferedReader(new FileReader(samReadsFile),
						1 << 16);
		if (convertToGenomeCoord) {
			samReader = new GenomeCoordConverterReader(isoforms, samReader);
			samReadsFile += "_genome";
		}
		return new Pair<String, Reader>(samReadsFile, samReader);
	}

}
