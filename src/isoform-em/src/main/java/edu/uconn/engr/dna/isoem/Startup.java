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
import java.util.concurrent.ArrayBlockingQueue;

import edu.uconn.engr.dna.isoem.processor.*;
import static edu.uconn.engr.dna.isoem.IsoEmOptionParser.*;
import static edu.uconn.engr.dna.util.Utils.sortEntriesDesc;
import static edu.uconn.engr.dna.util.Utils.sortEntriesById;
import static edu.uconn.engr.dna.util.Utils.writeValues;
import static edu.uconn.engr.dna.util.Utils.createTarGZ;
import static edu.uconn.engr.dna.util.Utils.removeDir;
import static edu.uconn.engr.dna.util.Utils.checkForSingleEndFiles;


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

                ConfidenceIntervalCalculator cicalc = null;


                String oDir;
                if (!options.has(OP_OUTPUT_DIR)) { // if you read from stdin, provide output file prefix
                    oDir = ".";
		    //parser.printHelpOn(System.out);
		    //System.exit(1);
                }
                else {
                    oDir = options.valueOf(OP_OUTPUT_DIR).toString();
                }
		List<String> samFiles = options.nonOptionArguments();
                
                if (samFiles.size() < 1) {
                    // add option to read from stdin
                    //if (!options.has(OP_OUTPUT_FILE_PREFIX)) { // if you read from stdin, provide output file prefix
		    //    parser.printHelpOn(System.out);
		    //    System.exit(1);
                    //}
                    //else {
                        samFiles = new ArrayList<String>();
                        samFiles.add("stdinSample");
                    //}
                }

                if (!options.has(OP_GTF) || options.has(OP_HELP)) {
                        parser.printHelpOn(System.out);
                        System.exit(1);
                }

                // ADDED in version 1.1.5 by Igor Mandric (GSU)
                int nrBootstraps = 0;
                int nrConfidenceBootstraps = 0;
                int confidenceValue = 95;

                boolean has_confidence_value = options.has(OP_CONFIDENCE_VALUE);
// sahar always generate 200 bootstrap samples
//                boolean has_number_bootstraps = options.has(OP_NUMBER_BOOTSTRAPS);
                boolean has_number_bootstraps = true;

                if (has_confidence_value) {
                    nrConfidenceBootstraps = 200; // Just hardcoded, so what?
                    if (options.valueOf(OP_CONFIDENCE_VALUE) != null) {
                        confidenceValue = (int) options.valueOf(OP_CONFIDENCE_VALUE);
                    }
                    cicalc = new ConfidenceIntervalCalculator(confidenceValue);
                }
                if (has_number_bootstraps) {
// sahar always generate 200 bootstrap samples
//                    nrBootstraps = (int) options.valueOf(OP_NUMBER_BOOTSTRAPS);
                    nrBootstraps = (int) 50;
                }

                /*
                if (options.has(OP_CONFIDENCE_VALUE)) {
                    nrBootstraps = 200; // Just hardcoded, so what?
                    confidenceValue = (int) options.valueOf(OP_CONFIDENCE_VALUE);
                    cicalc = new ConfidenceIntervalCalculator(confidenceValue);
                }
                else if (options.has(OP_NUMBER_BOOTSTRAPS)) {
// sahar always generate 200 bootstrap samples
//                    nrBootstraps = (int) options.valueOf(OP_NUMBER_BOOTSTRAPS);
                    nrBootstraps = (int) 200;
                }
                */

                Map<String, Integer> multiplicities = null;
                List<String> bootArray = null;


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
                boolean isAnySingleEndFile = false;
                isAnySingleEndFile = checkForSingleEndFiles(samFiles);
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
		} else {
                    if (isAnySingleEndFile) {
                        System.out.println("You can not use -a option when supplying single end read alignments to IsoEM!");
                        System.out.println("Type iseom -h to print help information");
                        System.exit(1);
                    }
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
                boolean endSeq = options.has(OP_ENDSEQ);
		for (int i = 0; i < n; ++i) {
                        String namePrefix = null;
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
								runUniq, reportCounts, endSeq, polyALength, readLimit);

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

                                List<List<IsoformList>> clusters2 = flow.computeClusters(EmUtils.getSamReader(samReadsFile));
                                // here we check how many isoformLists we have in reality after computing clusters
                                multiplicities = new HashMap<String, Integer>();
                                bootArray = new ArrayList<String>();
                                int bootCount = 0;
                                for (int ii = 0; ii < clusters2.size(); ii ++) {
                                    for (int jj = 0; jj < clusters2.get(ii).size(); jj ++) {
                                        ArrayIsoformList aiso = (ArrayIsoformList) (clusters2.get(ii).get(jj));
                                        bootCount += aiso.getMultiplicity();
                                        String aisoName = Integer.toString(ii) + "_" + Integer.toString(jj); // IGOR's super hack - we rely on the same order in the ArrayList. It is not changing, right?
                                        multiplicities.put(aisoName, (int) aiso.getMultiplicity());
                                        for (int pp = 0; pp < aiso.getMultiplicity(); pp ++) {
                                            bootArray.add(aisoName);
                                        }
                                    }
                                }
                                List<List<IsoformList>> new_clusters, igor_clusters;


                                int nrIterations = (nrBootstraps > nrConfidenceBootstraps)? nrBootstraps: nrConfidenceBootstraps;


                                new_clusters = new ArrayList<List<IsoformList>>();
                                new_clusters.addAll(clusters2);

                                long startTime = System.currentTimeMillis();


                                int cores = Runtime.getRuntime().availableProcessors();
                                int nThreads = cores <= 2 ? 2 : cores - 1;

                                List<Integer> bootIterationIds = new ArrayList<Integer>();
                                for (int bItId = 1; bItId <= nrIterations; bItId ++) {
                                    bootIterationIds.add(bItId);
                                }

                                List<List<List<IsoformList>>> bootClusters = BatchThreadPoolExecutor.newInstance(nThreads,
                                                new ArrayBlockingQueue<Integer>(Math.max(100, 10 * nThreads)),
                                                ParameterRunnableFactory.instance(BootParameterRunnable.class, clusters2, bootCount, multiplicities, bootArray),
                                                false).processAll(bootIterationIds).waitForTermination();
                                for (List bootBatchCluster: bootClusters) {
                                    new_clusters.addAll(bootBatchCluster);
                                }
                                /*
                                for (int iv = 1; iv <= nrIterations; iv ++) {
                                    doBootstrapClusters(clusters2, new_clusters, bootCount, multiplicities, bootArray, iv);
                                }
                                */

                                long startEMtime = System.currentTimeMillis();
                                System.out.println(startEMtime - startTime);





				Map<String, String> isoformToClusterMap = Utils.createIsoformToClusterMap(isoforms, clusters);
				List<Map<String, Double>> freq = flow.computeFpkms(new_clusters, nrIterations);

                                for (Map<String, Double> fr: freq) {
                                    EmUtils.addMissingIds(fr, isoforms.idIterator());
                                }

                                List<Map<String, Double>> tpms = new ArrayList<Map<String, Double>>();
                                for (Map<String, Double> fr: freq) {
                                    Map<String, Double> tpm = EmUtils.computeTpms(fr);
                                    tpms.add(tpm);
                                }



                                if (has_confidence_value) {
                                    for (Map<String, Double> fr: freq) {
                                        cicalc.updateIsoFpkm(fr);
                                    }
                                    for (Map<String, Double> tpm: tpms) {
                                        cicalc.updateIsoTpm(tpm);
                                    }
                                }


                                List<Map<String, Double>> geneFreq = new ArrayList<Map<String, Double>>();
                                List<Map<String, Double>> geneTpms = new ArrayList<Map<String, Double>>();
                                for (int x = 0; x <= nrIterations; x ++) {
                                    Map<String, Double> fpkm_weigthedGeneLengths = Utils.fpkmWeigthedGeneLengths(freq.get(x),isoforms, isoformToClusterMap);
				    Map<String, Double> fr = Utils.groupByCluster(freq.get(x), isoformToClusterMap);
				    EmUtils.addMissingIds(fr, clusters.idIterator());

                                    Map<String, Double> tpm = EmUtils.computeTpms(fr);

                                    geneFreq.add(fr);
                                    geneTpms.add(tpm);
                                    if (has_confidence_value) {
                                        cicalc.updateGeneFpkm(fr);
                                        cicalc.updateGeneTpm(tpm);
                                    }
                                }

                                namePrefix = samReadsFile.replace(".sam.gz", "");
                                namePrefix = namePrefix.replace(".sam", "");
                                int indexOfLastSlash = namePrefix.lastIndexOf("/");
                                namePrefix = namePrefix.substring(indexOfLastSlash + 1);

                                String isoOutputFileName;
                                String isoTpmFileName;

                                String geneOutputFileName;
                                String geneTpmFileName;

                                // bootstrap confidence intervals filenames
                                String isoOutputCiFileName;
                                String isoTpmCiFileName;

                                String geneOutputCiFileName;
                                String geneTpmCiFileName;


                                String isoCommonName;
                                String geneCommonName;

                                for (int bootIteration = 0; bootIteration <= nrIterations; bootIteration ++) {
                                    if (bootIteration == 0) {
                                        isoCommonName = oDir + "/" + namePrefix + "/output/Isoforms/";
                                        geneCommonName = oDir + "/" + namePrefix + "/output/Genes/";
                                    }
                                    else { // this is a simple bootstrap iteration
                                        isoCommonName = oDir + "/" + namePrefix + "/bootstrap/experiment_" + bootIteration + "/Isoforms/";
                                        geneCommonName = oDir + "/" + namePrefix + "/bootstrap/experiment_" + bootIteration + "/Genes/";
                                    }
                                    isoOutputFileName = isoCommonName + "iso_fpkm_estimates";
                                    isoTpmFileName = isoCommonName + "iso_tpm_estimates";

                                    geneOutputFileName = geneCommonName + "gene_fpkm_estimates";
                                    geneTpmFileName = geneCommonName + "gene_tpm_estimates";


                                    // writing fpkms for isoforms
				    writeValues(sortEntriesById(freq.get(bootIteration)), isoOutputFileName);
                                    // writing tmps for isoforms
				    writeValues(sortEntriesById(tpms.get(bootIteration)), isoTpmFileName);
                                    // writing fpkms for genes
				    writeValues(sortEntriesById(geneFreq.get(bootIteration)), geneOutputFileName);
                                    // writing tpms for genes
				    writeValues(sortEntriesById(geneTpms.get(bootIteration)), geneTpmFileName);
                                }



                                // get the basename of the .sam reads file

				timer.stop();
				log.debug("Total time " + timer.getGlobalTime());
				System.out.printf("Done. (%.2fs)\n",
								timer.getGlobalTime() / 1000.0);
                                //} // END OF BOOTSTRAP LOOPS
                            // now we have to compute the confidence intervals if any and to reset the cicalc
                            if (has_confidence_value) {
                                String dirname = oDir + "/" + namePrefix + "/output/ConfidenceIntervals/";
                                cicalc.writeValues(dirname);
                                cicalc.reset();
                            }
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
                    if (has_number_bootstraps) {
//sahar adding output to the boostrap.gz. needed by isoDE
			   String [] paths;
			   paths = new String[2];
			   paths[0] = oDir + "/" + namePrefix + "/bootstrap/";
			   paths[1] = oDir + "/" + namePrefix + "/output/";
//                        createTarGZ(oDir + "/" + namePrefix + "/bootstrap/", oDir + "/" + namePrefix + "/bootstrap.tar.gz");
                        createTarGZ(paths, oDir + "/" + namePrefix + "/bootstrap.tar.gz");
                        removeDir(oDir + "/" + namePrefix + "/bootstrap/");
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
