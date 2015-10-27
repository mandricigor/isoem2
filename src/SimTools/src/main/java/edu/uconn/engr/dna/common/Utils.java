/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uconn.engr.dna.common;

import java.util.ArrayList;
import java.util.Random;
import edu.uconn.engr.dna.probability.CustomWeightItemPicker;

/**
 *
 * @author Marius
 */
public class Utils {
    public static final int QSCORE_RANGE = 126-32;
     /**
     * Convert a single printable ASCII FASTQ format phred score to binary phred score.
     *
     * @param ch Printable ASCII FASTQ format phred score.
     * @return Binary phred score.
     */
    public static int fastqToPhred(final char ch) {
        if (ch < 33 || ch > 126) {
            throw new IllegalArgumentException("Invalid fastq character: " + ch);
        }
        return (ch - 33);
    }

    public static double phredProbability(int phredScore) {
        return Math.pow(10, -phredScore / 10.0);
    }
   public static char PhredTofastq(final int Phred) {
        if (Phred < 0 || Phred > 93)
        {
            throw new IllegalArgumentException("No corresponding fastq character for: " + Phred);
        }
        return ((char)(Phred + 33));
    }

//    public static char computeBase(char base,char fastqScore,Random randomNumberGenerator) {
//      String BASES = "ACGT";

//    double errorProb = phredProbability(fastqToPhred(fastqScore));
//      SimpleRandomAccessMap<String, Double> errorMap = new SimpleRandomAccessMap<String, Double>();
//      errorMap.put("T", errorProb);
//      errorMap.put("F", 1-errorProb);
//      CustomWeightItemPicker<String> errorPicker = new CustomWeightItemPicker<String>(errorMap,randomNumberGenerator);
//      if (errorPicker.pick(null).equals("T")) {
//          char newbase = BASES.charAt(randomNumberGenerator.nextInt(4));
//         System.out.print(newbase);
//          return newbase;
//          return BASES.charAt(randomNumberGenerator.nextInt(4));
//        }
//      else
//          return base;
//
//    }


    public static char computeBase(char base,CustomWeightItemPicker<String> positionMismatchRatePicker,Random randomNumberGenerator) {
      String BASES = "ACGT";

      switch (base) {
          case 'A':
              BASES = "CGT";
              break;
          case 'C':
              BASES = "AGT";
              break;
          case 'G':
              BASES = "ACT";
              break;
          case 'T':
              BASES = "ACG";
              break;
          case 'a':
              BASES = "cgt";
              break;
          case 'c':
              BASES = "agt";
              break;
          case 'g':
              BASES = "act";
              break;
          case 't':
              BASES = "acg";
              break;
      }
      if (positionMismatchRatePicker.pick(null).compareTo("T") == 0) {
//          char newbase = BASES.charAt(randomNumberGenerator.nextInt(4));
//         System.out.print(newbase);
//          return newbase;
          return BASES.charAt(randomNumberGenerator.nextInt(3));
        }
      else
          return base;

    }
}



