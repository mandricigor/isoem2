package edu.uconn.engr.dna.isoem.processor;

import edu.uconn.engr.dna.format.Isoform;
import edu.uconn.engr.dna.format.Isoforms;
import edu.uconn.engr.dna.isoem.Coord2;
import edu.uconn.engr.dna.isoem.IsoformList;
import edu.uconn.engr.dna.isoem.ReadCoordinatesBean2;
import edu.uconn.engr.dna.isoem.alignment.AlignmentId;
import edu.uconn.engr.dna.util.ParameterRunnable;
import edu.uconn.engr.dna.util.Utils;
import edu.uconn.engr.dna.util.WeightedIntervals;

import java.util.HashMap;
import java.util.Map;

/**
 * User: marius
 * Date: Jun 19, 2010
 * Time: 7:26:56 PM
 */
public class CoordToIsoformCoverageParameterRunnable
		implements ParameterRunnable<ReadCoordinatesBean2, Map<String, WeightedIntervals>> {

	private Map<String, Double> isoFrequencies;
	private Isoforms isoforms;
	private ParameterRunnable<ReadCoordinatesBean2, IsoformList[]> coordToIsoListAlgo;
	private Map<String, WeightedIntervals> isoCoverage;
	private int readLength = -1;
	public static int nReads;

	public CoordToIsoformCoverageParameterRunnable(
			Map<String, Double> isoFrequencies,
			Isoforms isoforms,
			ParameterRunnable<ReadCoordinatesBean2, IsoformList[]> coordToIsoListAlgo) {
		this.isoFrequencies = isoFrequencies;
		this.isoforms = isoforms;
		this.coordToIsoListAlgo = coordToIsoListAlgo;
		this.isoCoverage = new HashMap<String, WeightedIntervals>();
	}

	@Override
	public void run(ReadCoordinatesBean2 bean) {
//        System.out.println("in CoordToIsoformCoverageParameterRunnable isos length =");
		coordToIsoListAlgo.run(bean);
		IsoformList[] isoLists = coordToIsoListAlgo.done();

		for (Coord2[] readCoords : bean.getCoordinates().values()) {
			for (Coord2 c : readCoords) {
				if (readLength == -1) {
					readLength = computeReadLength(c);
				}

				Object e = c.getId();
				AlignmentId alignmentId = (AlignmentId) e;
				int id = alignmentId.getId();
				IsoformList isoList = isoLists[id];
				if (isoList != null) {
					synchronized(CoordToIsoformCoverageParameterRunnable.class) {
						nReads++;
					}
					double sum = 0;
					for (IsoformList.Entry entry : isoList.entrySet()) {
						double w = entry.getValue();
						if (isoFrequencies.containsKey(entry.getKey())) {
							sum += w * isoFrequencies.get(entry.getKey());
						} else {
							System.out.println("Warning: Missing isoform frequency for " + entry.getKey() + " !");
						}
					}
					if (sum >= Utils.EPS) {
						for (IsoformList.Entry entry : isoList.entrySet()) {
							double w = entry.getValue();
							Isoform isoform = isoforms.get(entry.getKey());
							if (isoFrequencies.containsKey(entry.getKey())) {
								increaseCoverage(isoform, c, isoList.getMultiplicity() * w * isoFrequencies.get(entry.getKey()) / sum);
							}
						}
					}
				}
			}
		}
	}

	private int computeReadLength(Coord2 c) {
		int[] coords = c.getCoords();
		int rl = 0;
		for (int i = 0; i < coords.length; i += 2) {
			rl += coords[i + 1] - coords[i] + 1;
		}
		return rl;
	}

	private void increaseCoverage(Isoform isoform, Coord2 c, double count) {
		WeightedIntervals wi = isoCoverage.get(isoform.getName());
		if (wi == null) {
			isoCoverage.put(isoform.getName(), wi = new WeightedIntervals(isoform.length()));
		}
		int end = isoform.getExons().convertGenomeToIsoCoord(c.getEnd());
		if (end - readLength < 0) {
			int[] coords = c.getCoords();
			StringBuilder sb = new StringBuilder("[");
			for (int i = 0; i < coords.length; ++i) {
				sb.append(coords[i]);
				sb.append(i == coords.length - 1 ? "]" : ",");
			}
			System.out.printf("WARNING no match: read coords %s isoform %s exons %s readLength %d\n",
					sb.toString(),
					isoform.getName(),
					isoform.getExons().toString(),
					readLength);
		} else {
			wi.add(end - readLength, end, count);
		}
	}

	@Override
	public Map<String, WeightedIntervals> done() {
		return isoCoverage;
	}
}
