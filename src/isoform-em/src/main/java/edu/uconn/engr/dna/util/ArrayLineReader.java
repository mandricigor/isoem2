package edu.uconn.engr.dna.util;

import java.io.IOException;
import java.io.Reader;

public class ArrayLineReader {

    private final Reader input;
    private final char[] buffer;
    private int bufStart;
    private int bufEnd;
    private final ArrayCharSequence seq;
    private int n;

    public ArrayLineReader(Reader input, char[] buffer) {
	this.input = input;
	this.buffer = buffer;
	this.bufStart = 0;
	this.bufEnd = 0;
	this.n = 0;
	this.seq = new ArrayCharSequence(buffer, 0, 0);
    }

    public String readLine() throws IOException {
	while (n < bufEnd && buffer[n] != '\n') {
	    ++n;
	}
	if (n == bufEnd) {
	    // move useful data at the beginning
	    bufEnd -= bufStart;
	    System.arraycopy(buffer, bufStart, buffer, 0, bufEnd);
	    bufStart = 0;

	    // need to read some more
	    int charsRead = input.read(buffer, bufEnd, buffer.length - bufEnd);
	    if (charsRead == -1) {
		// end of file
		return null;
	    }

	    n = bufEnd;
	    bufEnd += charsRead;
	    while (n < bufEnd && buffer[n] != '\n') {
		++n;
	    }
	    if (n == bufEnd) {
		throw new IllegalStateException(
			"Insufficient buffer size to fit one line");
	    }
	}
	seq.setStart(bufStart);
	seq.setLength(n - bufStart);
	bufStart = ++n;
	return seq.toString();
    }

    public void close() throws IOException {
	input.close();
    }

}
