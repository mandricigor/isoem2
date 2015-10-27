package edu.uconn.engr.dna.isoem.processor;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jul 12, 2010
 * Time: 9:56:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class SamFlagsInterpreter {
    private static final int READ_PAIRED_FLAG = 0x1;
    private static final int SEQUENCE_UNMAPPED = 0x4;
    private static final int MATE_UNMAPPED = 0x8;
    private static final int READ_STRAND_FLAG = 0x10;
    private static final int MATE_STRAND_FLAG = 0x20;
    private static final int FIRST_OF_PAIR_FLAG = 0x40;
    private static final int readPairedMask = READ_PAIRED_FLAG
            | SEQUENCE_UNMAPPED | MATE_UNMAPPED;

    private final static int readPairedResult = READ_PAIRED_FLAG;

    public static boolean isReadPaired(int flags) {
        return (flags & readPairedMask) == readPairedResult;
    }

    public static boolean isFirstReadInPair(int flags) {
        return (flags & FIRST_OF_PAIR_FLAG) != 0;
    }

    public static boolean isMateOnPositiveStrand(int flags) {
        return (flags & MATE_STRAND_FLAG) == 0;
    }

    public static boolean isAlignmentOnPositiveStrand(int flags) {
        return (flags & READ_STRAND_FLAG) == 0;
    }

}
