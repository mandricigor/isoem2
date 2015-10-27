package edu.uconn.engr.dna.sim.format;

/**
 * User: marius
 * Date: Jul 28, 2010
 */
public class FixedNamingConvention implements NamingConvention {
    private String[] fileNames;

    public FixedNamingConvention(String... fileNames) {
        this.fileNames = fileNames;
    }

    @Override
    public String[] getFileNames() {
        return fileNames;
    }
}
