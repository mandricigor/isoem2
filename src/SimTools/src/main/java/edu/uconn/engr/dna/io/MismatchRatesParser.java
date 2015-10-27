/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uconn.engr.dna.io;

import edu.uconn.engr.dna.common.SimpleRandomAccessMap;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.DataFormatException;
import edu.uconn.engr.dna.probability.CustomWeightItemPicker;

/**
 *
 * @author sahar
 */
public class MismatchRatesParser extends AbstractParser implements Parser{

private static final int readPositionField = 0;
private static final int mismatchRateField = 1;
private static final char COMMENT = '#';
private ArrayList<CustomWeightItemPicker> mismatchRatePicker;
private int readPosition;
Random randomNumberGenerator;
private FileInputStream inputStream;

    public MismatchRatesParser(int readLength, Random randomNumberGenerator) {
        //allPositionScoreMap = new ArrayList<SimpleRandomAccessMap>();
        mismatchRatePicker = new ArrayList<CustomWeightItemPicker>(readLength);
        this.randomNumberGenerator = randomNumberGenerator;
        readPosition = 0;
    }

        @Override
    public ArrayList<CustomWeightItemPicker> parse(String inFileName) throws Exception {
        inputStream = new FileInputStream(inFileName);
	super.read(inputStream);
        return mismatchRatePicker;
    }

   protected void processLine(String line) throws Exception {
        SimpleRandomAccessMap<String, Double> positionMismatchRate;
        CustomWeightItemPicker<String> icustomWeightItemPicker;

	if (line.isEmpty()|| line.charAt(0) == COMMENT) {
	    return;
	}
        positionMismatchRate = new SimpleRandomAccessMap<String, Double>();
	String[] parts = line.split(" ");
        String readPositionStr = getField(parts, readPositionField);
        String mismatchRateStr = getField(parts,mismatchRateField);
        if (Integer.parseInt(readPositionStr) != readPosition+1) {
            throw new DataFormatException();
       }
        positionMismatchRate.put("T",Double.parseDouble(mismatchRateStr));
        positionMismatchRate.put("F",1-Double.parseDouble(mismatchRateStr));
        icustomWeightItemPicker = new CustomWeightItemPicker<String>(positionMismatchRate,randomNumberGenerator);
        mismatchRatePicker.add(readPosition,icustomWeightItemPicker);
        readPosition++;
    }

    protected void handleException(String line, Exception e) {
	System.err.println("Unexpected read position number");
        System.err.println(line);
	e.printStackTrace();
}


}
