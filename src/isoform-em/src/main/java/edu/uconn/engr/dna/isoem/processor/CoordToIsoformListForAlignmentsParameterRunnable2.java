package edu.uconn.engr.dna.isoem.processor;

import edu.uconn.engr.dna.format.Isoform;
import edu.uconn.engr.dna.format.Isoforms;
import edu.uconn.engr.dna.isoem.*;
import edu.uconn.engr.dna.isoem.alignment.AlignmentId;
import edu.uconn.engr.dna.isoem.alignment.PairAlignmentId;
import edu.uconn.engr.dna.probability.CumulativeProbabilityDistribution;
import edu.uconn.engr.dna.util.Intervals;
import edu.uconn.engr.dna.util.Pair;
import edu.uconn.engr.dna.util.ParameterRunnable;
import edu.uconn.engr.dna.util.PositionData;

import java.util.*;

/**
 * User: marius
 * Date: Jun 19, 2010
 * Time: 7:26:56 PM
 */
public class CoordToIsoformListForAlignmentsParameterRunnable2
				implements ParameterRunnable<ReadCoordinatesBean2, IsoformList[]> {

	private final static double EPS = 0.000000000001;
	private final static Set<String> printedErrChroms = new HashSet<String>();
	private final Isoforms isoforms;
	private final boolean firstReadFromCodingStrand;
	private final Map<String, Pair<int[], PositionData[]>> sortedIsoCoordsByReference;
	private boolean matePairs;
	private final CumulativeProbabilityDistribution pd;
	private IsoformList[] isoformsForAlignment;
	private ParameterRunnable<IsoformListsBean, ?> processor;
	private int[] fragmentLenForAlignment;
	private final Comparator<Coord2> comparator;
	private final boolean antisense;

	private static final Object lock = new Object();
	public static int totalNReads = 0;

	public CoordToIsoformListForAlignmentsParameterRunnable2(Isoforms isoforms,
					Map<String, Pair<int[], PositionData[]>> sortedIsoCoordsByReference,
					boolean firstReadFromCodingStrand,
					boolean antisense,
					boolean matePairs,
					CumulativeProbabilityDistribution pd) {
		this.isoforms = isoforms;
		this.sortedIsoCoordsByReference = sortedIsoCoordsByReference;
		this.firstReadFromCodingStrand = firstReadFromCodingStrand;
		this.matePairs = matePairs;
		this.pd = pd;
		this.antisense = antisense;
		this.comparator = new Comparator<Coord2>() {

			@Override
			public int compare(Coord2 o1, Coord2 o2) {
				return o1.getStart() - o2.getStart();
			}
		};
	}

	public void setForwardProcess(ParameterRunnable<IsoformListsBean, ?> processor) {
		this.processor = processor;
	}

	@Override
	public void run(ReadCoordinatesBean2 bean) {
		synchronized(lock) {
			totalNReads += bean.getnReads();
		}
		isoformsForAlignment = new IsoformList[bean.getnAlignments()];
		if (pd == null) {
			fragmentLenForAlignment = new int[bean.getnAlignments()];
		}

//        System.out.println("in CoordToIsoformListForAlignmentsParameterRunnable2 length ="+isoformsForAlignment.length);
		for (Map.Entry<String, Coord2[]> entry : bean.getCoordinates().entrySet()) {
			Coord2[] readCoords = entry.getValue();
			Arrays.sort(readCoords, comparator);
			String chrom = entry.getKey();
			Pair<int[], PositionData[]> isoCoords = sortedIsoCoordsByReference.get(chrom);
			if (isoCoords != null) {
				processSortedCoordinates(readCoords, isoCoords);
			} else {
				if (!printedErrChroms.contains(chrom)) {
					synchronized (printedErrChroms) {
						if (!printedErrChroms.add(chrom)) {
							System.out.println("WARNING: No isoforms exist for reference sequence " + chrom);
						}
					}
				}
			}
		}
		if (processor != null) {
                        //populateMaps(isoformsForAlignment);
			processor.run(new IsoformListsBean(isoformsForAlignment, fragmentLenForAlignment,
							bean.getReadStarts()));
		}

	}


        /*
        private void populateMaps(IsoformList[] isos) {
            for (IsoformList iso: isos) {
                ArrayIsoformList aiso = (ArrayIsoformList) iso;
                if (aiso != null) {
                    double[] w = aiso.getWeight();
                    String[] n = aiso.getName();
                    Map<String, ArrayList<Double>> weightMap = aiso.weightMap;
                    for (int i = 0; i < w.length; i ++) {
                        ArrayList<Double> weightList = new ArrayList<Double>();
                        weightList.add(w[i]);
                        weightMap.put(n[i], weightList);
                    }
                }
            }
        }
        */


	private void processSortedCoordinates(Coord2[] rc,
					Pair<int[], PositionData[]> sortedIsoformCoord) {
		if (rc.length == 0 || sortedIsoformCoord == null) {
			return;
		}

		int[] position = sortedIsoformCoord.getFirst();
		PositionData[] positionData = sortedIsoformCoord.getSecond();

		int i = 0;
		for (int k = 0; k < rc.length; ++k) {
			Coord2 c = rc[k];
			int[] readCoords = c.getCoords();
			int rp = readCoords[0];
			while (position[i] < rp) {
				++i;
			}
			if (position[i] > rp) {
				if (i == 0) {
					continue;
				} else {
					--i;
				}
			}
			IsoformList matchingIsos = positionData[i].findMatches(readCoords);
			if (matchingIsos != null) {
				handleRead(c, matchingIsos);
			} else {
//				System.out.print("No match for coordinates ");
//				for (int j = 0; j < readCoords.length; ++j) {
//					System.out.print(readCoords[j] + " ");
//				}
//				System.out.println();
			}
		}
	}

	@Override
	public IsoformList[] done() {
		IsoformList[] result = isoformsForAlignment;
		isoformsForAlignment = null;
		if (processor != null) {
			processor.done();
		}
//		System.out.println("Reads in CoordToIsoformList " + totalNReads);
               System.out.print("");
		return result;
	}

	public void handleRead(Coord2 coord, IsoformList isoList) {
		Object e = coord.getId();
		AlignmentId alignmentId = (AlignmentId) e;
		int id = alignmentId.getId();
		boolean single = !(alignmentId instanceof PairAlignmentId);
		if (single) {
			if (isoList == null) {
				return;
			}
			isoformsForAlignment[id] = handleAlignmentForSingleRead(isoList, alignmentId, coord.getEnd());
                        ((ArrayIsoformList)isoformsForAlignment[id]).readName = alignmentId.getReadName(); // ATTENTION
		} else {
			IsoformList pairIsoformList;
			if (null == (pairIsoformList = isoformsForAlignment[id])) {
				// mate pairs can be on any strand,
				// regular paired reads should point to each other:
				// upstream read should be on the positive strand
				if ((!matePairs && !alignmentId.isUpstreamReadOnPositiveStrand())
								|| isoList == null) {
					isoformsForAlignment[id] = new ArrayIsoformList(new String[0], new double[0]);
				} else {
					// just in case the pair never comes
					isoList.setQualityScore(alignmentId.getQualityScoreWeight());
					isoList.setMultiplicity(alignmentId.getBiasCorrectedWeight());
					isoformsForAlignment[id] = isoList;
				}
				// exit now; waiting for the pair alignment
				return;
			} else {
				if (isoList != null) {
					isoList.intersect(pairIsoformList);
					if (!isoList.isEmpty()) {
						int f = handleAlreadySeenRead(isoList, (PairAlignmentId) alignmentId, coord.getEnd());
						if (pd == null) {
							fragmentLenForAlignment[id] = f;
						}
					}
				}
                                ((ArrayIsoformList)isoList).readName = alignmentId.getReadName(); // ATTENTION
				isoformsForAlignment[id] = isoList;
			}
		}
	}

	private IsoformList handleAlignmentForSingleRead(IsoformList isoList,
					AlignmentId alignmentId,
					int readEnd) {
		isoList.setQualityScore(alignmentId.getQualityScoreWeight());
		isoList.setMultiplicity(alignmentId.getBiasCorrectedWeight());
		for (Iterator<IsoformList.Entry> iterator = isoList.entrySet().iterator(); iterator.hasNext();) {
			IsoformList.Entry entry = iterator.next();
			Isoform isoform = isoforms.get(entry.getKey());
			if (firstReadFromCodingStrand || antisense)  {
				// read must come from coding strand for directional or from opposite strand for antisense
				if (antisense == isSameStrand(alignmentId.isUpstreamReadOnPositiveStrand(), isoform.getStrand())) {
					iterator.remove();
					continue;
				}
			}

			Intervals exons = isoform.getExons();
			int maxFragLen;
			if (alignmentId.isUpstreamReadOnPositiveStrand()) {
				int fragStartInIso = exons.convertGenomeToIsoCoord(alignmentId.getFragmentStart());
				maxFragLen = isoform.length() + 1 - fragStartInIso;
			} else {
				maxFragLen = exons.convertGenomeToIsoCoord(readEnd);
			}

			if (pd == null) {
				entry.setValue((double) -maxFragLen);
			} else {
				double weight = pd.cumulativeLowerTail(maxFragLen);
				if (weight < EPS) {
					iterator.remove();
					continue;
				}
				entry.setValue(weight * alignmentId.getQualityScoreWeight());
			}
		}
		return isoList;
	}

	private int handleAlreadySeenRead(IsoformList isoList,
					PairAlignmentId alignmentId, int fragEnd) {
		if (isoList.isEmpty()) {
			return -1;
		}
		isoList.setQualityScore(alignmentId.getQualityScoreWeight());
		isoList.setMultiplicity(alignmentId.getBiasCorrectedWeight());
		int fragmentLength = -1;
		for (Iterator<IsoformList.Entry> iterator = isoList.entrySet().iterator(); iterator.hasNext();) {
			IsoformList.Entry entry = iterator.next();
			Isoform isoform = isoforms.get(entry.getKey());

			if (firstReadFromCodingStrand || antisense) {
				// first read must come from coding strand second read must come from opposite strand for directional
				// and the other way around for antisense
				if (antisense == (alignmentId.getFirstReadStrand() == isoform.getStrand())) {
					iterator.remove();
					continue;
				}
			}

			fragmentLength = isoform.getExons().getDistanceInIsoformBetween(alignmentId.getFragmentStart(), fragEnd);
//sahar Oct 2015 error with paired end hisat alignments
// rest of fix in is Intervals.java in isoform-common util directory
			if (fragmentLength == -1) {
				fragmentLength = 0;  // the function returns fragmentLength - just in case this is the last isoform; set it to 0, so it would not return -1
				iterator.remove();
				continue;
			}

			if (pd == null) {
				entry.setValue((double) fragmentLength);
			} else {
				double prob = pd.getWeight(fragmentLength, 0);
				if (prob < EPS) {
					iterator.remove();
					continue;
				}
				entry.setValue(prob * alignmentId.getQualityScoreWeight());
			}
		}
		return fragmentLength;
	}

	private boolean isSameStrand(boolean positiveStrand, char strand) {
		return positiveStrand == (strand == '+');
	}
}
