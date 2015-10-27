/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uconn.engr.dna.main;

import edu.uconn.engr.dna.common.SimpleRandomAccessMap;
import edu.uconn.engr.dna.io.FastaParser;
import java.io.FileInputStream;
import java.util.Random;
import edu.uconn.engr.dna.probability.CustomWeightItemPicker;

/**
 *
 * @author Sahar
 */
public class InsertSNPs {



    public static void main(String[] args) throws Exception {
	if (args.length != 3) {
        	System.out.println("Arguments: fastaFile SNVrate seed");
		return;
        }
        Run(args[0],Double.parseDouble(args[1]),Integer.parseInt(args[2]));
    }
    private static void Run(String inFastaFileName, Double inSNVrate, int inRandomSeed)throws Exception {
         String fastaFileName;
//private int randomSeed;
        Random randomNumberGenerator;
        SimpleRandomAccessMap<String, Double> SNPRate;
        CustomWeightItemPicker<String> SNP_TF_ItemPicker;


        fastaFileName = inFastaFileName;
        randomNumberGenerator = new Random(inRandomSeed);
        SNPRate = new SimpleRandomAccessMap<String, Double>();
        SNPRate.put("T",inSNVrate);
        SNPRate.put("F",1-inSNVrate);
        SNP_TF_ItemPicker = new CustomWeightItemPicker<String>(SNPRate,randomNumberGenerator);
        FastaParser fastaParser = new FastaParser(randomNumberGenerator,SNP_TF_ItemPicker);
        Integer ret = fastaParser.parse(fastaFileName);

   }

    private static void parseFasta(String inFastaFileName,CustomWeightItemPicker<String> SNP_TF_ItemPicker) throws Exception {
        FileInputStream inputStream;

        inputStream = new FileInputStream(inFastaFileName);


    }


}
