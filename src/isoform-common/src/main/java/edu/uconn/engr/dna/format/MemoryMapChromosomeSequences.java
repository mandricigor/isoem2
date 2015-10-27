package edu.uconn.engr.dna.format;

import edu.uconn.engr.dna.util.CompressedString;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jul 19, 2010
 * Time: 9:30:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class MemoryMapChromosomeSequences extends AbstractTaggedSequences {
    private static final byte CR = 015;
    private static final byte LF = 012;

    public MemoryMapChromosomeSequences(String fileName) throws IOException {
        File file = new File(fileName);
        // Create a read-only memory-mapped file
        FileChannel roChannel =
                new RandomAccessFile(file, "r").getChannel();

        StringBuilder seqName = new StringBuilder();
        CompressedString sequence = null;
//        ByteString sequence2 = null;

//        Map<CharSequence, CharSequence> sequencesByTag2 = new HashMap<CharSequence, CharSequence>();
        
        boolean accSeqName = false;
        final long size = roChannel.size();
        final long chunkSize = Integer.MAX_VALUE;
        for (long pos = 0; pos < size; pos += chunkSize) {
            ByteBuffer memMapBuffer =
                    roChannel.map(FileChannel.MapMode.READ_ONLY,
                            pos, Math.min(chunkSize, size - pos));
            for (int r = memMapBuffer.remaining(); r-- > 0;) {
                byte b = memMapBuffer.get();
                if (b == '>') {
                    accSeqName = true;
                } else if (b == CR || b == LF) {
                    if (accSeqName == true) {
                        sequencesByTag.put(seqName.toString().trim().intern(),
                                sequence = new CompressedString());
//                        sequencesByTag2.put(seqName.toString().trim().intern(),
//                                sequence2 = new ByteString());
                        seqName = new StringBuilder();
                    }
                    accSeqName = false;
                } else {
                    if (accSeqName)
                        seqName.append((char)b);
                    else if (b != ' ' && b != '\t') {
                        sequence.add((byte)Character.toUpperCase(b));
//                        sequence2.add(b);
                    }
                }
            }
        }

/*
        long totalCap = 0;
        long totalUsed = 0;
        for (Map.Entry<CharSequence, CharSequence> entry : sequencesByTag.entrySet()) {
           totalCap += (((CompressedString)entry.getValue()).capacity()+1)/2;
           totalUsed += (((CompressedString)entry.getValue()).length()+1)/2;
           ensureEqual(entry.getKey(), entry.getValue(), sequencesByTag2.get(entry.getKey()));
        }
        System.out.println("Total buffers size " + totalCap);
        System.out.println("Total used size " + totalUsed);
*/


    }

    private void ensureEqual(CharSequence key, CharSequence value, CharSequence value2) {
        if (value.length() != value2.length()) {
            throw new IllegalStateException(key + " : different lengths " + value.length() + " | " + value2.length());
        }
        for (int i = 0; i < value.length(); ++i)
            if (value.charAt(i) != value2.charAt(i)) {
                throw new IllegalStateException(key + " : different chars at pos " + i
                        + " (" + value.charAt(i) + " | " + value2.charAt(i) + ')');
            }
        System.out.println(key + " ok!");
    }
}
