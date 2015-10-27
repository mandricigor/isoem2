package edu.uconn.engr.dna.util;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jul 19, 2010
 * Time: 11:55:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class ByteString implements CharSequence {

    private byte[] buffer;
    private int len;

    public ByteString() {
        buffer = new byte[16];
    }

    @Override
    public char charAt(int index) {
        if ((index < 0) || (index >= len)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return (char) buffer[index];
    }

    public void add(byte b) {
        if (len == buffer.length) {
            buffer = Arrays.copyOf(buffer, 2 * buffer.length);
        }
        buffer[len++] = b;
    }

    @Override
    public int length() {
        return len;
    }

    @Override
    public CharSequence subSequence(int a, int b) {
        throw new IllegalArgumentException("This method is not yet implemented!");
    }

    @Override
    public String toString() {
        throw new IllegalArgumentException("This method is not yet implemented!");
    }
}

