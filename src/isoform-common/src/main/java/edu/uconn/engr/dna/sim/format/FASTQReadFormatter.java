package edu.uconn.engr.dna.sim.format;

import edu.uconn.engr.dna.format.TaggedSequences;
import edu.uconn.engr.dna.sim.Read;
import edu.uconn.engr.dna.util.SamUtils;
import edu.uconn.engr.dna.util.Utils;

public class FASTQReadFormatter implements ReadFormatter {

    private TaggedSequences isoformSequences;
    private boolean reverseComplementMinusStrandReads;

    public FASTQReadFormatter(TaggedSequences isoformSequences, boolean reverseComplementMinusStrandReads) {
        this.isoformSequences = isoformSequences;
        this.reverseComplementMinusStrandReads = reverseComplementMinusStrandReads;
    }

    @Override
    public String format(Read read) {
        StringBuilder sb = new StringBuilder();
        sb.append('@');
        sb.append(read.getId());
        sb.append('\n');
        CharSequence seq = isoformSequences.getSequence(read.getIsoformId(),
                read.getReadStart(),
                read.getReadStart() + read.getReadLength() - 1);
        if (read.getStrand() == '-' && reverseComplementMinusStrandReads)
            seq = Utils.reverseComplement(seq);
        sb.append(seq);
        sb.append("\n+\n");
        sb.append(SamUtils.phredToFastq(Utils.arrayOf(read.getReadLength(), (byte) 60)));
        sb.append('\n');
        return sb.toString();
    }

    @Override
    public String getTableHeader() {
        return "";
    }

}
