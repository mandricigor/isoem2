package edu.uconn.engr.dna.util;


public class SimpleTokenizer {

    protected String line;
    protected int currDelimPos;
    protected int prevDelimPos;
    protected final char delim;
    protected final String delimitators;

    public SimpleTokenizer(char delimitator) {
        this.delim = delimitator;
        this.delimitators = null;
    }

    public SimpleTokenizer(String delimitators) {
        this.delim = 0;
        this.delimitators = delimitators;
    }

    public void setLine(String line) {
        this.line = line;
        this.currDelimPos = -1;
    }

    public String rest() {
        return line.substring(currDelimPos + 1);
    }

    public String currentString() {
        return line.substring(prevDelimPos, currDelimPos);
    }

    public String nextString() {
        skipNext();
        return currentString();
    }

    public int nextInt() {
        skipNext();
        return Utils.parseInt(line, 10, prevDelimPos, currDelimPos);
    }

    public void skipNext(int fieldsToSkip) {
        for (int i = 0; i < fieldsToSkip; ++i)
            skipNext();
    }

    public void skipNext() {
        this.prevDelimPos = this.currDelimPos + 1;
        this.currDelimPos = indexOfNextDelimiter(line, prevDelimPos);
    }

    private int indexOfNextDelimiter(String line, int startIndex) {
        if (delimitators == null) {
            int newIndex = line.indexOf(delim, startIndex);
            if (newIndex == -1) {
                return line.length();
            }
            return newIndex;
        } else {
            while (startIndex < line.length() && -1 == delimitators.indexOf(line.charAt(startIndex)))
                ++startIndex;
            return startIndex;
        }
    }

    public int getLastTokenStart() {
        return prevDelimPos;
    }

    public int getLastTokenEnd() {
        return currDelimPos;
    }
}