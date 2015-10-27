package edu.uconn.engr.dna.isoem.processor;

import edu.uconn.engr.dna.format.TaggedSequences;
import edu.uconn.engr.dna.isoem.Coord;
import edu.uconn.engr.dna.isoem.CoordType;
import edu.uconn.engr.dna.isoem.ReadCoordinatesBean;
import edu.uconn.engr.dna.isoem.SortedReadsInfoMap;
import edu.uconn.engr.dna.isoem.SortedReadsInfoMap.ReadInfo;
import edu.uconn.engr.dna.isoem.alignment.AlignmentId;
import edu.uconn.engr.dna.isoem.alignment.PairAlignmentId;
import edu.uconn.engr.dna.util.ParameterRunnable;
import edu.uconn.engr.dna.util.SimpleTokenizer;
import edu.uconn.engr.dna.util.Triple;
import edu.uconn.engr.dna.util.Utils;
import org.apache.log4j.Logger;

import java.util.*;

import static edu.uconn.engr.dna.isoem.processor.SamFlagsInterpreter.*;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jun 19, 2010
 * Time: 3:09:59 PM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class SamLinesToCoordParameterRunnable implements ParameterRunnable<List<String>, Object> {

	private static final Logger log = Logger.getLogger(SamLinesToCoordParameterRunnable.class);
	private final static Set<String> printedErrChroms = new HashSet<String>();
	private static int globalReadCount = 0;
	private final char delim = '\t';
	private final TaggedSequences genome;
	private final int flushReadsThreshold;
	private final boolean matePairs;
	private final int mismatches;
	private final boolean qualityScores;
	private int kmerLength;
	private Map<String, Integer>[] kmerCountForPosition;
	private int nr;
	private int na;
	private ParameterRunnable<ReadCoordinatesBean, ?> coordConsumer;
	private Map<String, List<Coord>> coordinatesByReference = null;
	private SimpleTokenizer tokenizer;
	private SortedReadsInfoMap readIdMap;
	private BitSet readStarts;
	private Map<String, List<Integer>> annotatedRepeatsStartsMap;
	private Map<String, List<Integer>> annotatedRepeatsEndsMap;
	private int repeatReadExclusionThreshold;

//    private static double maxBiasCorrectedWeight = 0;
//    private static double minBiasCorrectedWeight = Double.MAX_VALUE;
	public SamLinesToCoordParameterRunnable(TaggedSequences genome,
					int flushReadsThreshold,
					boolean matePairs,
					int mismatches,
					boolean qualityScores,
					int kmerLength,
					Map<String, Integer>[] kmerCountForPosition) {
		this.genome = genome;
		this.flushReadsThreshold = flushReadsThreshold;
		this.matePairs = matePairs;
		this.mismatches = mismatches;
		this.qualityScores = qualityScores;
		this.kmerLength = kmerLength;
		this.kmerCountForPosition = kmerCountForPosition;
	}

	public void setForwardProcess(ParameterRunnable<ReadCoordinatesBean, ?> coordConsumer) {
		this.coordConsumer = coordConsumer;
	}

	@Override
	public void run(List<String> lines) {
		if (tokenizer == null) {
			tokenizer = new SimpleTokenizer(delim);
		}

		int rn = -1;
		for (String line : lines) {
			if (line == null) {
				// new read
				if (readIdMap != null) {
					readIdMap.clear();
				}
				rn = -1;
				if (nr >= flushReadsThreshold) {
					flush();
				}
				continue;
			}
			try {
				tokenizer.setLine(line);
				tokenizer.skipNext(); // skip read name
				int flags = tokenizer.nextInt();
				String referenceSequenceName = tokenizer.nextString(); // (chromosome)
				if ("*".equals(referenceSequenceName)) {
					System.out.println("Drop: Ref* " + line);
					continue;
				}

				int alignmentStart = tokenizer.nextInt();
				tokenizer.skipNext(); // skip <MAPQ>
				String cigarString = tokenizer.nextString();
				tokenizer.skipNext(); // skip <MRNM>
				int matePosition = tokenizer.nextInt();
				tokenizer.skipNext(); // skip <ISIZE>
				String sequence = tokenizer.nextString();
//				if (sameBaseRepeated(sequence, 'A') || sameBaseRepeated(sequence, 'T')) {
//					System.out.println("Drop: ALLAS " + line);
//					continue;
//				}

				String qualScores;
				if (genome != null) {
					qualScores = tokenizer.nextString();
				} else {
					qualScores = null;
				}


				double qualityScoreWeight;
				if (genome == null) {
					qualityScoreWeight = 1.0;
				} else {
					qualityScoreWeight = computeWeight(referenceSequenceName,
									alignmentStart, sequence, cigarString, qualScores);
					if (qualityScoreWeight == 0.0) {
						continue;
					}
				}
				if (annotatedRepeatsStartsMap != null
								&& fallsWithinAnnotatedRepeats(referenceSequenceName, alignmentStart, cigarString)) {
//                    System.out.println("Falls within annotated repeat " + alignmentStart + " " + cigarString);
					qualityScoreWeight = 0.0;
				}

				if (coordinatesByReference == null) {
					coordinatesByReference = new HashMap<String, List<Coord>>();
					readStarts = new BitSet(flushReadsThreshold);
				}

				double biasCorrectedWeight = computeBiasCorrectedWeight(sequence);

//                synchronized (SamLinesToCoordParameterRunnable.class) {
//                    if (biasCorrectedWeight > maxBiasCorrectedWeight) {
//                        maxBiasCorrectedWeight = biasCorrectedWeight;
//                        System.out.println("max bias corrected weight " + maxBiasCorrectedWeight);
//                    }
//                    if (biasCorrectedWeight < minBiasCorrectedWeight) {
//                        minBiasCorrectedWeight = biasCorrectedWeight;
//                        System.out.println("min bias corrected weight " + minBiasCorrectedWeight);
//                    }
//                }
				if (isReadPaired(flags)) {
					boolean firstReadInPair = isFirstReadInPair(flags);
					if (readIdMap == null) {
						readIdMap = new SortedReadsInfoMap();
					}
					ReadInfo mate = readIdMap.remove(referenceSequenceName,
									matePosition, !firstReadInPair);
					if (mate == null) // this read expects a mate
					{
						readIdMap.put(alignmentStart, firstReadInPair,
										new ReadInfo(referenceSequenceName, cigarString,
										qualityScoreWeight, biasCorrectedWeight, flags));
					} else {
						qualityScoreWeight *= mate.getQuality();
						boolean mateOnPositiveStrand = isMateOnPositiveStrand(flags);
						boolean alignmentOnPositiveStrand = isAlignmentOnPositiveStrand(flags);
						// mate pairs should be aligned on same strand
						// regular pairs should be on opposite strands
						if (matePairs == (alignmentOnPositiveStrand == mateOnPositiveStrand)) {
							AlignmentId id;
							boolean firstReadOnPositiveStrand = firstReadInPair ? alignmentOnPositiveStrand
											: mateOnPositiveStrand;

							if (rn == -1) {
								rn = nr++;
								readStarts.set(na);
							}

							biasCorrectedWeight = (biasCorrectedWeight + mate.getBiasCorrectedWeight()) / 2;
							if (alignmentStart < matePosition) {
								id = new PairAlignmentId(na, alignmentOnPositiveStrand,
												alignmentStart, firstReadOnPositiveStrand,
												qualityScoreWeight, biasCorrectedWeight);
							} else {
								id = new PairAlignmentId(na, mateOnPositiveStrand,
												matePosition, firstReadOnPositiveStrand,
												qualityScoreWeight, biasCorrectedWeight);
							}
							++na;

							List<Coord> coordinates = getCoordinates(referenceSequenceName);
							addCoords(alignmentStart, id, cigarString,
											coordinates);
							addCoords(matePosition, id, mate.getCigar(), coordinates);
						}
					}
				} else { // unpaired

					if (rn == -1) {
						synchronized (SamLinesToCoordParameterRunnable.class) {
							globalReadCount++;
						}
						rn = nr++;
						readStarts.set(na);
					}
					boolean alignmentOnPositiveStrand = isAlignmentOnPositiveStrand(flags);
					AlignmentId id = new AlignmentId(na++, alignmentStart,
									alignmentOnPositiveStrand, qualityScoreWeight, biasCorrectedWeight);
					List<Coord> coordinates = getCoordinates(referenceSequenceName);
					addCoords(alignmentStart, id, cigarString, coordinates);
				}
			} catch (RuntimeException e) {
				System.err.println("ERROR in line" + line);
				throw e;
			}
		}
	}

	private boolean sameBaseRepeated(String sequence, char base) {
		int n = sequence.length();
		int i = 0;
		while (i < n && sequence.charAt(i) == base) {
			++i;
		}
		return i == n;
	}

	private double computeBiasCorrectedWeight(String sequence) {
		String kmer = sequence.substring(0, kmerLength);
//        int end = Math.min(29, sequence.length() - kmerLength + 1);
		int end = sequence.length() / 2 + 3;
		int n = sumKmerCount(kmer, end - 6, end);
		int d = sumKmerCount(kmer, 0, 2);
		if (d == 0) {
			d = 1;
		}
		return n / 3.0 / (double) d;
	}

	private int sumKmerCount(String kmer, int start, int end) {
		int s = 0;
		for (; start < end; ++start) {
			s += Utils.get(kmerCountForPosition[start], kmer, 0);
		}
		return s;
	}

	private void flush() {
		if (coordinatesByReference != null) {
			coordConsumer.run(new ReadCoordinatesBean(coordinatesByReference,
							nr, na, readStarts));
			coordinatesByReference = null;
			readStarts = null;
			nr = 0;
			na = 0;
		}
	}

	@Override
	public Object done() {
		flush();
		synchronized (SamLinesToCoordParameterRunnable.class) {
			System.out.println("Global read count " + globalReadCount);
		}
		return coordConsumer.done();
	}

	private List<Coord> getCoordinates(String referenceSequenceName) {
		List<Coord> coordinates = null;
		coordinates = coordinatesByReference.get(referenceSequenceName);
		if (coordinates == null) {
			coordinatesByReference.put(new String(referenceSequenceName),
							coordinates = new ArrayList<Coord>());
		}
		return coordinates;
	}

	private void addCoords(int pos, AlignmentId readId, String cigar,
					List<Coord> coordinates) {
		Coord previousCoord = null;

		int i = 0;
		int n = cigar.length();
		char prevSymbol = 'N';
		do {
			int k = i;
			while (i < n && Character.isDigit(cigar.charAt(i))) {
				++i;
			}
			if (i == n) {
				throw new IllegalArgumentException(
								"Malformed CIGAR string: " + cigar);
			}
			int elemLen = Utils.parseInt(cigar, 10, k, i);

			char cigarSymbol = cigar.charAt(i);
			if (cigarSymbol == 'M' || cigarSymbol == 'S') {
				if (prevSymbol != 'N') {
					// have to collapse intervals so we drop the end of the
					// previous interval and only add the end of the new one
					previousCoord = (Coord) previousCoord.getPreviousSignature();
				} else {
					// interval start
					previousCoord = Coord.newUnnamedInstance(pos,
									previousCoord, CoordType.START);
				}

				// interval end
				int p = pos + elemLen - 1;
				if (i == n - 1) // read end
				{
					previousCoord = Coord.newNamedInstance(p, readId,
									previousCoord, CoordType.END);
				} else {
					previousCoord = Coord.newUnnamedInstance(p,
									previousCoord, CoordType.END);
				}
			} else if (cigarSymbol != 'N') {
				throw new IllegalArgumentException(
								"CIGAR character: " + cigarSymbol + " is not supported (in cigar="
								+ cigar + ")");
			}
			prevSymbol = cigarSymbol;
			++i;
			pos += elemLen;
		} while (i < n);
		// only interested in the read end
		if (previousCoord != null) {
			if (previousCoord.getPos() == 247180030) {
				System.out.println("What do we have here");
			}
			coordinates.add(previousCoord);
		}

	}

	private double computeWeight(String referenceSequenceName, int pos,
					String sequence, String cigar, String qualScores) {
		CharSequence chromSeq = genome.getSequence(referenceSequenceName);
		if (chromSeq == null) {
			if (!printedErrChroms.contains(referenceSequenceName)) {
				synchronized (printedErrChroms) {
					if (!printedErrChroms.add(referenceSequenceName)) {
						System.out.println("WARNING: Missing nucleotide sequence for reference sequence " + referenceSequenceName);
					}
				}
			}
			return 0.0;
		}

		CharSequence chrom = chromSeq;

		int i = 0;
		int n = cigar.length();
		int soffset = 0;
		int m = 0;
		double quality = 1.0;
		do {
			int k = i;
			while (i < n && Character.isDigit(cigar.charAt(i))) {
				++i;
			}
			if (i == n) {
				throw new IllegalArgumentException(
								"Malformed CIGAR string: " + cigar);
			}
			int elemLen = Utils.parseInt(cigar, 10, k, i);

			if (cigar.charAt(i) == 'M') {
				for (int a = pos - 1, b = soffset, c = 0; c < elemLen; ++a, ++b, ++c) {
					double p = 0.0;
					if (qualityScores) {
						p = Utils.phredProbability(Utils.fastqToPhred(qualScores.charAt(b)));
					}
					if (a >= chrom.length()) {
						System.out.println("WARNING: alignment goes outside sequence " + referenceSequenceName
										+ " which has length " + chrom.length() + ". Ignored");
						return 0.0;
					}
					if (chrom.charAt(a) == sequence.charAt(b)) {
						if (qualityScores) {
							quality *= 1 - p;
						}

					} else {
						if (qualityScores) {
							quality *= p / 3;
						}

						++m;
						if (m > mismatches) {
							return 0.0;
						}
					}
				}
				soffset += elemLen;
			}
			pos += elemLen;
			++i;
		} while (i < n);
		return quality;
	}

	private boolean fallsWithinAnnotatedRepeats(String referenceSequenceName, int pos,
					String cigar) {
		List<Integer> annotatedRepStarts = annotatedRepeatsStartsMap.get(referenceSequenceName);
		List<Integer> annotatedRepEnds = annotatedRepeatsEndsMap.get(referenceSequenceName);
		if (annotatedRepStarts == null) {
			return false;
		}
		int i = 0;
		int n = cigar.length();
		int basesInsideRepeat = 0;
		do {
			int k = i;
			while (i < n && Character.isDigit(cigar.charAt(i))) {
				++i;
			}
			if (i == n) {
				throw new IllegalArgumentException(
								"Malformed CIGAR string: " + cigar);
			}
			int elemLen = Utils.parseInt(cigar, 10, k, i);

			if (cigar.charAt(i) == 'M') {
				basesInsideRepeat += insideAnnotatedRepeat(annotatedRepStarts, annotatedRepEnds, pos, pos + elemLen - 1);
			}
			pos += elemLen;
			++i;
		} while (i < n);
		return basesInsideRepeat >= repeatReadExclusionThreshold;
	}

	private int insideAnnotatedRepeat(List<Integer> starts, List<Integer> ends, int start, int end) {
		int pos = Collections.binarySearch(starts, start);
		if (pos < 0) {
			pos = -pos - 1;
		}
		if (pos == 0) {
			return 0;
		}
		int repeatEnd = ends.get(pos - 1);
		if (repeatEnd >= end) {
			return end - start + 1;
		} else if (repeatEnd >= start) {
			return repeatEnd - start + 1;
		} else {
			return 0;
		}
	}

	public void setAnnotatedRepeatsStartsMap(Map<String, List<Integer>> annotatedRepeatsStartsMap) {
		this.annotatedRepeatsStartsMap = annotatedRepeatsStartsMap;
	}

	public void setAnnotatedRepeatsEndsMap(Map<String, List<Integer>> annotatedRepeatsEndsMap) {
		this.annotatedRepeatsEndsMap = annotatedRepeatsEndsMap;
	}

	public void setRepeatReadExclusionThreshold(int repeatReadExclusionThreshold) {
		this.repeatReadExclusionThreshold = repeatReadExclusionThreshold;
	}
}
