package edu.uconn.engr.dna.isoem.processor;

import edu.uconn.engr.dna.format.TaggedSequences;
import edu.uconn.engr.dna.isoem.Coord2;
import edu.uconn.engr.dna.isoem.ReadCoordinatesBean2;
import edu.uconn.engr.dna.isoem.SortedReadsInfoMap;
import edu.uconn.engr.dna.isoem.SortedReadsInfoMap.ReadInfo;
import edu.uconn.engr.dna.isoem.alignment.AlignmentId;
import edu.uconn.engr.dna.isoem.alignment.PairAlignmentId;
import edu.uconn.engr.dna.util.*;

import java.util.*;

import static edu.uconn.engr.dna.isoem.processor.SamFlagsInterpreter.*;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jun 19, 2010
 * Time: 3:09:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class SamLinesToCoordParameterRunnable2 implements ParameterRunnable<List<String>, Object> {

	private final static Set<String> printedErrChroms = new HashSet<String>();
	private final char delim = '\t';
	private final TaggedSequences genome;
	private final int flushReadsThreshold;
	private final boolean matePairs;
	private final int mismatches;
	private final boolean qualityScores;
	private final int polyALength;
	private int kmerLength;
	private Map<String, Integer>[] kmerCountForPosition;
	private int nr;
	private int na;
	private ParameterRunnable<ReadCoordinatesBean2, ?> coordConsumer;
	private Map<String, List<Coord2>> coordinatesByReference = null;
	private SimpleTokenizer tokenizer;
	private SortedReadsInfoMap readIdMap;
	private BitSet readStarts;
	private Map<String, List<Integer>> annotatedRepeatsStartsMap;
	private Map<String, List<Integer>> annotatedRepeatsEndsMap;
	private int repeatReadExclusionThreshold;
	private int[] coordBuffer;
	private final boolean polyAEnabled;
	public static int globalCount;
	private static Object lock = new Object();
//	private static int droppedBecauseQual;
//	private static int droppedBecauseRef;

	public SamLinesToCoordParameterRunnable2(TaggedSequences genome,
			int flushReadsThreshold,
			boolean matePairs,
			int mismatches,
			boolean qualityScores,
			int polyALength,
			int kmerLength,
			Map<String, Integer>[] kmerCountForPosition) {
		this.genome = genome;
		this.flushReadsThreshold = flushReadsThreshold;
		this.matePairs = matePairs;
		this.mismatches = mismatches;
		this.qualityScores = qualityScores;
		this.polyALength = polyALength;
		this.polyAEnabled = polyALength >= 0;
		this.kmerLength = kmerLength;
		this.kmerCountForPosition = kmerCountForPosition;
//sahar debug
//                System.err.println("In SamLines...Runnable2 constructor with KmerCount structure "+kmerCountForPosition);
	}

	@Override
	public void run(List<String> lines) {
//        	System.out.println("in run SamLinesToCoordParameterRunnable2 ");
		if (tokenizer == null) {
			tokenizer = new SimpleTokenizer(delim);
		}

		int rn = -1;
		for (String line : lines) {
			if (line == null) {
				synchronized (lock) {
					globalCount++;
				}
				// new read
				if (readIdMap != null) {
					if (!readIdMap.isEmpty()) {
						// add orphan pairs as singles
						if (rn == -1) {
							rn = nr++;
							readStarts.set(na);
						}
						for (Pair<Integer, ReadInfo> p : readIdMap.getRemaining()) {
							int pos = p.getFirst();
							ReadInfo i = p.getSecond();
							boolean alignmentOnPositiveStrand = isAlignmentOnPositiveStrand(i.getFlags());
							addSingle(i.getReadName(), i.getRefSeq(), alignmentOnPositiveStrand, pos,
									i.getCigar(), i.getQuality(), i.getBiasCorrectedWeight());
						}
						readIdMap.clear();
					}
				}
				rn = -1;
				if (nr >= flushReadsThreshold) {
					flush();
				}
				continue;
			}
			try {
				tokenizer.setLine(line);
				//tokenizer.skipNext(); // do not skip read name
                                String readName = tokenizer.nextString();
				int flags = tokenizer.nextInt();
				String referenceSequenceName = tokenizer.nextString(); // (chromosome)
				if ("*".equals(referenceSequenceName)) {
//					synchronized (lock) {
//						droppedBecauseRef++;
//					}
					continue;
				}

				int alignmentStart = tokenizer.nextInt();
				tokenizer.skipNext(); // skip <MAPQ>
				String cigarString = tokenizer.nextString();
				tokenizer.skipNext(); // skip <MRNM>
				int matePosition = tokenizer.nextInt();
				tokenizer.skipNext(); // skip <ISIZE>
				String sequence = tokenizer.nextString();
                                String clippedSequence = Utils.clipSequence(cigarString, sequence);
				if (coordBuffer == null) {
					coordBuffer = new int[2 * sequence.length()];
				}
				int polySuffix = 0;
				if (polyALength >= 0) {
					polySuffix = largestRepeatedATSuffix(sequence);
					if (polySuffix == sequence.length()) {
//					System.out.println("Dropped read at pos " + alignmentStart);
						continue;
					}
				}

				boolean computeQuals = (genome != null);
				String qualScores;
				if (computeQuals) {
					qualScores = tokenizer.nextString();
				} else {
					qualScores = null;
				}
				double qualityScoreWeight;
				if (computeQuals) {
					qualityScoreWeight = computeWeight(referenceSequenceName,
							alignmentStart, sequence, cigarString, qualScores);
					if (qualityScoreWeight == 0.0) {
//						synchronized (lock) {
//							droppedBecauseQual++;
//						}
						continue;
					}
				} else {
					qualityScoreWeight = 1.0;
				}

				if (annotatedRepeatsStartsMap != null
						&& fallsWithinAnnotatedRepeats(referenceSequenceName, alignmentStart, cigarString)) {
//                    System.out.println("Falls within annotated repeat " + alignmentStart + " " + cigarString);
					qualityScoreWeight = 0.0;
				}

				if (coordinatesByReference == null) {
					coordinatesByReference = new HashMap<String, List<Coord2>>();
					readStarts = new BitSet(flushReadsThreshold);
				}

//				double biasCorrectedWeight = (kmerCountForPosition == null) ? 1.0 : computeBiasCorrectedWeight(sequence);
//sahar debug
//                System.err.println("In SamLines...Runnable2 before calling computeBiasCorrectedWeight KmerCount structure "+kmerCountForPosition);
				double biasCorrectedWeight = (kmerCountForPosition == null) ? 1.0 : computeBiasCorrectedWeight(clippedSequence);
				if (isReadPaired(flags)) {
					boolean firstReadInPair = isFirstReadInPair(flags);
					if (readIdMap == null) {
						readIdMap = new SortedReadsInfoMap();
					}
					SortedReadsInfoMap.ReadInfo mate =
							readIdMap.remove(referenceSequenceName, matePosition, !firstReadInPair);
					if (mate == null) { // this read expects a mate
						readIdMap.put(alignmentStart, firstReadInPair,
								new ReadInfo(readName, referenceSequenceName, cigarString,
								qualityScoreWeight, biasCorrectedWeight, flags));
					} else {
						// add pair
						qualityScoreWeight *= mate.getQuality();
						boolean alignmentOnPositiveStrand = isAlignmentOnPositiveStrand(flags);
						boolean mateOnPositiveStrand = isMateOnPositiveStrand(flags);
						// mate pairs should be aligned on same strand
						// regular pairs should be on opposite strands
						if (matePairs == (alignmentOnPositiveStrand == mateOnPositiveStrand)) {
							if (rn == -1) {
								rn = nr++;
								readStarts.set(na);
							}

							AlignmentId id;
							boolean firstReadOnPositiveStrand = firstReadInPair ? alignmentOnPositiveStrand
									: mateOnPositiveStrand;

							biasCorrectedWeight = (biasCorrectedWeight + mate.getBiasCorrectedWeight()) / 2;
							if (alignmentStart < matePosition) {
								id = new PairAlignmentId(na, readName, alignmentOnPositiveStrand,
										alignmentStart, firstReadOnPositiveStrand,
										qualityScoreWeight, biasCorrectedWeight);
							} else {
								id = new PairAlignmentId(na, readName, mateOnPositiveStrand,
										matePosition, firstReadOnPositiveStrand,
										qualityScoreWeight, biasCorrectedWeight);
							}
							++na;

							List<Coord2> coordinates = getCoordinates(referenceSequenceName);
							addCoords(alignmentStart, id, cigarString,
									coordinates);
							addCoords(matePosition, id, mate.getCigar(), coordinates);
						}
					}
				} else { // unpaired

					if (rn == -1) {
						rn = nr++;
						readStarts.set(na);
					}
					boolean alignmentOnPositiveStrand = isAlignmentOnPositiveStrand(flags);
					addSingle(readName, referenceSequenceName, alignmentOnPositiveStrand, alignmentStart, cigarString,
							qualityScoreWeight, biasCorrectedWeight);
				}
			} catch (RuntimeException e) {
				System.err.println("ERROR in line" + line);
				throw e;
			}
		}
	}

	private void addSingle(String readName, String referenceSequenceName,
			boolean alignmentOnPositiveStrand,
			int alignmentStart,
			String cigarString,
			double qualityScoreWeight,
			double biasCorrectedWeight) {
		AlignmentId id = new AlignmentId(na++, readName, alignmentStart,
				alignmentOnPositiveStrand, qualityScoreWeight, biasCorrectedWeight);
		List<Coord2> coordinates = getCoordinates(referenceSequenceName);
		addCoords(alignmentStart, id, cigarString, coordinates);
	}

	private int largestRepeatedATSuffix(String sequence) {
		int n = sequence.length();
		char base = sequence.charAt(n - 1);
		if (base == 'A' || base == 'T') {
			int i = n - 2;
			while (i >= 0 && sequence.charAt(i) == base) {
				--i;
			}
			return n - i - 1;
		} else {
			return 0;
		}
	}

	private double computeBiasCorrectedWeight(String sequence) {
//sahar debug
//                System.err.println("In computeBiasCorrectedWeight");
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
			Map<String, Object> m = (Map) coordinatesByReference;
			for (Map.Entry<String, Object> e : m.entrySet()) {
				e.setValue(((List<Coord2>) e.getValue()).toArray(new Coord2[0]));
			}
			coordConsumer.run(new ReadCoordinatesBean2((Map) m,
					nr, na, readStarts));
			coordinatesByReference = null;
			readStarts = null;
			nr = 0;
			na = 0;
		}
	}

	@Override
	public Object done() {
//		System.out.println("Global count " + globalCount + " dropped because qual " + droppedBecauseQual + " dropped because ref " + droppedBecauseRef);
		flush();
		return coordConsumer.done();
	}

	private List<Coord2> getCoordinates(String referenceSequenceName) {
		List<Coord2> coordinates = null;
		coordinates = coordinatesByReference.get(referenceSequenceName);
		if (coordinates == null) {
			coordinatesByReference.put(new String(referenceSequenceName),
					coordinates = new LightArray<Coord2>());
		}
		return coordinates;
	}

	private void addCoords(int pos, AlignmentId readId, String cigar,
			List<Coord2> coordinates) {

		int i = 0;
		int n = cigar.length();
		int nCoords = 0;
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
                        switch (cigarSymbol) {
                            case 'S': // ignore the soft clipped ends
                            case 'I': // also inserted bases has no effect on the genome coordinated covered
                                break;
                            case 'M':
//				if ((prevSymbol != 'N') || (prevSymbol != 'D')) {
				if ((prevSymbol != 'N') && (prevSymbol != 'D') && (nCoords > 0)) {
					// have to collapse intervals so we drop the end of the
					// previous interval and only add the end of the new one
                                        // example: if previous element was 'I'; does not create a
                                        // gap in coverage
						nCoords--;
				} else {
					// interval start
					coordBuffer[nCoords++] = pos;
				}

				// interval end
				int p = pos + elemLen - 1;
				coordBuffer[nCoords++] = p;
        			pos += elemLen;
                                break;
                            case 'N'://both correspond to areas in genome not covered by read
                            case 'D': // unless we want to ignore the 'D' gap since it is usually small
        			pos += elemLen;
                                break;
                        }
        		++i;
			prevSymbol = cigarSymbol;
		} while (i < n);
		if (nCoords > 0) {
			Coord2 c = new Coord2(readId, Arrays.copyOf(coordBuffer, nCoords));
			coordinates.add(c);
		}

	}

	private double computeWeight(String referenceSequenceName, int pos,
			String sequence, String cigar, String qualScores) {
		CharSequence chromSeq = genome.getSequence(referenceSequenceName);
		if (chromSeq == null) {
			if (!printedErrChroms.contains(referenceSequenceName)) {
				synchronized (printedErrChroms) {
					if (!printedErrChroms.add(referenceSequenceName)) {
						System.out.println("WARNING: Missing nucleotide sequence for reference sequence "
								+ referenceSequenceName);
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
                double prev_p = 0.0;
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

			char cigarCode = cigar.charAt(i);
			if ((cigarCode != 'S') && (cigarCode != 'N')) {
				for (int a = pos - 1, b = soffset, c = 0; c < elemLen; ++a, ++b, ++c) {
					double p = 0.0;
					if (qualityScores) {
                                                if (cigarCode == 'D')
                                                    p = prev_p;
                                                else
                                                    p = Utils.phredProbability(Utils.fastqToPhred(qualScores.charAt(b)));
					}
					if ((cigarCode != 'I') && (a < 0 || a >= chrom.length())) {
						if (!polyAEnabled) {
							System.out.println("WARNING: alignment at position " + pos + " goes outside sequence " + referenceSequenceName
									+ " which has length " + chrom.length() + ". Ignored");
							return 0.0;
						}
					} else switch (cigarCode) {
                                        
                                           case 'M':
                                                if (Character.toLowerCase(chrom.charAt(a)) == Character.toLowerCase(sequence.charAt(b))) {
                                                    if (qualityScores)
                                                    	quality *= 1 - p;
                                                } else {
                                                    if (qualityScores)
							quality *= p / 3;
						}
                                                break;
                                           case 'I':
                                                    if (qualityScores)
							quality *= p;
                                                    break;
                                            case 'D':
                                                    if (qualityScores)
							quality *= p;  // set to p of the previoous base
                                                    break;
                                        }
					++m;
					if (m > mismatches) {
//						System.out.println("NO you don't!");
						return 0.0;
					}
                                        prev_p = p;
                                 }
                        }
			if (cigarCode == 'M' || cigarCode == 'S' || cigarCode == 'I') { //advance read pointer for M,I,S
				soffset += elemLen;
			}
			if (cigarCode != 'S' && cigarCode != 'I') { // do not advance pos for soft clipped ends or insertions to the genome
				pos += elemLen;                     // advance genome pointer for M,D,N
			}
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
                        char cigarCode = cigar.charAt(i);
			if (cigarCode == 'M' ) {
				basesInsideRepeat += insideAnnotatedRepeat(annotatedRepStarts, annotatedRepEnds, pos,
						pos + elemLen - 1);
			}
			if (cigarCode != 'S' && cigarCode != 'I')  // do not advance pos for soft clipped ends or insertions to the genome
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

	public void setForwardProcess(ParameterRunnable<ReadCoordinatesBean2, ?> coordConsumer) {
		this.coordConsumer = coordConsumer;
	}
}
