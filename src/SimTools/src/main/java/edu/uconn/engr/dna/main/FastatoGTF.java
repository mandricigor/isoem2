/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uconn.engr.dna.main;

import edu.uconn.engr.dna.io.FastaParser;
import edu.uconn.engr.dna.io.FastatoGTFParser;
import java.io.FileInputStream;
import edu.uconn.engr.dna.probability.CustomWeightItemPicker;

/**
 *
 * @author Sahar
 */
public class FastatoGTF {
    public static void main(String[] args) throws Exception {
	if (args.length != 1) {
        	System.out.println("Arguments: fastaFile");
		return;
        }
        Run(args[0]);
    }
    private static void Run(String inFastaFileName)throws Exception {
         String fastaFileName;
//private int randomSeed;

        fastaFileName = inFastaFileName;
        FastatoGTFParser fastaParser = new FastatoGTFParser();
        fastaParser.parse(fastaFileName);

   }


}


