package edu.uconn.engr.dna.util;

public class ArrayCharSequence implements CharSequence {

	private char[] buffer;
	private int start;
	private int len;

	public ArrayCharSequence(char[] buffer, int start, int len) {
		this.buffer = buffer;
		this.start = start;
		this.len = len;
	}

	@Override
	public char charAt(int index) {
       if ((index < 0) || (index >= len)) {
            throw new StringIndexOutOfBoundsException(index);
        }
		return buffer[start+index];
	}

	@Override
	public int length() {
		return len;
	}

	@Override
	public CharSequence subSequence(int a, int b) {
		return new String(buffer, start+a, b-a);
	}

	@Override
	public String toString() {
		return new String(buffer, start, len);
	}
	
	public void setStart(int start) {
		this.start = start;
	}

	public void setLength(int n) {
		this.len = n;
	}


	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
		    return true;
		}
		if (anObject instanceof ArrayCharSequence) {
			ArrayCharSequence anotherString = (ArrayCharSequence)anObject;
		    int n = len;
		    if (n == anotherString.len) {
				char v1[] = buffer;
				char v2[] = anotherString.buffer;
				int i = start;
				int j = anotherString.start;
				while (n-- != 0) {
				    if (v1[i++] != v2[j++])
					return false;
				}
				return true;
		    }
		}
		return false;
	}
	
    public int hashCode() {
	    int off = start;
	    int h = 0;
        for (int i = 0; i < len; i++) {
            h = 31*h + buffer[off++];
        }
        return h;
	}

	public int nextIndexOf(char c, int i) {
		while (i < len && c != buffer[start+i]) {
			++i;
		}
		return i;
	}

}
