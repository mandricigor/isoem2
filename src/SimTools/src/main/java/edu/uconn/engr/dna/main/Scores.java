/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uconn.engr.dna.main;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 *
 * @author sahar
 */
public class Scores {
    private ArrayList<PositionScoreFreq> scoreList;
    private int maxCapacity;

      public Scores(int readLength) {
          maxCapacity = readLength;
          scoreList = new ArrayList<PositionScoreFreq>(maxCapacity);

      }
    public boolean full() {
        return (maxCapacity == scoreList.size());
    }

    public void add(PositionScoreFreq score) {
        scoreList.add(score);
    }

    public ListIterator rIterator() {
        return scoreList.listIterator();
    }


}
