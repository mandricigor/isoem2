package edu.uconn.engr.dna.isoem;

import edu.uconn.engr.dna.util.Converter;
import edu.uconn.engr.dna.util.SimpleTokenizer;

public class DuplicatesRemovingConverter implements Converter<String, String> {

    private static final int READ_PAIRED_FLAG = 0x1;
    private static final int SEQUENCE_UNMAPPED = 0x4;
    private static final int MATE_UNMAPPED = 0x8;
    private static final int FIRST_OF_PAIR_FLAG = 0x40;

    private ReadIdMapper<String, String> readIdMap;
    private RecentReadsCache recentReadCache;
    private SimpleTokenizer tokenizer;

    public DuplicatesRemovingConverter() {
        this.readIdMap = new SortedReadsIdMapper<String, String>();
        this.recentReadCache = new SortedReadsRecentReadsCache();
        this.tokenizer = new SimpleTokenizer('\t');
    }

    public void clear() {
        recentReadCache.clear();
        readIdMap.clear();
    }

    public String convert(String line) {
        if (line == null) {
            return null;
        }
        tokenizer.setLine(line);
        String readName = tokenizer.nextString();
        int flags = tokenizer.nextInt();
        String referenceSequenceName = tokenizer.nextString(); // (chromosome)
        int alignmentStart = tokenizer.nextInt();
        tokenizer.skipNext(); // skip <MAPQ>
        String cigarString = tokenizer.nextString();
        tokenizer.skipNext(); // skip <MRNM>
        int matePosition = tokenizer.nextInt();

        boolean readPaired = ((flags & READ_PAIRED_FLAG) != 0)
                && ((flags & SEQUENCE_UNMAPPED) == 0)
                && ((flags & MATE_UNMAPPED) == 0);

        boolean firstReadInPair = (flags & FIRST_OF_PAIR_FLAG) != 0;

        ReadInfo<String> mateInfo;
        if (readPaired) {
            // search for the mate
            mateInfo = readIdMap.remove(readName, matePosition, !firstReadInPair);
            if (mateInfo == null) {
                // this read expects a mate
                readIdMap.put(readName, alignmentStart, firstReadInPair, line,
                        cigarString, referenceSequenceName);
                return null;
            } else {
                if (recentReadCache.add(readName, referenceSequenceName,
                        alignmentStart, matePosition,
                        cigarString, mateInfo.getCigar())) {
                    return null;
                } else {
                    // not seen before, output both reads in the pair
                    return line + '\n' + mateInfo.getInternalId();
                }
            }
        } else {
            // unpaired
            if (recentReadCache.add(readName, referenceSequenceName,
                    alignmentStart, Integer.MAX_VALUE,
                    cigarString, null)) {
                return null;
            } else {
                // not seen before
                return line;
            }
        }
    }

}
