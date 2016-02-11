package edu.uconn.engr.dna.isoem;

import edu.uconn.engr.dna.format.Isoform;
import edu.uconn.engr.dna.format.Isoforms;
import edu.uconn.engr.dna.format.TaggedSequences;
import edu.uconn.engr.dna.isoem.processor.*;
import edu.uconn.engr.dna.probability.CumulativeProbabilityDistribution;
import edu.uconn.engr.dna.util.*;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * User: marius
 * Date: Jun 21, 2010
 * Time: 10:32:14 AM
 */
public class IsoEMFlowTool {

	protected static final Logger log = Logger.getLogger(IsoEMFlowTool.class);
	protected static final int maxReadsPerBatch = 1 << 17;
	protected static final int maxLinesPerBatch = 1 << 17;
	protected final int kmerLength;
	protected final int nThreads;
	private static final int BUFF_SIZE = 1 << 16;
	protected final Isoforms isoforms;
	protected final boolean isFirstReadFromCodingStrand;
	protected final boolean matePairs;
	protected final TaggedSequences genome;
	protected final boolean qualityScores;
	protected final int mismatches;
	private Map<String, Integer>[] kmerCount;
	private boolean runUniq;
	private boolean reportCounts;
        private boolean endSeq;
	private Map<String, List<Integer>> annotatedRepeatsStartsMap;
	private Map<String, List<Integer>> annotatedRepeatsEndsMap;
	private int repeatReadExclusionThreshold;
	private int readLength;
	private final Map<String, Pair<int[], PositionData[]>> isoformCoordinates;
    private int polyALen;
	private final int maxNReads;
	private boolean antisense;

    protected IsoEMFlowTool(Isoforms isoforms,
                            boolean firstReadFromCodingStrand,
							boolean antisense,
                            boolean matePairs,
                            TaggedSequences genome,
                            boolean qualityScores,
                            int mismatches,
                            int kmerLength,
                            Map<String, Integer>[] kmerCount,
                            Map<String, List<Integer>> annotatedRepeatsStartsMap,
                            Map<String, List<Integer>> annotatedRepeatsEndsMap,
                            int repeatReadExclusionThreshold,
                            boolean runUniq,
                            boolean reportCounts, 
                            boolean endSeq,
							int polyALen,
							int maxNReads) {
		this.kmerCount = kmerCount;
		this.runUniq = runUniq;
		this.reportCounts = reportCounts;
                this.endSeq = endSeq;
        this.polyALen = polyALen;
        int cores = Runtime.getRuntime().availableProcessors();
		nThreads = cores <= 2 ? 2 : cores - 1;
		this.isoforms = isoforms;
		this.isFirstReadFromCodingStrand = firstReadFromCodingStrand;
		this.antisense = antisense;
		this.matePairs = matePairs;
		this.genome = genome;
		this.qualityScores = qualityScores;
		this.mismatches = mismatches;
		this.kmerLength = kmerLength;
		this.annotatedRepeatsStartsMap = annotatedRepeatsStartsMap;
		this.annotatedRepeatsEndsMap = annotatedRepeatsEndsMap;
		this.repeatReadExclusionThreshold = repeatReadExclusionThreshold;
		this.readLength = -1;
//sahar bug fix: some isoform data was lost with multiple threads
		this.isoformCoordinates = new PositionDataTool().computeDataForChromosomes(nThreads, isoforms);
//		this.isoformCoordinates = new PositionDataTool().computeDataForChromosomes(nThreads, isoforms);
		this.maxNReads = maxNReads;
	}

	protected <T> List<T> parse(Reader inputFile, int nThreads, ParameterRunnableFactory<List<String>, T> parameterRunnableFactory) throws IOException, InterruptedException {
		ThreadPooledSamParser parser = new ThreadPooledSamParser<T>(
						nThreads, Math.max(10, 3 * nThreads),
						maxLinesPerBatch, 
						parameterRunnableFactory,
						maxNReads == -1 ? Integer.MAX_VALUE : maxNReads);
		List<T> result = parser.parse(inputFile);
		readLength = parser.getReadLength();
		return result;
	}

	/**
	 * For each cluster, in parallel, run EM
	 */
	protected Map<String, Double> runEM(List<List<IsoformList>> clusters, Map<String, Double> adjustedIsoLengths) throws InterruptedException {
		long start = System.currentTimeMillis();
		System.out.println("Running EM...");
//		Map<Integer, Double> adjustedIntIsoLengths = new HashMap<Integer, Double>();
//		for (Map.Entry<String, Double> e : adjustedIsoLengths.entrySet()) {
//			adjustedIntIsoLengths.put(isoforms.getIndexOf(e.getKey()), e.getValue());
//		}
		List<Map<Object, Double>> results = BatchThreadPoolExecutor.newInstance(nThreads,
						new ArrayBlockingQueue<List<IsoformList>>(Math.max(100, 10 * nThreads)),
						ParameterRunnableFactory.instance(EmParameterRunnable.class, adjustedIsoLengths, runUniq, reportCounts),
						false).processAll(clusters).waitForTermination();
		Map<Object, Double> freq = Utils.reduce(results, UniformBinaryOperator.<Object, Double>mapReunion());
		if (freq == null) {
			freq = new HashMap<Object, Double>();
		}
		log.debug("EM " + (System.currentTimeMillis() - start));
		return (Map)freq;
//		Map<String, Double> result = new HashMap<String, Double>();
//		for (Map.Entry<Object, Double> e : freq.entrySet()) {
//			result.put(isoforms.getKey((Integer) e.getKey()), e.getValue());
//		}
//		return result;
	}

	protected Map<String, Double> createAdjustedIsoLengths(CumulativeProbabilityDistribution pd) {
		double fragmentLengthMean = pd.getMean();
		double stdDev = Math.sqrt(pd.getVariance());
		Map<String, Double> adjustedIsoLengths = new HashMap<String, Double>();
		if (annotatedRepeatsStartsMap == null) {
			int count = 0;
			for (Isoform i : isoforms.groupIterator()) {
				int l = i.length();
				double adjLen;
                                if (isEndSeq()) {
                                    adjLen = (double)l>fragmentLengthMean ? fragmentLengthMean : (double)l;
                                    adjLen = Math.round(adjLen);
                                }
                                else {
                                adjLen = 0;
				if (l > fragmentLengthMean + 4 * stdDev) {
					adjLen = l - fragmentLengthMean + 1;
				} else {
					for (int k = 1; k <= l; ++k) {
						adjLen += pd.getWeight(k, 0) * (l - k + 1);
					}
					adjLen = Math.round(adjLen);
				}
                                }
				adjustedIsoLengths.put(i.getName(), adjLen);
			}
//            System.out.println("There are " + count + " isoforms shorter than 400 bases");
		} else {
			for (Isoform iso : isoforms.groupIterator()) {
                                int adjustedLength;
                                if (isEndSeq()) {
                                    double l = (double)iso.length();
                                    double adjLen = (l>fragmentLengthMean ? fragmentLengthMean : l);
                                    adjustedLength = (int)Math.round(adjLen);
                                } else
                                {
				String chrom = iso.getChromosome();
				int n = (int) (iso.length() - fragmentLengthMean + 1);
				int adjustedLength = n;
				if (annotatedRepeatsStartsMap.containsKey(chrom)) {
					RepeatIsoMapper repeatIsoMapper = new RepeatIsoMapper(iso,
									annotatedRepeatsStartsMap.get(chrom),
									annotatedRepeatsEndsMap.get(chrom));
					int repeatLength = 0;
					for (int i = 0; i < n; ++i) {
						if (repeatIsoMapper.isNextPositionInRepeat()) {
							++repeatLength;
						} else {
							if (repeatLength > readLength - repeatReadExclusionThreshold) {
								adjustedLength -= (repeatLength - readLength + 2 * repeatReadExclusionThreshold - 1);
							}
							repeatLength = 0;
						}
					}
					if (repeatLength > readLength - repeatReadExclusionThreshold) {
						adjustedLength -= (repeatLength - readLength + 2 * repeatReadExclusionThreshold - 1);
					}
				}
                                }
				adjustedIsoLengths.put(iso.getName(), Math.max(0.0, adjustedLength));
			}
		}
		return adjustedIsoLengths;
	}

	protected int countReads2(List<List<IsoformList>> rcClusters) {
		int sum = 0;
		for (List<IsoformList> lrc : rcClusters) {
			sum += countReads(lrc);
		}
		return sum;
	}

	protected int countReads(List<IsoformList> rcs) {
		int total = 0;
		for (IsoformList rc : rcs) {
			total += rc.getMultiplicity();
		}
		return total;
	}

	protected int countReadClasses(List<List<IsoformList>> rcIsos) {
		int count = 0;
		for (List<IsoformList> lrc : rcIsos) {
			count += lrc.size();
		}
		return count;
	}

	protected ParameterRunnable<List<String>, Object> createSamLinesToCoord2Runnable(
					ParameterRunnable<ReadCoordinatesBean2, ?> coordConsumer) {
		SamLinesToCoordParameterRunnable2 r = new SamLinesToCoordParameterRunnable2(genome, maxReadsPerBatch,
						matePairs, mismatches, qualityScores, polyALen, kmerLength, kmerCount);
//sahar debug
//                System.err.println("In IsoEMFlowTool constructor with KmerCount structure "+kmerCount);
		r.setForwardProcess(coordConsumer);
		r.setAnnotatedRepeatsStartsMap(annotatedRepeatsStartsMap);
		r.setAnnotatedRepeatsEndsMap(annotatedRepeatsEndsMap);
		r.setRepeatReadExclusionThreshold(repeatReadExclusionThreshold);
		return r;
	}

	public int getNThreads() {
		return nThreads;
	}

	public Isoforms getIsoforms() {
		return isoforms;
	}

	public <T, R> SingleBatchThreadPoolExecutor<T, R> getClusterProcess() {
		Converter<IsoformList, Iterator<Object>> iteratorFactory = new Converter<IsoformList, Iterator<Object>>() {
			@Override
			public Iterator<Object> convert(IsoformList item) throws IllegalStateException {
				return item.keySet().iterator();
			}
		};
		BinaryOperator<IsoformList, IsoformList, Void> conflictResolver = new BinaryOperator<IsoformList, IsoformList, Void>() {
			@Override
			public Void compute(IsoformList toBeRemoved, IsoformList toBeKept) {
				toBeKept.setMultiplicity(toBeRemoved.getMultiplicity() + toBeKept.getMultiplicity());
				return null;
			}
		};
		return new SingleBatchThreadPoolExecutor(
						new ReadClassCompactAndClusterParameterRunnable(iteratorFactory, conflictResolver),
						//						                new ReadClassCollectParameterRunnable(),
						new ArrayBlockingQueue<List<IsoformList>>(
						Math.max(200, 10 * nThreads)));
	}

	public ParameterRunnable<ReadCoordinatesBean2, IsoformList[]> createCoordToIsoformListRunnable2(
					CumulativeProbabilityDistribution pd,
					ParameterRunnable<IsoformListsBean, ?> forwardProcess) {
		final CoordToIsoformListForAlignmentsParameterRunnable2 r2 = new CoordToIsoformListForAlignmentsParameterRunnable2(
						isoforms, isoformCoordinates, isFirstReadFromCodingStrand, antisense,
						matePairs, pd);
		r2.setForwardProcess(forwardProcess);
		return r2;
	}

	public void setKmerCount(Map<String, Integer>[] kmerCounts) {
		this.kmerCount = kmerCounts;
	}

	public boolean usesBiasCorrection() {
		return kmerCount != null;
	}
	
	public void detectKmerCount(Reader samReader) throws IOException, InterruptedException {
		setKmerCount(
						Utils.reduce(
						parse(
						samReader,
						getNThreads(),
						ParameterRunnableFactory.instance(KmerCounter.class, kmerLength)),
						UniformBinaryOperator.arrayMerge(
						UniformBinaryOperator.<String, Integer>mapMerger(true,
						UniformBinaryOperator.IntegerSum))));
	}

	public double countUniqReads(List<List<IsoformList>> clusters) {
		double uniq = 0;
		for (List<IsoformList> l : clusters) {
			for (IsoformList il : l) {
				if (il.size() == 1) {
					uniq += il.getMultiplicity();
				}
			}
		}
		return uniq;
	}

	boolean isReportCounts() {
		return reportCounts;
	}

        boolean isEndSeq() {
                return endSeq;
        }

	static class RepeatIsoMapper {

		private Intervals exons;
		private List<Integer> repeatStarts;
		private List<Integer> repeatEnds;
		private int isoformExon;
		private int genomePosition;
		private int repeatIndex;

		public RepeatIsoMapper(Isoform iso, List<Integer> repeatStarts, List<Integer> repeatEnds) {
			this.exons = iso.getExons();
			this.repeatStarts = repeatStarts;
			this.repeatEnds = repeatEnds;

			this.isoformExon = 0;
			this.genomePosition = (int) exons.getStart(isoformExon);
			this.repeatIndex = Collections.binarySearch(repeatStarts, genomePosition);
			if (repeatIndex < 0) {
				repeatIndex = -repeatIndex - 1;
			}
		}

		public boolean isNextPositionInRepeat() {
			boolean answer = repeatIndex < repeatStarts.size()
							&& repeatStarts.get(repeatIndex) <= genomePosition
							&& genomePosition <= repeatEnds.get(repeatIndex);
			if (genomePosition == exons.getEnd(isoformExon)) {
				isoformExon++;
				genomePosition = (int) exons.getStart(isoformExon);
			} else {
				genomePosition++;
			}

			while (repeatIndex < repeatStarts.size()
							&& genomePosition > repeatEnds.get(repeatIndex)) {
				repeatIndex++;
			}
			return answer;
		}
	}
}
