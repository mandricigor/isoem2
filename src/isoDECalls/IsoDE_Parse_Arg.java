
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.*;

public class IsoDE_Parse_Arg {
	
	
    private static final String DIR1_PARAMETER = "D1";
    private static final String DIR2_PARAMETER = "D2";
    private static final String BOOTSTRAP_SUPPORT_PARAMETER = "B";
    private static final String FOLD_CHANGE_PARAMETER = "DFC";
    private static final String OUT_FILE_PARAMETER = "OUT";
    private static final String MATCHING_A_PARAMETER = "A";
    private static final String MATCHING_M_PARAMETER = "M";
	
	/**
	   * Write the output string (isoem command) in a file
	   * @param string to be written
	   * @return create a file where the string is written
	   */
	 static void writeParametersFile(String D1, String D2,double B, Double DFC, String OUT, String P, String outputFile) {
		 				 
			try{
				  // Create file 						
				  FileWriter fstream = new FileWriter(outputFile);
				  BufferedWriter out = new BufferedWriter(fstream);
				  out.write(String.valueOf(D1) + "\n");
				  out.write(String.valueOf(D2) + "\n");
				  out.write(String.valueOf(B) + "\n");
				  out.write(String.valueOf(DFC) + "\n");
				  out.write(String.valueOf(OUT) + "\n");
				  out.write(String.valueOf(P));
				  
				  out.close();
		     }catch (Exception e){//Catch exception if any
				  System.err.println("Error: " + e.getMessage());
		     }		 
   }
	    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
        String D1;
        String D2 ;
        Double B;
        Double DFC ;
        String OUT ;
        Boolean A ;
        Boolean M ;    
		
        
	
        ArgumentParser parser = ArgumentParsers.newArgumentParser("IsoDE_Parse_Arg")
                .description("Arguments parser for input to IsoDE script.");
        
        
        MutuallyExclusiveGroup group = parser.addMutuallyExclusiveGroup("group");
        group.addArgument("-a").dest(MATCHING_A_PARAMETER)
    		    .metavar("AllPair")
    		    .action(Arguments.storeTrue())
    		    .help(" create all pair ratio");
        group.addArgument("-m").dest(MATCHING_M_PARAMETER)
      		.metavar("MatchingPair")
    		.action(Arguments.storeTrue())
    		.help(" create matching pair ratio");        

       
    	parser.addArgument("d1").dest(DIR1_PARAMETER)
    	.metavar("boostrapingPath1")
        .type(String.class)
        .help("Directory 1 containing the bootstrapped files ");
        	
    	
    	parser.addArgument("d2").dest(DIR2_PARAMETER)
    	.metavar("boostrapingPath2")
        .type(String.class)
        .help("Directory 2 containing the bootstrapped files ");
    	
        
    	parser.addArgument("b").dest(BOOTSTRAP_SUPPORT_PARAMETER)
    	.metavar("BootstrapSupport")
    	.setDefault(95)
    	.type(Double.class)
    	.help(" Bootstrap support to the decision: DE or non-DE");
    	
    	
        parser.addArgument("dfc").dest(FOLD_CHANGE_PARAMETER)
        .metavar("DesiredFoldChange")
        .setDefault(2)
        .type(Double.class)
        .help("Desired fold change  ");
        
    	parser.addArgument("out").dest(OUT_FILE_PARAMETER)
    	.metavar("OutputMatrix")
        .type(String.class)
        .help("Name of the output file ");


        
        try {
            Namespace n = parser.parseArgs(args); //test.gtf sam1.sam sam2.sam sam3.sam -m 23 -d 10
            
            //n = parser.parseArgs(args);
            //System.out.println(n);
  
            
            D1 = n.getString(DIR1_PARAMETER);
            D2 = n.getString(DIR2_PARAMETER);
            B = n.getDouble(BOOTSTRAP_SUPPORT_PARAMETER);
            DFC = n.getDouble(FOLD_CHANGE_PARAMETER);
            OUT = n.getString(OUT_FILE_PARAMETER);
            A = n.getBoolean(MATCHING_A_PARAMETER);
            M = n.getBoolean(MATCHING_M_PARAMETER);

          
            String P="M";
     		if ((A == true) && (M == true)){
     			//System.out.println("Either m or option a but not both" );
     			System.exit(12);
     		}  else if ((A == false) && (M == false)){
     			M = true;
     			P = "M";
     		} else if ((A == true)){
     			P="A";     			
     		    M= false;
     		} else if((M == true)){
     			P="M";     			
     		    A= false;
     		}
     		
     		
/*            System.out.println("The parameters/options are:");
        	System.out.println("A = " + A);
        	System.out.println("M = " + M);
        	System.out.println("P = " + P);*/
     		
     		writeParametersFile(D1, D2, B, DFC, OUT, P, "param.txt");


        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

		
	}
}
