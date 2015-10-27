/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uconn.engr.dna.main;

import edu.uconn.engr.dna.common.Utils;
import java.util.ArrayList;
import java.util.Random;
import edu.uconn.engr.dna.probability.CustomWeightItemPicker;

/**
 *
 * @author sahar
 */

public class Read {
    private String readID;
    private String sequence;
    private String line3;
    private String qScores;

    public Read (String readID,String sequence, String line3,String qScores) {
        this.setReadID(readID);
        this.setSequence(sequence);
        this.setLine3(line3);
        this.setQScores(qScores);
}
    public String getReadID() {
	if(readID!=null) {
		return new String(readID);
	}
	return null;
    }
    public String getSequence() {
	if(sequence!=null) {
		return new String(sequence);
	}
	return null;
    }
    public String getLine3() {
	if(line3!=null) {
		return new String(line3);
	}
	return null;
    }
    public String getQScores() {
	if(qScores!=null) {
		return new String(qScores);
	}
	return null;
    }
    public void setReadID(String readID) {
	if(readID == null) {
		this.readID = null;
	} else {
		this.readID = new String(readID);
	}
    }

    public void setSequence(String sequence) {
	if(sequence == null) {
		this.sequence = null;
	} else {
		this.sequence = new String(sequence);
	}
    }

    public void setLine3(String line3) {
	if(line3 == null) {
		this.line3 = null;
	} else {
		this.line3 = new String(line3);
	}
    }

    public void setQScores(String qScores) {
	if(qScores == null) {
		this.qScores = null;
	} else {
		this.qScores = new String(qScores);
	}
    }


    public void insertError(ArrayList<CustomWeightItemPicker> matchPositionScorePicker,ArrayList<CustomWeightItemPicker> mismatchPositionScorePicker,ArrayList<CustomWeightItemPicker> positionMismatchRatePicker, Random randomNumberGenerator) {
        StringBuffer modifiedScores = new StringBuffer();
        StringBuffer modifiedSequence = new StringBuffer();
        int length = sequence.length();
        for (int i = 0; i < length; i++){
            char iBase = Utils.computeBase(sequence.charAt(i),positionMismatchRatePicker.get(i),randomNumberGenerator);
            if (iBase == sequence.charAt(i)) {// no mismatch
                modifiedScores =  modifiedScores.append((matchPositionScorePicker.get(i)).pick(null));
            }
            else {
                modifiedScores =  modifiedScores.append((mismatchPositionScorePicker.get(i)).pick(null));
            }
            modifiedSequence.append(iBase);
        }
        qScores = modifiedScores.toString();
       sequence = modifiedSequence.toString();
    }

}
