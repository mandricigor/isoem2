package edu.uconn.engr.dna.format;

import java.io.FileReader;
import java.io.IOException;

public class ChromosomeSequences extends AbstractTaggedSequencesFromFile {

    public ChromosomeSequences(String referenceFilename) throws IOException {
        loadFile(new FileReader(referenceFilename));
    }

    @Override
    protected boolean splitLine(String line, String[] parts) {
        if (line.startsWith(">")) {
            parts[0] = line.substring(1).intern();
            parts[1] = "";
            return true;
        } else {
            parts[0] = line.toUpperCase();
            return false;
        }

    }

    public static void main(String[] args) throws IOException {
        System.out.println("Loading fasta file " +  args[0]);
        long start = System.currentTimeMillis();
        ChromosomeSequences c = new ChromosomeSequences(args[0]);
        System.out.println("Time: " + (System.currentTimeMillis() - start));
        for (CharSequence tag : c.getAllTags()) {
            System.out.println(">" + tag + ":" + c.getSequence(tag).length() + "<");
        }
    }

}
