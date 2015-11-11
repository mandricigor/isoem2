package edu.uconn.engr.dna.isoem;

import edu.uconn.engr.dna.util.LightArray;
import edu.uconn.engr.dna.util.Pair;
import edu.uconn.engr.dna.util.Utils;

import java.util.*;

public class SortedReadsInfoMap {

	private Map<Integer, List<ReadInfo>> readsByRefSeqAndPosition1;
	private Map<Integer, List<ReadInfo>> readsByRefSeqAndPosition2;

	public SortedReadsInfoMap() {
		readsByRefSeqAndPosition1 = new HashMap<Integer, List<ReadInfo>>();
		readsByRefSeqAndPosition2 = new HashMap<Integer, List<ReadInfo>>();
	}

	public boolean isEmpty() {
		return readsByRefSeqAndPosition1.isEmpty() && readsByRefSeqAndPosition2.isEmpty();
	}

	public Iterable<Pair<Integer, ReadInfo>> getRemaining() {
		List<Pair<Integer, ReadInfo>> l = getPairs(readsByRefSeqAndPosition1);
		l.addAll(getPairs(readsByRefSeqAndPosition2));
		return l;
	}

	private List<Pair<Integer, ReadInfo>> getPairs(Map<Integer, List<ReadInfo>> readsByRefSeqAndPosition) {
		List<Pair<Integer, ReadInfo>> res = new ArrayList<Pair<Integer, ReadInfo>>();
		for (Map.Entry<Integer, List<ReadInfo>> e : readsByRefSeqAndPosition.entrySet()) {
			Integer pos = e.getKey();
			List<ReadInfo> l = e.getValue();
			int n = l.size();
			for (int i = 0; i < n; ++i) {
				ReadInfo ri = l.get(i);
				if (ri != null) {
					res.add(new Pair<Integer, ReadInfo>(pos, ri));
				}
			}
		}
		return res;
	}

	public void clear() {
		readsByRefSeqAndPosition1.clear();
		readsByRefSeqAndPosition2.clear();
	}

	public void put(int position, boolean firstReadInPair, ReadInfo info) {
		if (firstReadInPair) {
			put(info, position, readsByRefSeqAndPosition1);
		} else {
			put(info, position, readsByRefSeqAndPosition2);
		}
	}

	private void put(ReadInfo info, int pos, Map<Integer, List<ReadInfo>> readInfoByPos) {
		List<ReadInfo> readInfoList = readInfoByPos.get(pos);
		if (readInfoList == null) {
			readInfoByPos.put(pos,
							readInfoList = new LightArray<ReadInfo>());
		}
		readInfoList.add(info);
	}

	public ReadInfo remove(String referenceSequenceName, int position,
					boolean firstReadInPair) {
		if (firstReadInPair) {
			return remove(referenceSequenceName, position,
							readsByRefSeqAndPosition1);
		} else {
			return remove(referenceSequenceName, position,
							readsByRefSeqAndPosition2);
		}
	}

	private ReadInfo remove(String referenceSequenceName, int position,
					Map<Integer, List<ReadInfo>> readInfoByPosition) {
		List<ReadInfo> readInfoList = readInfoByPosition.get(position);
		if (readInfoList == null) {
			return null;
		}
		int n = readInfoList.size();
		int nuls = 0;
		for (int i = 0; i < n; ++i) {
			ReadInfo info = readInfoList.get(i);
			if (info != null) {
				if (info.getRefSeq().equals(referenceSequenceName)) {
					readInfoList.set(i, null); // lazy removal
					return info;
				}
			} else {
				++nuls;
			}
		}
		if (nuls > n / 2) { // lazy removal
			Utils.compact(readInfoList);
		}
		return null;
	}

	public static class ReadInfo {

		private final String refSeq;
		private final String cigar;
		private final double quality;
		private final double biasCorrectedWeight;
		private final int flags;
                private final String readName;

		public ReadInfo(String readName, String refSeq, String cigar, double quality,
						double biasCorrectedWeight, int flags) {
                        this.readName = readName;
			this.refSeq = refSeq;
			this.cigar = cigar;
			this.quality = quality;
			this.biasCorrectedWeight = biasCorrectedWeight;
			this.flags = flags;
		}

                public String getReadName() {
                    return readName;
                }

		public double getBiasCorrectedWeight() {
			return biasCorrectedWeight;
		}

		public String getCigar() {
			return cigar;
		}

		public double getQuality() {
			return quality;
		}

		public String getRefSeq() {
			return refSeq;
		}

		public int getFlags() {
			return flags;
		}
	}
}
