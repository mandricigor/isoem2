package edu.uconn.engr.dna.isoem;


public class ReadInfo<I> {

	private CharSequence cigar;
	private CharSequence referenceSequenceName;
	private I internalId;

	public ReadInfo(CharSequence cigar, CharSequence referenceSequenceName, I internalId) {
		this.cigar = cigar;
		this.referenceSequenceName = referenceSequenceName;
		this.internalId = internalId;
	}

	public CharSequence getReferenceSequenceName() {
		return referenceSequenceName;
	}

	public I getInternalId() {
		return internalId;
	}

	public CharSequence getCigar() {
		return cigar;
	}
	
	
}
