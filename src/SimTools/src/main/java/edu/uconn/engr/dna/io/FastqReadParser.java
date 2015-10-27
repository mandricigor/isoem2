/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uconn.engr.dna.io;


import edu.uconn.engr.dna.main.Read;
import edu.uconn.engr.dna.main.Reads;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import edu.uconn.engr.dna.probability.CustomWeightItemPicker;

/**
 *
 * @author sahar
 */
public class FastqReadParser extends AbstractParser implements Parser{

    private Reads reads;
    private static int lineCounter;
    private static long readCounter;
    private static String readID;
    private static String sequence;
    private static String line3;
    private static String qScores;
    private FileInputStream inputStream;
    private FileOutputStream outputStream;
    private Random randomNumberGenerator;
    //ArrayList<CustomWeightItemPicker> allPositionScorePicker;
    private ArrayList<CustomWeightItemPicker> matchPositionScorePicker;
    private ArrayList<CustomWeightItemPicker> mismatchPositionScorePicker;
    private ArrayList<CustomWeightItemPicker> positionMismatchRatePicker;
        

    
    public FastqReadParser(ArrayList<CustomWeightItemPicker> matchPositionScorePicker,ArrayList<CustomWeightItemPicker> mismatchPositionScorePicker,ArrayList<CustomWeightItemPicker> positionMismatchRatePicker, Random randomNumberGenerator) {
        this.reads = new Reads();
        this.positionMismatchRatePicker = positionMismatchRatePicker;
        this.mismatchPositionScorePicker = mismatchPositionScorePicker;
        this.matchPositionScorePicker = matchPositionScorePicker;
        this.randomNumberGenerator = randomNumberGenerator;
        lineCounter = 1;
        readCounter = 1;
    }

@Override
    public Reads parse(String inFileName) throws Exception {
        String outFileName = inFileName + ".Error";
        String commentMarker = " ";;
        inputStream = new FileInputStream(inFileName);
        outputStream = new FileOutputStream(outFileName);
        super.setCommentMarker(commentMarker);
	super.read(inputStream);
	return reads;
}


@Override
    public void processLine(String line) throws Exception {
	if (line.isEmpty()) {
		return ;
	}
        int x = 0;
        if (readCounter== 959625)
            readCounter = readCounter + x;
        switch (lineCounter){
            case 1:
                if (line.equals("+"))
                        readID = new String ("Errorrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr");
                readID = new String(line);
                break;
            case 2:
                sequence = new String(line);
                break;
            case 3:
                line3 = new String (line);
                break;
            case 4:
                qScores = new String(line);
                break;
        }
        lineCounter++;
        if (lineCounter > 4){
            readID = fresh(readID);
            Read read = new Read(readID,sequence,line3,qScores);
            read.insertError(matchPositionScorePicker,mismatchPositionScorePicker,positionMismatchRatePicker,randomNumberGenerator);
            if (reads.full()) {
                writeReads();
            }
            reads.add(read);
            lineCounter = 1;
            readCounter++;
    }

}

protected void writeReads() throws Exception {
    Read currentRead;
    ListIterator readsIterator = reads.rIterator();
    while (readsIterator.hasNext()) {
        currentRead = (Read) readsIterator.next();
        outputStream.write((currentRead.getReadID()+"\n").getBytes(), 0, currentRead.getReadID().length()+1);
        outputStream.write((currentRead.getSequence()+"\n").getBytes(), 0, currentRead.getSequence().length()+1);
        outputStream.write((currentRead.getLine3()+"\n").getBytes(), 0, currentRead.getLine3().length()+1);
        outputStream.write((currentRead.getQScores()+"\n").getBytes(), 0, currentRead.getQScores().length()+1);
        readsIterator.remove();
    }
    outputStream.flush();
}


protected void handleException(String line, Exception e) {
	System.err.println("Invalid file format!");
	e.printStackTrace();
}

}
