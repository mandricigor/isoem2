package edu.uconn.engr.dna.isoem;

import edu.uconn.engr.dna.util.DefaultMultiMap;
import edu.uconn.engr.dna.util.MultiMap;

import java.util.Collection;

public class SortedReadsRecentReadsCache implements RecentReadsCache {
    private MultiMap<Integer, AlignmentBean> recentAlignments = new DefaultMultiMap<Integer, AlignmentBean>();
    private CharSequence currentRead;

    @Override
    public void clear() {
        recentAlignments.clear();
    }

    public boolean add(CharSequence readName,
                       CharSequence referenceSequenceName, int alignmentStart, int matePosition,
                       CharSequence alignmentCigar, CharSequence mateCigar) {

        if (alignmentStart > matePosition) {
            return add(readName, referenceSequenceName, matePosition, alignmentStart, mateCigar, alignmentCigar);
        }

        if (!readName.equals(currentRead)) {
            recentAlignments.clear();
            currentRead = readName;
        }

        AlignmentBean ab = new AlignmentBean(referenceSequenceName, matePosition, alignmentCigar, mateCigar);

        Collection<AlignmentBean> col = recentAlignments.get(alignmentStart);
        if (col == null || col.isEmpty() || !col.contains(ab)) {
            recentAlignments.put(alignmentStart, ab);
            return false;
        } else {
            return true;
        }
    }


    private static class AlignmentBean {
        private final CharSequence referenceSequenceName;
        private final int matePosition;
        private final CharSequence alignmentCigar;
        private final CharSequence mateCigar;

        public AlignmentBean(CharSequence referenceSequenceName, int matePosition,
                             CharSequence alignmentCigar, CharSequence mateCigar) {
            this.referenceSequenceName = referenceSequenceName;
            this.matePosition = matePosition;
            this.alignmentCigar = alignmentCigar;
            this.mateCigar = mateCigar;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime
                    * result
                    + ((alignmentCigar == null) ? 0 : alignmentCigar.hashCode());
            result = prime * result
                    + ((mateCigar == null) ? 0 : mateCigar.hashCode());
            result = prime * result + matePosition;
            result = prime
                    * result
                    + ((referenceSequenceName == null) ? 0
                    : referenceSequenceName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            AlignmentBean other = (AlignmentBean) obj;
            if (alignmentCigar == null) {
                if (other.alignmentCigar != null)
                    return false;
            } else if (!alignmentCigar.equals(other.alignmentCigar))
                return false;
            if (mateCigar == null) {
                if (other.mateCigar != null)
                    return false;
            } else if (!mateCigar.equals(other.mateCigar))
                return false;
            if (matePosition != other.matePosition)
                return false;
            if (referenceSequenceName == null) {
                if (other.referenceSequenceName != null)
                    return false;
            } else if (!referenceSequenceName
                    .equals(other.referenceSequenceName))
                return false;
            return true;
        }


    }

}

