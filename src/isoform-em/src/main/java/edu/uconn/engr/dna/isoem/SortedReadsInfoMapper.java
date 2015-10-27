package edu.uconn.engr.dna.isoem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SortedReadsInfoMapper implements ReadInfoMapper {
    public Map<Integer, List<String>> readsByRefSeqAndPosition1;
    public Map<Integer, List<String>> readsByRefSeqAndPosition2;

    public SortedReadsInfoMapper() {
	readsByRefSeqAndPosition1 = new HashMap<Integer, List<String>>();
	readsByRefSeqAndPosition2 = new HashMap<Integer, List<String>>();
    }

    @Override
    public void clear() {
	readsByRefSeqAndPosition1.clear();
	readsByRefSeqAndPosition2.clear();
    }

    @Override
    public void put(String referenceSequenceName, int position,
	    boolean firstReadInPair, String cigar) {
	if (firstReadInPair) {
	    put(referenceSequenceName, position, cigar,
		    readsByRefSeqAndPosition1);
	} else {
	    put(referenceSequenceName, position, cigar,
		    readsByRefSeqAndPosition2);
	}
    }

    private void put(String referenceSequenceName, int pos, String cigar,
	    Map<Integer, List<String>> refSeqAndCigarByPos) {
	List<String> refSeqAndCigar = refSeqAndCigarByPos.get(pos);
	if (refSeqAndCigar == null) {
	    refSeqAndCigarByPos.put(pos,
		    refSeqAndCigar = new LinkedList<String>());
	}
	refSeqAndCigar.add(referenceSequenceName);
	refSeqAndCigar.add(cigar);
    }

    @Override
    public String remove(String referenceSequenceName, int position,
	    boolean firstReadInPair) {
	if (firstReadInPair) {
	    return remove(referenceSequenceName, position,
		    readsByRefSeqAndPosition1);
	} else {
	    return remove(referenceSequenceName, position,
		    readsByRefSeqAndPosition2);
	}
    }

    private String remove(String referenceSequenceName, int position,
	    Map<Integer, List<String>> refSeqAndCigarByPosition) {
	List<String> refSeqAndCigarList = refSeqAndCigarByPosition
		.get(position);
	if (refSeqAndCigarList == null) {
	    return null;
	}
	for (Iterator<String> iterator = refSeqAndCigarList.iterator(); iterator
		.hasNext();) {
	    String refSeq = iterator.next();
	    if (refSeq.equals(referenceSequenceName)) {
		iterator.remove();
		String cigar = iterator.next();
		iterator.remove();
		return cigar;
	    }
	}
	return null;
    }
}
