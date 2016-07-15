import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
//import java.io.File;
import java.io.FileInputStream;
//import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 
 * @author Charly
 * Create a file which contains the gene ID and 5, 6 and 7
 5.	log_2 fc = log_2(2ndFPKM/1stFPKM) (Exp FC) 
 6.	1stFPKM = original IsoEM (FPKM_1)
 7.	2ndFPKM  = original IsoEM (FPKM_2)


 * Former version: Gene_200_FPKM.java
 */
public class Exp_Log_FC {


	
	
	
	/**
	   *  Create a map
	   * Map: key: geneID, value: list containing the first FPKM value from one run of IsoEm
	   * @param filename: file contains gene ID , and the 1 FPKM value from one run of IsoEm
	   * @return
	   */
	  Map createMapGene(String filename) {
	    
		Map<String,List<Double>> map = new HashMap<String,List<Double>>();
	    int error = 0;
	    
	    try{		  
			  
			  FileInputStream fstream = new FileInputStream(filename);
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  
			 
			  String strLine;
			  String geneId = "";
			  String delims = "[\\s]+"; //myString.split("\\s+");
			  while ((strLine = br.readLine()) != null)   {
				  String[] tokens = strLine.split(delims);
			      geneId = tokens[0];
		     
			      if(map.containsKey(geneId)) {
			    	    error ++;
				      }
				      else {
				    	  double exprgene = Double.parseDouble(tokens[1].toString()); 
				    	  List<Double> s = new ArrayList<Double>();
				    	  s.add(exprgene);
				    	  //s.clear();
				    	  //map.put(geneId, Double.parseDouble(tokens[1].toString()));
				    	 //List<Double> s = Collections.emptySet();
						 map.put(geneId,s);
				      }
			  }
			  
			  in.close();		  
			  
			  }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
			  }
	    System.out.println("Size of map (createMapGene): " + map.size() );	    //no error
	    Map sortedMap = new TreeMap(map); //sort the map
		return sortedMap;
	  }
	

	  /* log 2 of double n */
	  double log2(double n)
	  {
	      return (((double)Math.log(n)) /( (double)Math.log(2)));
	  }

	

		
		/**
		 Create a map which contains the gene ID and 5, 6 and 7
		5.	log_2 fc = log_2(2ndFPKM/1stFPKM) (Exp FC) 
		6.	1stFPKM = original IsoEM (FPKM_1)
		7.	2ndFPKM  = original IsoEM (FPKM_2)
		 */
		Map create_log_2_Exp_FC (Map mapUhr, Map mapBrain) {  //convert this to write to file
			int nberGenes=0;
			Map<String,List<Double>> mapExp = new HashMap<String,List<Double>>();	
			
			for(Object word: mapUhr.keySet()) {
				List<Double> listRatio = new ArrayList<Double>();
				double ratio =0;
				List<Double> listUhr = (List) mapUhr.get(word);
				nberGenes++;
				
				if(mapBrain.containsKey(word)) {
					List<Double> listBrain = (List) mapBrain.get(word);
					
					//for (int i =0; i< listUhr.size(); i++){	 	

						double fpkmUhr = listUhr.get(0);
						double fpkmBrain = listBrain.get(0);
						if (fpkmBrain == 0.0 &  fpkmUhr == 0.0) ratio = 0.0;
						else if (fpkmBrain == 0.0 &  fpkmUhr != 0.0) ratio = - 100.0;
						else if (fpkmUhr == 0.0 &  fpkmBrain != 0.0) ratio = 100.0;
						else ratio = log2(fpkmBrain/fpkmUhr);

						//System.out.println( "Show me the ratio: " + ratio );	
						listRatio.add(ratio);
						listRatio.add(fpkmUhr);
						listRatio.add(fpkmBrain);
						//Collections.sort(listRatio);
						//System.out.println( " Gene: " + word.toString() +"  fpkmBrain: " + fpkmBrain + " fpkmUhr: " + fpkmUhr + " ratio: " + ratio );
						
					 //}

				} //end if geneId in mapBrain
				
				mapExp.put(word.toString(), listRatio);	
				//if (nberGenes == 3) break;
			} // end for word in mapUhr
			
			System.out.println("Nber of genes (create_map_geneID_ratio): " + nberGenes );	
		    //System.out.println("Size of map (create_map_geneID_ratio): " + mapRatio.size() );	    //no error
		    Map sortedMap = new TreeMap(mapExp); //sort the map
			return sortedMap;
				
	}
		 
		
		
		
		
	
		/**
		 * Prints the contents of a Map containing
		 * readIds and alignment.	   
		 * The readIds are keys and their respective alignment the values.
		 */
			void write_log2_FC(Map map, String output_file) {  //convert this to write to file
				try{
					  // Create file 
						
					  FileWriter fstream = new FileWriter(output_file);
					  BufferedWriter out = new BufferedWriter(fstream);
					  
					  for(Object word : map.keySet()) {
						  out.write(word.toString()+ "  ");
						  List<Double> s  = (List)map.get(word);
						  //Collections.sort(s);
						  Iterator itr = s.iterator(); 
						  while(itr.hasNext()) {
							  double element = (Double)itr.next(); 
							  out.write(element + "  ");
						  }
						  out.write("\n");
					  }
					  out.close();
				  }catch (Exception e){//Catch exception if any
				  System.err.println("Error: " + e.getMessage());
				  }
			   } 	
		
		
			
		public static void main(String[] args) {
			
			//change uhr and brain to sam1 and sam2
			//String filenameB = "One_run_sam2/sam2_output.gene_estimates";
			  String filenameB = args[0];
			  //String filenameB = "C:/Users/ytematetiagueu1/Desktop/Scripts_test/New_Test_Data/Gene_Expression/BRAIN_result.gene_estimates";// will be results1
			  		  
			  //String filenameU = "One_run_sam1/sam1_output.gene_estimates";
			  String filenameU = args[1];
			  //String filenameU = "C:/Users/ytematetiagueu1/Desktop/Scripts_test/New_Test_Data/Gene_Expression/UHR_result.gene_estimates"; // will be results1
			  			
			  
			  //Log_Lower_Upper_Matching_Ratio
			  Exp_Log_FC elfc = new Exp_Log_FC();
			  
			  Map map_geneU = elfc.createMapGene(filenameU);
			  Map map_geneB = elfc.createMapGene(filenameB);
			  
			  System.out.println("map of 200 fpkm per genes and per sample created !"); 
			  
			  Map map_U_B_ratio = elfc.create_log_2_Exp_FC(map_geneU, map_geneB);
			  elfc.write_log2_FC(map_U_B_ratio, "Exp_log2_FC.txt");
			  
			  System.out.println("Done !");
			  
		}  
		
		
      }
