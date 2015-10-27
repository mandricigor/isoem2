package edu.uconn.engr.dna.isoem.processor;

import edu.uconn.engr.dna.util.ParameterRunnable;
import edu.uconn.engr.dna.util.SimpleTokenizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.uconn.engr.dna.isoem.processor.SamFlagsInterpreter.isFirstReadInPair;
import static edu.uconn.engr.dna.isoem.processor.SamFlagsInterpreter.isReadPaired;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jul 12, 2010
 * Time: 9:21:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class KmerCounter implements ParameterRunnable<List<String>, Map<String, Integer>[]> {
    private static final int SEEN_NONE = 0;
    private static final int FIRST_FLAG = 1;
    private static final int SECOND_FLAG = 2;
    private static final int SEEN_BOTH = 3;
    private static final char delim = '\t';

    private int kmerLength;
    private Map<String, Integer>[] kmerCount;
    private ParameterRunnable<List<String>, Void> forwardProcess;
    private SimpleTokenizer tokenizer;


    public KmerCounter(Integer kmerLength) {
        this.kmerLength = kmerLength;
    }

    public KmerCounter(int kmerLength,
                       ParameterRunnable<List<String>, Void> forwardProcess) {
        this.kmerLength = kmerLength;
        this.forwardProcess = forwardProcess;
    }

    @Override
    public void run(List<String> lines) {
        System.out.println("in KmerCounter   lines length =");
        if (tokenizer == null)
            tokenizer = new SimpleTokenizer(delim);

        int seenRead = SEEN_NONE;
        for (String line : lines) {
            if (line == null) {
                // new read
                seenRead = SEEN_NONE;
                continue;
            } else if (seenRead == SEEN_BOTH)
                continue;

            try {
                tokenizer.setLine(line);
                tokenizer.skipNext(); // skip read name
                int flags = tokenizer.nextInt();
                if (isReadPaired(flags)) {
                    int read = isFirstReadInPair(flags) ? FIRST_FLAG : SECOND_FLAG;
                    if ((seenRead & read) > 0)
                        continue;
                    else
                        seenRead |= read;
                } else
                    seenRead = SEEN_BOTH;

                tokenizer.skipNext(7); // skip <RNAME> <POS> <MAPQ> <CIGAR> <MRNM> <MPOS> <ISIZE>
                String sequence = tokenizer.nextString();
                count(sequence);
            } catch (RuntimeException e) {
                System.err.println("ERROR in line" + line);
                throw e;
            }
        }
        if (forwardProcess != null)
            forwardProcess.run(lines);
    }

    private void count(String sequence) {
        count(sequence, 0, 2);
//        int end = Math.min(29, sequence.length() - kmerLength + 1);
        int end = sequence.length()/2 + 3;
        count(sequence, end - 6, end);
    }

    private void count(String sequence, int start, int end) {
        if (kmerCount == null)
            kmerCount = new Map[sequence.length()];
        for (; start < end; ++start) {
            String kmer = sequence.substring(start, start + kmerLength);
            if (kmerCount[start] == null)
                kmerCount[start] = new HashMap<String, Integer>();
            increaseCount(kmer, kmerCount[start]);
        }
    }

    private void increaseCount(String kmer, Map<String, Integer> kmerCountMap) {
        Integer c = kmerCountMap.get(kmer);
        if (c == null)
            kmerCountMap.put(new String(kmer), 1);
        else
            kmerCountMap.put(kmer, c + 1);
    }

    @Override
    public Map<String, Integer>[] done() {
        if (forwardProcess != null)
            forwardProcess.done();
        return kmerCount;
    }

    public Map<String, Integer>[] getKmerCount() {
        return kmerCount;
    }

}
