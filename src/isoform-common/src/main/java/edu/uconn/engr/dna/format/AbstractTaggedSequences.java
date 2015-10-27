package edu.uconn.engr.dna.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class AbstractTaggedSequences  implements TaggedSequences {
	protected Map<CharSequence, CharSequence> sequencesByTag;

	public AbstractTaggedSequences()  {
		sequencesByTag = new LinkedHashMap<CharSequence, CharSequence>();
	}
	
	@Override
	public CharSequence getSequence(CharSequence tag, int start, int end) {
		CharSequence sequence = sequencesByTag.get(tag);
		if (sequence == null) {
			return null;
		}
		
		// adjust coordinates: start and end are 1 based, with inclusive end 
		// but the substring needs 0-based start, with exclusive end
		return sequence.subSequence(start-1, end);
	}

	@Override
	public CharSequence getSequence(CharSequence tag) {
		return sequencesByTag.get(tag);
	}
	
	@Override
	public Collection<CharSequence> getAllTags() {
		return new ArrayList<CharSequence>(sequencesByTag.keySet());
	}

	@Override
	public CharSequence remove(CharSequence tag) {
		return sequencesByTag.remove(tag);
	}

	@Override
	public CharSequence put(CharSequence tag, CharSequence seq) {
		return sequencesByTag.put(tag, seq);
	}
}
