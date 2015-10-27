/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uconn.engr.dna.io;

import edu.uconn.engr.dna.common.SimpleRandomAccessMap;
import edu.uconn.engr.dna.common.Utils;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Random;
import edu.uconn.engr.dna.probability.CustomWeightItemPicker;



public class ScoreDistribParser extends AbstractParser implements Parser{

private static final char COMMENT = '#';
//private ArrayList<SimpleRandomAccessMap> allPositionScoreMap;
private ArrayList<CustomWeightItemPicker> allPositionScorePicker;
private int readPosition;
Random randomNumberGenerator;

private FileInputStream inputStream;

    public ScoreDistribParser(int readLength, Random randomNumberGenerator) {
        //allPositionScoreMap = new ArrayList<SimpleRandomAccessMap>();
        allPositionScorePicker = new ArrayList<CustomWeightItemPicker>(readLength);
        this.randomNumberGenerator = randomNumberGenerator;
        readPosition = 0;

    }
    @Override
    public ArrayList<CustomWeightItemPicker> parse(String inFileName) throws Exception {
        inputStream = new FileInputStream(inFileName);
	super.read(inputStream);
        return allPositionScorePicker;
    }

    protected void processLine(String line) throws Exception {
        SimpleRandomAccessMap<String, Double> scoreFreqMap;
        CustomWeightItemPicker<String> icustomWeightItemPicker;

	if (line.isEmpty() || line.charAt(0) == COMMENT) {
	    return;
	}
        scoreFreqMap = new SimpleRandomAccessMap<String, Double>();
	String[] parts = line.split(" ");
        for (int i = 0; i < Utils.QSCORE_RANGE; i++) {
                    String ScoreCharFreq = getField(parts,i);
                    String ScoreChar = "";
                    ScoreChar += Utils.PhredTofastq(i);
                    scoreFreqMap.put(ScoreChar,Double.parseDouble(ScoreCharFreq));
        }
        icustomWeightItemPicker = new CustomWeightItemPicker<String>(scoreFreqMap,randomNumberGenerator);
        allPositionScorePicker.add(readPosition,icustomWeightItemPicker);
//        allPositionScoreMap.add(readPosition, scoreFreqMap);
        readPosition++;
    }

    protected void handleException(String line, Exception e) {
	System.err.println("Number of frequenvy values does not equal number of possible fastq quality scores");
        System.err.println("line: "+line);
	e.printStackTrace();
}

}
