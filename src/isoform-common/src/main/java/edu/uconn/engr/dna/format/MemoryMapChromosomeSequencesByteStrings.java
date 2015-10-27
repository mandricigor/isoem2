package edu.uconn.engr.dna.format;

import edu.uconn.engr.dna.util.ByteString;

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
public class MemoryMapChromosomeSequencesByteStrings extends AbstractTaggedSequences {
    private static final byte CR = 015;
    private static final byte LF = 012;

    public MemoryMapChromosomeSequencesByteStrings(String fileName) throws IOException {
        File file = new File(fileName);
        // Create a read-only memory-mapped file
        FileChannel roChannel =
                new RandomAccessFile(file, "r").getChannel();

        StringBuilder seqName = new StringBuilder();
        ByteString sequence = null;

        boolean accSeqName = false;
        final long size = roChannel.size();
        final long chunkSize = Integer.MAX_VALUE;
        for (long pos = 0; pos < size; pos += chunkSize) {
            ByteBuffer memMapBuffer =
                    roChannel.map(FileChannel.MapMode.READ_ONLY,
                            pos, Math.min(chunkSize, size - pos));
            while (memMapBuffer.hasRemaining()) {
                byte b = memMapBuffer.get();
                if (b == '>') {
                    accSeqName = true;
                } else if (b == CR || b == LF) {
                    if (accSeqName == true) {
                        sequencesByTag.put(seqName.toString().trim().intern(),
                                sequence = new ByteString());
                        seqName = new StringBuilder();
                    }
                    accSeqName = false;
                } else {
                    char c = (char) b;
                    if (accSeqName)
                        seqName.append(c);
                    else if (!Character.isWhitespace(c))
                        sequence.add(b);
                }
            }
        }
//        for (Map.Entry<CharSequence, CharSequence> e : sequencesByTag.entrySet())
//            e.setValue(e.getValue().toString());
    }
}