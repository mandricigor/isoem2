package edu.uconn.engr.dna.util;


public class TokenizerWithReplace extends SimpleTokenizer {

	private StringBuilder newLine;
	private int prevDelimPosInNewLine;

	public TokenizerWithReplace(char delimitator) {
		super(delimitator);
		this.newLine = new StringBuilder();
	}
	
	public void setLine(String line) {
		super.setLine(line);
		this.newLine.delete(0, newLine.length());
		this.prevDelimPosInNewLine = -1;
	}
	
	private void prepareForUpdate() {
    	prevDelimPosInNewLine = newLine.length();
    	if (prevDelimPosInNewLine > 0) {
    		newLine.append(delim);
    	}
	}

	public void update(CharSequence s) {
		prepareForUpdate();
		newLine.append(s);
	}

	public void update(CharSequence s, int start, int end) {
		prepareForUpdate();
    	newLine.append(s, start, end);
	}

	public void update(int i) {
		prepareForUpdate();
    	newLine.append(i);
	}

	public void update(char[] array) {
		prepareForUpdate();
    	newLine.append(array);
	}

	public void replaceNextToken(int newToken) {
		super.skipNext();
		update(newToken);
	}

	public void replaceNextToken(CharSequence newToken) {
		super.skipNext();
		update(newToken);
	}

	public String getNewLine(String optionalAppend) {
		boolean restIsEmpty = currDelimPos+1 >= line.length();
    	if (prevDelimPosInNewLine > 0 && !restIsEmpty) {
    		newLine.append(delim);
    	}
    	if (!restIsEmpty) {
    		newLine.append(line, currDelimPos+1, line.length());
    	}
    	if (optionalAppend != null) {
    		newLine.append(delim);
    		newLine.append(optionalAppend);
    	}
		return newLine.toString();
	}

	public void replaceNextTokenWithReverseComplement() {
		skipNext();
		char[] array = new char[currDelimPos-prevDelimPos];
		line.getChars(prevDelimPos, currDelimPos, array, 0);
		Utils.reverseComplement(array);
		update(array);
	}

	public void replaceNextTokenWithReverse() {
		skipNext();
		char[] array = new char[currDelimPos-prevDelimPos];
		line.getChars(prevDelimPos, currDelimPos, array, 0);
		Utils.reverse(array);
		update(array);
	}

	public void skipNextWithCopy() {
		skipNext();
		update(line, prevDelimPos, currDelimPos);		
	}
	
}
