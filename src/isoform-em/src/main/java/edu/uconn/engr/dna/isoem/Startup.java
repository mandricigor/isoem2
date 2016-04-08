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
import static edu.uconn.engr.dna.util.Utils.sortEntriesById;
import static edu.uconn.engr.dna.util.Utils.writeValues;
import static edu.uconn.engr.dna.util.Utils.createTarGZ;
import static edu.uconn.engr.dna.util.Utils.removeDir;


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


        public static int generateRandomNber(int aStart, int aEnd){
                Random random = new Random();
            long range = (long)aEnd - (long)aStart + 1; //get the range, casting to long to avoid overflow problems         
            long fraction = (long)(range * random.nextDouble()); // compute a fraction of the range, 0 <= frac < range
            int randomNumber =  (int)(fraction + aStart);
            return randomNumber;
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
                boolean has_number_bootstraps = options.has(OP_NUMBER_BOOTSTRAPS);

                if (has_confidence_value) {
                    nrConfidenceBootstraps = 200; // Just hardcoded, so what?
                    if (options.valueOf(OP_CONFIDENCE_VALUE) != null) {
                        confidenceValue = (int) options.valueOf(OP_CONFIDENCE_VALUE);
                    }
                    cicalc = new ConfidenceIntervalCalculator(confidenceValue);
                }
                if (has_number_bootstraps) {
                    nrBootstraps = (int) options.valueOf(OP_NUMBER_BOOTSTRAPS);
                }

                /*
                if (options.has(OP_CONFIDENCE_VALUE)) {
                    nrBootstraps = 200; // Just hardcoded, so what?
                    confidenceValue = (int) options.valueOf(OP_CONFIDENCE_VALUE);
                    cicalc = new ConfidenceIntervalCalculator(confidenceValue);
                }
                else if (options.has(OP_NUMBER_BOOTSTRAPS)) {
                    nrBootstraps = (int) options.valueOf(OP_NUMBER_BOOTSTRAPS);
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
                                List<List<IsoformList>> new_clusters;

                                int nrIterations = (nrBootstraps > nrConfidenceBootstraps)? nrBootstraps: nrConfidenceBootstraps;
                                for (int bootIteration = 0; bootIteration <= nrIterations; bootIteration ++) {  // MAIN BOOTSTRAP FOR LOOP
                                if (bootIteration > 0) {
                                    new_clusters = doBootstrapClusters(clusters2, bootCount, multiplicities, bootArray);
                                }
                                else {
                                    new_clusters = clusters2;
                                }

                                int bootCount2 = 0;
                                for (int ii = 0; ii < new_clusters.size(); ii ++) {
                                    for (int jj = 0; jj < new_clusters.get(ii).size(); jj ++) {
                                        ArrayIsoformList aiso = (ArrayIsoformList) (new_clusters.get(ii).get(jj));
                                        bootCount2 += aiso.getMultiplicity();
                                    }
                                }

				Map<String, Double> freq = flow.computeFpkms(new_clusters);
				EmUtils.addMissingIds(freq, isoforms.idIterator());
                                Map<String, Double> tpms = EmUtils.computeTpms(freq);                  


                                if (has_confidence_value) {
                                    cicalc.updateIsoFpkm(freq);
                                    cicalc.updateIsoTpm(tpms);
                                }


                                // get the basename of the .sam reads file
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
                                // ----------------------------------------


                                String isoCommonName;
                                String geneCommonName;

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


                                if ((bootIteration == 0) || (bootIteration < nrBootstraps)) {
                                    // writing fpkms for isoforms
				    System.out.println("Writing isoform FPKMs to "
								+ isoOutputFileName);
				    writeValues(sortEntriesById(freq), isoOutputFileName);

                                    // writing tmps for isoforms
				    System.out.println("Writing isoform TPMs to "
								+ isoTpmFileName);
				    writeValues(sortEntriesById(tpms), isoTpmFileName);
                                }
                                //----------------------------------
				Map<String, String> isoformToClusterMap = Utils.createIsoformToClusterMap(isoforms, clusters);
                                Map<String, Double> fpkm_weigthedGeneLengths = Utils.fpkmWeigthedGeneLengths(freq,isoforms, isoformToClusterMap);
				freq = Utils.groupByCluster(freq, isoformToClusterMap);
				EmUtils.addMissingIds(freq, clusters.idIterator());

                                tpms = EmUtils.computeTpms(freq);

                                if (has_confidence_value) {
                                    cicalc.updateGeneFpkm(freq);
                                    cicalc.updateGeneTpm(tpms);
                                }

                                if ((bootIteration == 0) || (bootIteration < nrBootstraps)) {
                                    // writing fpkms for genes
				    System.out.println("Writing gene FPKMs to "
								+ geneOutputFileName);
				    writeValues(sortEntriesById(freq), geneOutputFileName);
                                    // writing tpms for genes
				    System.out.println("Writing gene TPMs to "
								+ geneTpmFileName);
				    writeValues(sortEntriesById(tpms), geneTpmFileName);
                                }
				timer.stop();
				log.debug("Total time " + timer.getGlobalTime());
				System.out.printf("Done. (%.2fs)\n",
								timer.getGlobalTime() / 1000.0);
                                }
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
                        createTarGZ(oDir + "/" + namePrefix + "/bootstrap/", oDir + "/" + namePrefix + "/bootstrap.tar.gz");
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


        public static List<List<IsoformList>> doBootstrapClusters(List<List<IsoformList>> clusters, int bCount, Map<String, Integer> m, List<String> bootArray) {
            // map m stands for multiplicities of each read
            Map<String, Integer> bootMultiplicities = new HashMap<String, Integer>();
            for (Map.Entry<String, Integer> entry: m.entrySet()) {
                bootMultiplicities.put(entry.getKey(), 0);
            }
            for (int k = 0; k < bCount; k ++) {
                int p = generateRandomNber(0, bCount - 1);
                String pickedRead = bootArray.get(p);
                int currentCount = bootMultiplicities.get(pickedRead);
                bootMultiplicities.put(pickedRead, currentCount + 1);
            }
            List<List<IsoformList>> new_clusters = new ArrayList<List<IsoformList>>();
            for (int ii = 0; ii < clusters.size(); ii ++) {
                ArrayList<IsoformList> super_igor_isoform_list = new ArrayList<IsoformList>();
                for (int jj = 0; jj < clusters.get(ii).size(); jj ++) {
                    ArrayIsoformList aiso = (ArrayIsoformList) (clusters.get(ii).get(jj));
                    String aisoName = Integer.toString(ii) + "_" + Integer.toString(jj);
                    if (bootMultiplicities.get(aisoName) > 0) {
                        ArrayIsoformList new_aiso = new ArrayIsoformList(aiso);
                        new_aiso.setMultiplicity(bootMultiplicities.get(aisoName));
                        super_igor_isoform_list.add(new_aiso);
                    }
                }
                new_clusters.add(super_igor_isoform_list);
            }
            return new_clusters;
        }




        /*
        public static List<List<IsoformList>> doBootstrapClusters(List<List<IsoformList>> clusters, int bootCount){
            List<List<IsoformList>> new_clusters = new ArrayList<List<IsoformList>>();
            int[] count = new int[bootCount];
            // initialize count array
            for (int j = 0; j < count.length; j++)
                count[j] = 0;
            // generate number of copies for each read
            for (int j = 0; j < count.length; j++)
                count[generateRandomNber(0, bootCount - 1)]++;
            // Now it is time to recompute the weights for each IsoformList
            int bootCounter = 0;
            for (int ii = 0; ii < clusters.size(); ii ++) {
                ArrayList<IsoformList> super_igor_isoform_list = new ArrayList<IsoformList>();
                for (int jj = 0; jj < clusters.get(ii).size(); jj ++) {
                    // This is the actual bootstrap
                    ArrayIsoformList aiso = (ArrayIsoformList) (clusters.get(ii).get(jj));
                    ArrayIsoformList aiso2 = new ArrayIsoformList(aiso);
                    HashMap<String, Double> aiosMap = new HashMap<String, Double>();
                    for (Map.Entry<String, ArrayList<Double>> entry : aiso2.weightMap.entrySet()) {
                        double sumBootWeight = 0;
                        for (int kk = 0; kk < entry.getValue().size(); kk ++) {
                            sumBootWeight += count[bootCounter++] * entry.getValue().get(kk);
                        }
                        aiosMap.put(entry.getKey(), sumBootWeight);
                    }
                    // Now we have to recompute the weights for each entry of the IsoformList by normalization 
                    double sumWeight = 0;
                    for (Map.Entry<String, Double> entry : aiosMap.entrySet()) {
                        sumWeight += entry.getValue();
                    }
                    for (Map.Entry<String, Double> entry : aiosMap.entrySet()) {
                        aiosMap.put(entry.getKey(), entry.getValue() / sumWeight); // Now I normalized everything in the IsoformList
                    }
                    for (IsoformList.Entry e : aiso2.entrySet()) {
                        String entryName = e.getKey();
                        double newWeight = aiosMap.get(entryName);
                        e.setValue(newWeight);
                    }
                    super_igor_isoform_list.add(aiso2);
                }
                new_clusters.add(super_igor_isoform_list);
            }
            return new_clusters;
        }
        */


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
