/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uconn.engr.dna.main;

/**
 *
 * @author sahar
 */


public class Entry {

    public static void main(String[] args) throws Exception {
	if (args.length != 5) {
        	System.out.println("Arguments: fastqFile readLength qualityScoreDistfileBasename mismatchMatchRateFile seed");
		return;
        }
        //int readLength = Integer.getInteger(args[1]);
        Simulator simulator = new Simulator(args[0],Integer.parseInt(args[1]),args[2],args[3], Integer.parseInt(args[4]));
        simulator.run();
	
    }

    
}
