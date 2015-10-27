/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uconn.engr.dna.main;

/**
 *
 * @author sahar
 */

//import java.util.ArrayList;
import java.util.*;

public class Reads {

    private ArrayList<Read> readList;
    private int maxCapacity = 1000;

    public Reads() {
      readList = new ArrayList<Read>(maxCapacity);
    }

    public boolean full() {
        return (maxCapacity == readList.size());
    }

    public void add(Read read) {
        readList.add(read);
    }

    public ListIterator rIterator() {
        return readList.listIterator();
    }


}
