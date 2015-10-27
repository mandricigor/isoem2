package edu.uconn.engr.dna.isoem;

import edu.uconn.engr.dna.util.DefaultMultiMap;
import edu.uconn.engr.dna.util.MultiMap;


public class SortedReadsIdMapper<E, I> implements ReadIdMapper<E, I> {
    public MultiMap<Integer, ReadInfo<I>> readsByPositionAndFlag1;
    public MultiMap<Integer, ReadInfo<I>> readsByPositionAndFlag2;
    public E currentExternalId;
    private int unclaimed;

    @Override
    public void clear() {
        readsByPositionAndFlag1.clear();
        readsByPositionAndFlag2.clear();
    }

    public SortedReadsIdMapper() {
        readsByPositionAndFlag1 = new DefaultMultiMap<Integer, ReadInfo<I>>();
        readsByPositionAndFlag2 = new DefaultMultiMap<Integer, ReadInfo<I>>();
    }


    @Override
    public void put(E externalId, int position, boolean firstReadInPair, I internalId,
                    CharSequence cigar, CharSequence referenceSequenceName) {
        checkIdChange(externalId);
        ReadInfo<I> ri = new ReadInfo<I>(cigar, referenceSequenceName, internalId);
        if (firstReadInPair) {
            readsByPositionAndFlag1.put(position, ri);
        } else {
            readsByPositionAndFlag2.put(position, ri);
        }
    }


    @Override
    public ReadInfo<I> remove(E externalId, int position, boolean firstReadInPair) {
        checkIdChange(externalId);
        if (firstReadInPair) {
            return readsByPositionAndFlag1.removeOne(position);
        } else {
            return readsByPositionAndFlag2.removeOne(position);
        }
    }

    private void checkIdChange(E externalId) {
        if (!externalId.equals(currentExternalId)) {
            unclaimed += readsByPositionAndFlag1.size() + readsByPositionAndFlag2.size();
            readsByPositionAndFlag1.clear();
            readsByPositionAndFlag2.clear();
            currentExternalId = externalId;
        }
    }

    public int unclaimed() {
        return unclaimed;
    }

}
