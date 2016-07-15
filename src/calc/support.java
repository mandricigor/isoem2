import java.io.*;
import org.apache.commons.math3.distribution.BinomialDistribution;


public class support {
	
	int best_Support(int nberOfRatios, double p, double p_value) {

		int i;
		
		BinomialDistribution bdist = new BinomialDistribution(nberOfRatios, p);
		
		double result = 0.0;
		
		for( i = 1; i <= nberOfRatios; i++ ){
			if( bdist.cumulativeProbability(i-1) >= (1 - p_value) ) break;
		}
				
		return i;
	}
	
	
	public static void main(String[] args) {
		
		int minSupport; 
		int n1 = Integer.parseInt(args[0]);
		int n2 = Integer.parseInt(args[1]);
//sahar isode2 switching to 200 boostrap iterations, random match pairing (instead of all pairs)
//		int nberOfRatios = n1*n2;
		int nberOfRatios = n1;
		double p_value= Double.parseDouble(args[2]);
//		try {
//			PrintWriter out = new PrintWriter(args[3]);
			support s = new support();
//			out.println( "Bootstrap runs for condition 1: " + n1 );
//			out.println( "Bootstrap runs for condition 2: " + n2 );
//			out.println( "Significance level: " + p_value );
//			out.println();
//			out.println( "Total bootstrap pairs: " + n1*n2 );
			minSupport = s.best_Support(nberOfRatios, 0.5, p_value);
//			out.println( "Minimum bootstrap support: " + minSupport + " (" + (double)(100*minSupport)/(double)(n1*n2)+"%)");
//sahar isode2 switching to 200 boostrap iterations, random match pairing (instead of all pairs)
//			System.out.println((double)(100*minSupport)/(double)(n1*n2));
			System.out.println((double)(100*minSupport)/(double)(n1));
//            		out.close();
//		} catch (IOException ex) {
//			System.out.println("Error creating file");
//		}
	} 

}
