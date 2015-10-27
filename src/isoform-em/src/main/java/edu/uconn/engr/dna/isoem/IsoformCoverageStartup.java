package edu.uconn.engr.dna.isoem;

import java.util.zip.GZIPInputStream;
import edu.uconn.engr.dna.format.*;
import edu.uconn.engr.dna.io.DefaultTwoFieldParser;
import edu.uconn.engr.dna.io.GTFParser;
import edu.uconn.engr.dna.io.GTFWriter;
import edu.uconn.engr.dna.io.GenesAndIsoformsParser;
import edu.uconn.engr.dna.probability.CumulativeProbabilityDistribution;
import edu.uconn.engr.dna.probability.NormalProbabilityDistribution;
import edu.uconn.engr.dna.util.*;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import static edu.uconn.engr.dna.isoem.IsoformCoverageOptionParser.*;

/**
 * @author marius
 */
public class IsoformCoverageStartup {

	private static Logger log = Logger.getLogger(IsoformCoverageStartup.class);
	private static final int KMER_LENGTH = 6;

	public static void main(String[] argsList) throws Exception {
//		for (int i = 0; i < argsList.length; ++i) {
//			System.out.println(argsList[i]);
//		}

		OptionParser parser = new IsoformCoverageOptionParser();
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
		if (!options.has(OP_GTF) || !options.has(OP_FREQ_FILE) || samFiles.size() < 1 || options.has(OP_HELP)) {
			parser.printHelpOn(System.out);
			System.exit(1);
		}

		if ((options.has(OP_MISMATCHES) || options.has(OP_QUALITY))
				&& !options.has(OP_GENOME)) {
			System.out.printf(
					"Genome sequence not specified (option -%s missing). Run with -%s to see he help screen.\n",
					OP_GENOME, OP_HELP);
			System.exit(1);
		}

		CumulativeProbabilityDistribution pd = null;
		if (options.has(OP_FRAG_MEAN) && options.has(OP_FRAG_DEV)) {
			pd = new NormalProbabilityDistribution((Double) options.valueOf(OP_FRAG_MEAN), (Double) options.valueOf(OP_FRAG_DEV), 0);
		} else if (!options.has(OP_FRAG_DISTRIB_AUTO)) {
			System.out.println("Fragment length distribution unknown!");
			System.out.println("You must specify the fragment length distribution either as mean and std-dev,\n"
					+ "or ask to be detected automatically by using the option "
					+ OP_FRAG_DISTRIB_AUTO);
			System.out.println("Type isoem -h to print help information");
			System.exit(1);
		}

		IsoformCoverageStartup startup = new IsoformCoverageStartup();
		TimingTool timer = new TimingTool();
		timer.start(null);

		System.out.println("Parsing GTF file...");
		GenesAndIsoformsParser giParser = new GTFParser();
		Pair<Clusters, Isoforms> p = giParser.parse(new FileInputStream(options.valueOf(OP_GTF).toString()));
		Clusters clusters = p.getFirst();
		Isoforms isoforms = p.getSecond();
		if (options.has(OP_CLUSTERS_FILE)) {
			System.out.println("Parsing clusters file...");
			clusters = new Clusters();
			DefaultTwoFieldParser.getInvertedTwoFieldParser(clusters).parse(
					new FileInputStream(options.valueOf(OP_CLUSTERS_FILE).toString()));
			EmUtils.synchronizeClustersWithIsoforms(clusters, isoforms);
		}
		System.out.println("Found " + isoforms.size() + " isoforms and "
				+ clusters.size() + " genes");

		final Map<String, List<Coord>> sortedIsoformCoords = EmUtils.getIsoformCoords(isoforms, new HashMap<String, List<Coord>>());
		EmUtils.sortIsoCoords(sortedIsoformCoords);

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
			timer.stopAndPrintTime();
			System.out.println("Occupied memory " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024) + "k");
		} else {
			genome = null;
		}

		final boolean isFirstReadFromCodingStrand = options.has(OP_STRAND);
		final boolean isAntisense = options.has(OP_ANTISENSE);

		String freqFileName = String.valueOf(options.valueOf(OP_FREQ_FILE));
		System.out.println("Parsing frequency file..." + freqFileName);
		StringToDoubleRandomAccessMap<String> freqMap = new StringToDoubleRandomAccessMap();
		DefaultTwoFieldParser.getRegularTwoFieldParser(freqMap).parse(
				new FileInputStream(options.valueOf(OP_FREQ_FILE).toString()));

		int polyALength = -1;
		Isoforms originalIsoforms = isoforms;
		if (options.has(OP_POLYA)) {
			polyALength = (Integer) options.valueOf(OP_POLYA);
			isoforms = new Isoforms(isoforms);
			Utils.addFakePolyAToExons(isoforms, polyALength);
		}

		log.debug("FirstReadFromCS? " + isFirstReadFromCodingStrand);
		final boolean convertToGenomeCoord = false;
		int n = samFiles.size();

		boolean runUniq = options.has(OP_UNIQ);
		for (int i = 0; i < n; ++i) {
			try {
				String samReadsFile = samFiles.get(i);
				boolean matePairs = options.has(IsoEmOptionParser.OP_MATE_PAIRS);

				IsoEMFlowTool tool = new IsoEMFlowTool(isoforms,
						isFirstReadFromCodingStrand, isAntisense,
						matePairs, genome, qualityScores, mismatches, KMER_LENGTH,
						null, null, null, -1, runUniq, false, polyALength, -1);

				if (options.has(OP_BIAS)) {
					System.out.println("Detecting biases...");
					tool.detectKmerCount(EmUtils.getSamReader(samReadsFile));
				} else {
					tool.setKmerCount(EmUtils.createFakeKmerCount());
				}


				IsoformCoverageFlow flow = new IsoformCoverageFlow(tool, pd, freqMap);

				System.out.println("Parsing reads file... " + samReadsFile);
				Map<String, WeightedIntervals> coverage = flow.computeCoverage(
						EmUtils.getSamReader(samReadsFile));
				//flow.scale(coverage, 1000000.0 / flow.getNumberOfReads());
				System.out.println("# reads: " + flow.getNumberOfReads());
				Map<String, List<String>> clusterToIsoformsSortedByFreq = sortIsoformsByFreqDecreasingly(clusters, freqMap);
				Collection<String> chromosomes = EmUtils.getAllChromosomes(isoforms);

				String outputFileNamePrefix = null;
				if (options.has(OP_OUTPUT_FILE_PREFIX)) {
					outputFileNamePrefix = options.valueOf(OP_OUTPUT_FILE_PREFIX).toString();
				} else {
					outputFileNamePrefix = samReadsFile.replace(".sam.gz", "");
					outputFileNamePrefix = outputFileNamePrefix.replace(".sam", "");
				}

				String outputFileName = null;
				if (options.has(OP_OUTPUT_FILE_PREFIX)) {
					outputFileName = outputFileNamePrefix + ".bed";
				} else {
					outputFileName = outputFileNamePrefix + "_iso_read_coverage.bed";
				}

				System.out.println("Writing read coverage to "
						+ outputFileName);
				PrintWriter w = new PrintWriter(outputFileName);
				String description = "";
				if (options.has(OP_DESCRIPTION)) {
					description = options.valueOf(OP_DESCRIPTION).toString();
				}
				startup.writeBed(0, Color.RED, coverage, isoforms,
						clusterToIsoformsSortedByFreq, chromosomes,
						description, w);
				startup.writeBed(1, Color.BLUE, coverage, isoforms,
						clusterToIsoformsSortedByFreq, chromosomes,
						description, w);
				w.close();

				if (options.has(OP_OUTPUT_FILE_PREFIX)) {
					outputFileName = outputFileNamePrefix + ".gtf";
				} else {
					outputFileName = outputFileNamePrefix + "_isoforms_w_fpkm.gtf";
				}
				System.out.println("Writing isoforms to "
						+ outputFileName);
				w = new PrintWriter(outputFileName);
				startup.writeGTF(clusterToIsoformsSortedByFreq,
						Utils.createIsoformToClusterMap(originalIsoforms, clusters),
						chromosomes, originalIsoforms, freqMap, description, w);
				w.close();

				timer.stop();
				System.out.printf("Done. (%.2fs)\n",
						timer.getGlobalTime() / 1000.0);
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
	}


	private void writeGTF(Map<String, List<String>> clusterIsoformsSortedByFreq,
			Map<String, String> isoformToClusterMap,
			Collection<String> chromosomes,
			Isoforms isoforms,
			Map<String, Double> freqMap,
			String description,
			PrintWriter w) throws IOException {
		w.println(String.format("track name=\"%s Isoform FPKMs\" description=\"%s Isoform FPKMs\" useScore=1",
				description, description));
		GTFWriter gtfWriter = new GTFWriter(w, "isoviz");
		double c = 999/(Math.log(1000)/Math.log(2));
		for (Isoform iso: isoforms.groupIterator()) {
//		for (String chromosome : chromosomes) {
//			for (Map.Entry<String, List<String>> e : clusterIsoformsSortedByFreq.entrySet()) {
//				List<String> isoformsInCluster = e.getValue();
//				for (String i : isoformsInCluster) {
//					Isoform iso = isoforms.getValue(i);
//					if (chromosome.equals(iso.getChromosome())) {
						Double fpkm = freqMap.get(iso.getName());
						if (fpkm >= 1) {
							double intensity = Math.min(1000.0, 1+c*(Math.log(Math.max(1,fpkm))/Math.log(2)));
                                                 //       double intensity = fpkm;
							gtfWriter.writeExons(iso.getChromosome(), iso.getExons(),
									intensity, iso.getStrand(),
									isoformToClusterMap.get(iso.getName()),
									iso.getName() + ":" + String.format("%.1f", fpkm));
						}
//					}
//				}
//			}
		}
	}

	private static Map<String, List<String>> sortIsoformsByFreqDecreasingly(
			Clusters clusters,
			final StringToDoubleRandomAccessMap<String> freqMap) {
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		for (Cluster c : clusters.groupIterator()) {
			List<String> isoNames = new ArrayList(c.keySet());
			Collections.sort(isoNames, new Comparator<String>() {

				@Override
				public int compare(String i1, String i2) {
					Double f1 = freqMap.get(i1);
					Double f2 = freqMap.get(i2);
					if (f1 == null) {
						f1 = 0.0;
					}
					if (f2 == null) {
						f2 = 0.0;
					}
					return f2.compareTo(f1);
				}
			});
			result.put(c.getName(), isoNames);
		}
		return result;
	}

	private void writeBed(int track,
			Color color,
			Map<String, WeightedIntervals> coverage,
			Isoforms isoforms,
			Map<String, List<String>> clusterIsoformsSortedByFreq,
			Collection<String> chromosomes,
			String description,
			PrintWriter w) throws FileNotFoundException {
		w.printf("track type=bedGraph color=%d,%d,%d name=\"%s %s most frequent isoform\"\n",
				color.getRed(), color.getGreen(), color.getBlue(),
				description,
				literal(track + 1));
		for (String chromosome : chromosomes) {
			List<Isoform> trackIsoforms = getIsoformsForTrack(track, isoforms, clusterIsoformsSortedByFreq, chromosome);
			sortByIsoformStart(trackIsoforms);
			BedWritingIntervalHandler handler = new BedWritingIntervalHandler(w, chromosome);
			for (Isoform iso : trackIsoforms) {
				WeightedIntervals wi = coverage.get(iso.getName());
				if (wi != null) {
					handler.setIsoform(iso);
					wi.traverseIntervals(handler, 100.0);
				}
			}
		}

	}

	class BedWritingIntervalHandler implements WeightedIntervals.WeightedIntervalHandler {

		private int prevEnd = -1;
		private PrintWriter writer;
		private String chromosome;
		private Isoform isoform;
		private IntervalVisitorWithWeight iv;

		BedWritingIntervalHandler(PrintWriter writer, String chromosome) {
			this.writer = writer;
			this.chromosome = chromosome;
			this.iv = new IntervalVisitorWithWeight();
		}

		@Override
		public void handle(int start, int end, double weight) {
			if (weight > 0) {
				iv.setWeight(weight);
				CoordinatesMapper.visitGenomeIntervals(isoform.getExons(), start + 1, end, iv);
			}
		}

		public void setIsoform(Isoform isoform) {
			this.isoform = isoform;
		}

		class IntervalVisitorWithWeight implements IntervalVisitor {

			private double weight;

			@Override
			public void visit(int isoformIntervalStart, int isoformIntervalEnd, int genomeIntervalStart, int genomeIntervalEnd) {
				genomeIntervalStart -= 1;
				if (prevEnd != -1 && prevEnd < genomeIntervalStart) {
					writer.printf("%s\t%d\t%d\t%f\n", chromosome, prevEnd, genomeIntervalStart, 0.0);
				}

				writer.printf("%s\t%d\t%d\t%f\n", chromosome,
						genomeIntervalStart,
						prevEnd = genomeIntervalEnd,
						weight);
			}

			public void setWeight(double weight) {
				this.weight = weight;
			}
		}
	}

	private void sortByIsoformStart(List<Isoform> trackIsoforms) {
		Collections.sort(trackIsoforms, new Comparator<Isoform>() {

			@Override
			public int compare(Isoform i1, Isoform i2) {
				return (int) (i1.getExons().getStart() - i2.getExons().getStart());
			}
		});
	}

	private List<Isoform> getIsoformsForTrack(
			int track,
			Isoforms isoforms,
			Map<String, List<String>> clusterIsoformsSortedByFreq,
			String chromosome) {
		List<Isoform> trackIsoforms = new ArrayList<Isoform>();
		for (Map.Entry<String, List<String>> e : clusterIsoformsSortedByFreq.entrySet()) {
			List<String> isoformsInCluster = e.getValue();
			if (isoformsInCluster.size() > track) {
				Isoform isoform = isoforms.getValue(isoformsInCluster.get(track));
				if (chromosome.equals(isoform.getChromosome())) {
					trackIsoforms.add(isoform);
				}
			}
		}
		return trackIsoforms;
	}

	private String literal(int k) {
		switch (k) {
			case 1:
				return "First";
			case 2:
				return "Second";
			case 3:
				return "Third";
			default:
				return k + "th";
		}
	}
}
