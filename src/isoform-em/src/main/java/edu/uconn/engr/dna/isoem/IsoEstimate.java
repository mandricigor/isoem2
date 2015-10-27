/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uconn.engr.dna.isoem;

/**
 *
 * @author Sahar
 */
public class IsoEstimate {
    
    final double count;
    final double freq;

    public double getCount() {
        return count;
    }

    public double getFreq() {
        return freq;
    }

    public IsoEstimate(double count, double freq) {
        this.count = count;
        this.freq = freq;
    }


}
