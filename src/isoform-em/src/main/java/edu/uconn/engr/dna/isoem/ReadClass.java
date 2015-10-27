package edu.uconn.engr.dna.isoem;

public class ReadClass {

    private int size;

    private final IsoformList isoforms;

    public ReadClass(int size, IsoformList isoforms) {
	this.size = size;
	this.isoforms = isoforms;
    }

    /**
     * Returns the number of reads in this class
     * 
     * @return the number of reads in this class
     */
    public int getSize() {
	return size;
    }

    /**
     * Returns the id of the isoforms which are compatible with all the reads in
     * this class
     * 
     * @return a collection of strings representing the ids of the isoforms with
     *         which all the reads in this class are compatible
     */
    public IsoformList getIsoforms() {
	return isoforms;
    }

    public void setSize(int size) {
	this.size = size;
    }
}
