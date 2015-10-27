package edu.uconn.engr.dna.util;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jul 19, 2010
 * Time: 11:55:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class CompressedString implements CharSequence {

    private byte[] buffer;
    private int buffCapacity;

    private int len;
    private char[] byteCodeToChar = new char[16];
    private byte[] byteCode = new byte[256];
    private byte codePoints;

    public CompressedString() {
        buffer = new byte[10];
        buffCapacity = 2 * buffer.length;
    }

    @Override
    public char charAt(int index) {
        if ((index < 0) || (index >= len)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        int byteIndex = index>>1;
        if ((index & 1) == 0) {
            return byteCodeToChar[buffer[byteIndex] & 0xF];
        } else {
            return byteCodeToChar[(buffer[byteIndex] & 0xF0) >> 4];
        }
    }

    public void add(byte b) {
        if (len == buffCapacity) {
            buffer = Arrays.copyOf(buffer, 2 * buffer.length);
            buffCapacity = 2 * buffer.length;
        }
        byte bc = byteCode[b];
        if (bc == 0) {
            byteCode[b] = bc = ++codePoints;
            byteCodeToChar[bc] = (char)b;
        }

        buffer[len>>1] |= (bc << ((len & 1)<<2)) ;

//        if ((len & 1) == 1) {
//             odd number -> least significant half
//            buffer[byteIndex] |= bc;
//        } else {
//             even number -> most significant half
//            buffer[byteIndex] |= bc << 4;
//        }
        len++;
    }

    @Override
    public int length() {
        return len;
    }

    @Override
    public CharSequence subSequence(int a, int b) {
        StringBuilder sb = new StringBuilder(b-a);
        for (int i = a; i < b; ++i)
            sb.append(charAt(i));
        return sb;
    }

    @Override
    public String toString() {
        return subSequence(0, length()).toString();
    }

}