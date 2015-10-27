/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package edu.uconn.engr.dna.main;


import edu.uconn.engr.dna.io.FastqReadParser;
//import io.*;
import edu.uconn.engr.dna.io.MismatchRatesParser;
import edu.uconn.engr.dna.io.ScoreDistribParser;
import edu.uconn.engr.dna.probability.CustomWeightItemPicker;
import java.util.ArrayList;
import java.util.Random;
/**
 *
 * @author sahar
 */
public class Simulator {
private String fatqFileName;
private int readLength;
//private int randomSeed;
private Random randomNumberGenerator;
private String scoreDitributionBaseFileName;
private String mismatchRatesFileName;
//private Read tRead;
private FastqReadParser fastqParser;
//private ScoreDistribParser scoreDistribParser;
private ArrayList<CustomWeightItemPicker> matchPositionScorePicker;
private ArrayList<CustomWeightItemPicker> mismatchPositionScorePicker;
private ArrayList<CustomWeightItemPicker> positionMismatchRatePicker;
//private ArrayList<CustomWeightProbabilityDistribution> PositionScoreDistrib;
//private ArrayList<SimpleRandomAccessMap> matchPositionScoreMap;
//private ArrayList<SimpleRandomAccessMap> mismatchPositionScoreMap;
//private ArrayList<SimpleRandomAccessMap> mismatchRateDistribution;



public Simulator(String fatqFileName, int readLength, String scoreDitributionBaseFileName, String mismatchRatesFileName, int randomSeed) {
        this.fatqFileName = fatqFileName;
        this.readLength = readLength;
        this.scoreDitributionBaseFileName = scoreDitributionBaseFileName;
        this.mismatchRatesFileName = mismatchRatesFileName;
//        this.randomSeed = randomSeed;
        randomNumberGenerator = new Random(randomSeed);
        //matchPositionScoreMap = new ArrayList<SimpleRandomAccessMap>(readLength);
        //mismatchPositionScoreMap = new ArrayList<SimpleRandomAccessMap>(readLength);
        //mismatchRateDistribution = new ArrayList<SimpleRandomAccessMap>(readLength);
        //PositionScoreDistrib = new ArrayList<CustomWeightProbabilityDistribution>(readLength);
//        allPositionScorePicker = new ArrayList<CustomWeightItemPicker>(readLength);

}
    public void run() throws Exception {
        //CustomWeightItemPicker<String> icustomWeightItemPicker;
	System.out.print("reading quality score distribution file from " + scoreDitributionBaseFileName + "... ");
        matchPositionScorePicker = loadPositionScoreDistribution(scoreDitributionBaseFileName+".match");
        mismatchPositionScorePicker = loadPositionScoreDistribution(scoreDitributionBaseFileName+".mismatch");
        positionMismatchRatePicker = loadMismatchRates();


        System.out.print("reading fastq read form " + fatqFileName + " and simulating errors ");
	fastqParser = new FastqReadParser(matchPositionScorePicker,mismatchPositionScorePicker,positionMismatchRatePicker,randomNumberGenerator);
        fastqParser.parse(fatqFileName);
}
//    private SimpleRandomAccessMap<String, Double> loadPositionScoreDistribution(CustomWeightProbabilityDistribution positionScoreDist, int readLength) {
        private ArrayList<CustomWeightItemPicker> loadPositionScoreDistribution(String filename) {
        ScoreDistribParser positionScoreParser = new ScoreDistribParser(readLength,randomNumberGenerator);
        ArrayList<CustomWeightItemPicker> map = null;
	try {
                map = positionScoreParser.parse(filename);
	} catch(Exception e) {
		System.err.println("Error parsing custom weights");
		e.printStackTrace();
	}
	return map;
}

        private ArrayList<CustomWeightItemPicker> loadMismatchRates() {
        MismatchRatesParser mismatchRatesParser = new MismatchRatesParser(readLength,randomNumberGenerator);
        ArrayList<CustomWeightItemPicker> map = null;
	try {
                map = mismatchRatesParser.parse(mismatchRatesFileName);
	} catch(Exception e) {
		System.err.println("Error parsing custom weights");
		e.printStackTrace();
	}
	return map;
}
    
}
