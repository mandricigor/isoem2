package edu.uconn.engr.dna.format;

import edu.uconn.engr.dna.util.Intervals;
import edu.uconn.engr.dna.util.Utils;

public class IsoformSequencesFromGenome extends AbstractTaggedSequences {

	
	public IsoformSequencesFromGenome(TaggedSequences genomeSequence, 
			Isoforms isoforms) {
		for (Isoform isoform : isoforms.isoformIterator()) {
			CharSequence sequence = genomeSequence.getSequence(isoform.getChromosome());
			if (sequence == null) {
				System.out.println("no sequence found for chromosome " + isoform.getChromosome() 
					+ "; isoform " + isoform.getName() + " ignored");
				continue;
			}
			Intervals intervals = isoform.getExons();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < intervals.size(); ++i) {
				sb.append(sequence.subSequence((int)intervals.getStart(i)-1, 
						(int)intervals.getEnd(i)));
			}
			String isoSequence;
			if (isoform.getStrand() == '+') {
				isoSequence = sb.toString();
			} else {
				isoSequence = Utils.reverseComplement(sb);
			}
			sequencesByTag.put(isoform.getName(), isoSequence);
		}
	}

}
