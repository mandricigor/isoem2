import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 
 * @author Charly
 * Class generate log_2(FC) lower/upper bound for user specified bootstrap support B (B=xx%)
 * N: number of run of bootstrap
 * P = matching: generate  pairs by sampling the first and second sam file without replacement

	sort P = # logs of ratios of pairs of bootstraps in ascending order 
         compute Both smallest and largest out of P (P_min, P_max)
         if P_min > 0 report P_min, else if P_max <0 then report P_max 
         otherwise report NDE
	if 1stFPKM =0, and 2ndFPKM # 0, log_2(FC) 2ndFPKM/1stFPKM = 100
	if 2ndFPKM=0, and 1stFPKM # 0, log_2(FC) 2ndFPKM/1stFPKM = - 100
	if 2ndFPKM= 0 and 1stFPKM=0, log_2(FC) 2ndFPKM/1stFPKM = 0


 * Former version: Gene_200_FPKM.java
 * Cover GoogleDoc
 * : 1, 2, 3 and 4
 */
public class Log_Lower_Upper_Matching_Ratio {
	
	
	/**
	   * Create a map from the input file to store geneID
	   * Map: key: geneID, value: empty list (to later contain the FPKM values)
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
				    	  s.clear();
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
	
	  
	  
	
	//Create a map with geneId as key and value is a set containing all the 200 expressions 
	//The output is the map
	
	  Map create200Expre(Map<String,List<Double>> map, String dir) {


		  File directory = new File(dir);      
		  File[] myarray;
		  myarray=directory.listFiles();
		  int error=0;

		  for (int j = 0; j < myarray.length; j++)
		  {

			  try{		  

				  File path= myarray[j]; 
				  FileReader fr = new FileReader(path);
				  BufferedReader br = new BufferedReader(fr);

				  //int error=0;
				  String strLine;
				  String geneId; 
				  String delims = "[\\s]+";

				  while (((strLine = br.readLine()) != null) && (error==0))  {
					  String[] tokens = strLine.split(delims);
					  geneId = tokens[0];

					  // Create map2 : (key= readId , List<alignment>)
					  if(map.containsKey(geneId)) {
						  List<Double> s = map.get(geneId);
						  double geneExp = Double.parseDouble(tokens[1].toString()); 
						  s.add(geneExp);
						  map.put(geneId, s);
					  }
					  else {
						  error++;
						  System.out.println("Name of the file: " + path.toString() );
					  }

				  }

				  fr.close();		  

			  }catch (Exception e){//Catch exception if any
				  System.err.println("Error: " + e.getMessage());
			  }

			  if (error != 0) {
				  System.out.println("create200Expre: nber of error before end " + error );
				  break;
			  }
		  }
		  System.out.println("create200Expre: nber of error  " + error );
		  return map;
	  }


	  /* log 2 of double n */
	  double log2(double n)
	  {
	      return (((double)Math.log(n)) /( (double)Math.log(2)));
	  }

	
	
		
		/**
		 create a map which contains the log_2 ratio of matching fpkm picked from a 200 fpkm gene value with direction predifined sam2/sam1
		 if 1stFPKM =0, and 2ndFPKM # 0, log_2(FC) 2ndFPKM/1stFPKM = 100
		 if 2ndFPKM=0, and 1stFPKM # 0, log_2(FC) 2ndFPKM/1stFPKM = - 100
		 if 2ndFPKM= 0 and 1stFPKM=0, log_2(FC) 2ndFPKM/1stFPKM = 0
		 */
		Map create_map_geneID_log_2_matching_ratio(Map mapUhr, Map mapBrain) {  //convert this to write to file
			int nberGenes=0;
			Map<String,List<Double>> mapRatio = new HashMap<String,List<Double>>();	
			
			for(Object word: mapUhr.keySet()) {
				
				List<Double> listUhr = (List) mapUhr.get(word);
				nberGenes++;
				
				if(mapBrain.containsKey(word)) {
					List<Double> listRatio = new ArrayList<Double>();
					double ratio =0;
					
					List<Double> listBrain = (List) mapBrain.get(word);
					
					for (int i =0; i< listUhr.size(); i++){	 				//listUhr.size() = nber of runs of bootstrap
					//for (int i =0; i< 30; i++){				
						double fpkmUhr = listUhr.get(i);
						double fpkmBrain = listBrain.get(i);
						//if (fpkmBrain == 0.0 &  fpkmUhr == 0.0) ratio = 0.0;  //old
						if (fpkmBrain == 0.0 &  fpkmUhr == 0.0) ratio = 1.0;
						else if (fpkmBrain == 0.0 &  fpkmUhr != 0.0) ratio = -100.0;
						else if (fpkmUhr == 0.0 &  fpkmBrain != 0.0) ratio = 100.0;
						else ratio = log2(fpkmBrain/fpkmUhr);

//						System.out.println( "Position " + i + " Gene: " + word.toString() +"  fpkmBrain: " + fpkmBrain + " fpkmUhr: " + fpkmUhr + " ratio: " + ratio );
						listRatio.add(ratio);
						//Collections.sort(listRatio);
					 }
					mapRatio.put(word.toString(), listRatio);
				} //end if geneId in mapBrain
				
					
				//if (nberGenes == 3) break;
				//System.out.println( "We r processing gene number: " + nberGenes);
			} // end for word in mapUhr
			
			System.out.println("Nber of genes (create_map_geneID_ratio): " + nberGenes );	
		    //System.out.println("Size of map (create_map_geneID_ratio): " + mapRatio.size() );	    //no error
		    Map sortedMap = new TreeMap(mapRatio); //sort the map
			return sortedMap;
				
	}
		 

		
		/*
		 * 
			Additional parameters (default is in bold):  
			P = A or M 
			A = all pairs: pair each first bootstrapping sample with every second bootstrapping sample 
     		resulting in N^2 FC values

		 */
	
		
		/**
		 create a map which contains the log_2 ratio of matching fpkm picked from a 200 fpkm gene value with direction predifined sam2/sam1
		 if 1stFPKM =0, and 2ndFPKM # 0, log_2(FC) 2ndFPKM/1stFPKM = 100
		 if 2ndFPKM=0, and 1stFPKM # 0, log_2(FC) 2ndFPKM/1stFPKM = - 100
		 if 2ndFPKM= 0 and 1stFPKM=0, log_2(FC) 2ndFPKM/1stFPKM = 0
		 */
		Map all_Pair_matching_ratio(Map mapUhr, Map mapBrain) {  //convert this to write to file
			int nberGenes=0;
			Map<String,List<Double>> mapRatio = new HashMap<String,List<Double>>();	
			
			for(Object word: mapUhr.keySet()) {
				
				List<Double> listUhr = (List) mapUhr.get(word);
				nberGenes++;
				
				if(mapBrain.containsKey(word)) {
					List<Double> listRatio = new ArrayList<Double>();
					double ratio =0;
					
					List<Double> listBrain = (List) mapBrain.get(word);
					
					for (int i =0; i< listUhr.size(); i++){	 				//listUhr.size() = nber of runs of bootstrap
					//for (int i =0; i< 30; i++){				
						double fpkmUhr = listUhr.get(i);
						
						for (int j =0; j< listBrain.size(); j++){	 
							double fpkmBrain = listBrain.get(j);
							//if (fpkmBrain == 0.0 &  fpkmUhr == 0.0) ratio = 0.0; //old
							if (fpkmBrain == 0.0 &  fpkmUhr == 0.0) ratio = 1.0;
							else if (fpkmBrain == 0.0 &  fpkmUhr != 0.0) ratio = -100.0;
							else if (fpkmUhr == 0.0 &  fpkmBrain != 0.0) ratio = 100.0;
							else ratio = log2(fpkmBrain/fpkmUhr);

							listRatio.add(ratio);
						}
						
						
					 }
					mapRatio.put(word.toString(), listRatio);
				} //end if geneId in mapBrain
				
				
			} // end for word in mapUhr
			
			System.out.println("Nber of genes (create_map_geneID_ratio): " + nberGenes );	
		    //System.out.println("Size of map (create_map_geneID_ratio): " + mapRatio.size() );	    //no error
		    Map sortedMap = new TreeMap(mapRatio); //sort the map
			return sortedMap;
				
	}
		 	
		
		
		
		
		/**
		 create a map which contains the geneId + log_2_FC
		2.	log_2(FC) lower/upper bound for user specified bootstrap support B (B=95% default)
		sort P = # logs of ratios of pairs of bootstraps in ascending order 
         compute Both smallest and largest out of P (P_min, P_max)
         Given: ratio0,....ratio199
         P_min= ratio4 and P_max= ratio194
         if P_min > 0 report P_min, else if P_max <0 then report P_max 
         otherwise report NDE 

		 */
		Map create_map_Pmin_Pmax(Map map, double support) {  //convert this to write to file
			int nberGenes=0;
			String log_2_FC;
			Map<String,String> mapRatio = new HashMap<String,String>();	
			
			for(Object word: map.keySet()) {
				List<Double> listRatio = (List)map.get(word);
				int runs = listRatio.size();
//		    System.out.println("runs: " + runs );	    
//		    System.out.println("support: " + support );	    
				Collections.sort(listRatio);
				nberGenes++;
				//int low_bound = (int) ((100 - support)/2)*runs/100; Incorrect
				int low_bound = (int) (((100 - support))*runs/100 -1);
				int upper_bound = (int)((runs - low_bound)-1);
				
//		    System.out.println("lowbound: " + low_bound );	   
//		    System.out.println("upperbound: " + upper_bound );	    
				double P_min = listRatio.get(low_bound);
				double P_max = listRatio.get(upper_bound);
				if (P_min > 0) log_2_FC = String.valueOf(P_min);
				else if (P_max < 0) log_2_FC = String.valueOf(P_max);
				else log_2_FC = "NDE";
				mapRatio.put(word.toString(), log_2_FC);
				//System.out.println("P_min: " + P_min  + "  P_max: " + P_max);	
		    } // end for word in map
			
			//System.out.println("Nber of genes (create_map_geneID_ratio): " + nberGenes );	
		    //System.out.println("Size of map (create_map_geneID_ratio): " + mapRatio.size() );	    //no error
		    Map sortedMap = new TreeMap(mapRatio); //sort the map
			return sortedMap;
				
	}
		
		
		
		
		
		
		
		/**
		 create a map which contains the geneId + percentage of gene supporting FC
		3. Support for user given FC (FC= xx in %)
		sort P = # logs of ratios of pairs of bootstraps in ascending order
		report  (| bootstrap ratios larger than FC| / |P| )*100%
		 */
		
		Map create_FC_Support(Map map, double FC) {  //convert this to write to file
			
			Map<String,Double> mapFCSup= new HashMap<String,Double>();	
			Double thres = log2(FC); 
			for(Object word: map.keySet()) {
				int nber_Support_Gene=0;
				List<Double> listRatio = (List)map.get(word);
				int runs = listRatio.size();
				Collections.sort(listRatio);

				for (int i =0; i< runs; i++){			//Crosscheck pls
				//for (int i =0; i< 30; i++){
					//if (Math.abs(listRatio.get(i))>= thres)
					if (listRatio.get(i)>= thres)	
						nber_Support_Gene++;
				}
				mapFCSup.put(word.toString(), (nber_Support_Gene/(double) runs)*100);	
		    } // end for word in map
			
			Map sortedMap = new TreeMap(mapFCSup); //sort the map
			return sortedMap;
		}
		
		
		
		
		/**
		 create a map which contains the geneId + percentage of gene supporting FC
		 4.	Support for user given 1/FC (FC = 1/xx in %)	
		 sort P = # logs of ratios of pairs of bootstraps in ascending order
		 report  (|bootstrap ratios smaller than FC| / |P|) *100%
		 */
		
		Map create_1_FC_Support(Map map, double FC) {  //convert this to write to file
			
			Map<String,Double> mapFCSup= new HashMap<String,Double>();	
			Double t = 1.0/FC;
			Double thres = log2(t); 
			for(Object word: map.keySet()) {
				int nber_Support_Gene=0;
				List<Double> listRatio = (List)map.get(word);
				int runs = listRatio.size();
				Collections.sort(listRatio);

				//for (int i =0; i< listUhr.size(); i++){
				for (int i =0; i< runs; i++){
					//if (Math.abs(listRatio.get(i))< thres)
					if (listRatio.get(i)<= thres)
						nber_Support_Gene++;
				}
				mapFCSup.put(word.toString(), (nber_Support_Gene/(double) runs)*100);	
		    } // end for word in map
			
			Map sortedMap = new TreeMap(mapFCSup); //sort the map
			return sortedMap;
		}
		
		
		
	
		/**
		 * Prints the contents of a 1 Map containing
		 * create_map_Pmin_Pmax: GeneId, a string value.	   
		 * The readIds are keys and their respective alignment the values.
		 */
			void write_log2_FC(Map map, String output_file) {  //convert this to write to file
				try{
					  // Create file 
						
					  FileWriter fstream = new FileWriter(output_file);
					  BufferedWriter out = new BufferedWriter(fstream);
					  
					  for(Object word : map.keySet()) {
						  out.write(word.toString()+ "  ");
						  String s  = (String)map.get(word);
						  out.write(s + "\n");
					  }
					  out.close();
				  }catch (Exception e){//Catch exception if any
				  System.err.println("Error: " + e.getMessage());
				  }
			   } 	
		
			
			
			/**
			 * Prints the contents of a Map containing
			 * readIds and one double value.	   
			 * The readIds are keys and their respective alignment the values.
			 */
				void write_map_genes(Map map, String output_file) {  //convert this to write to file
					try{
						  // Create file 
							
						  FileWriter fstream = new FileWriter(output_file);
						  //FileWriter fstream = new FileWriter("Genes_Brain_Uhr_ratio.txt");
						  //FileWriter fstream = new FileWriter("New_90_conf_Interval_BRAIN.txt");
						  BufferedWriter out = new BufferedWriter(fstream);
						  
						  for(Object word : map.keySet()) {
							  out.write(word.toString()+ "  ");
							  Double s  = (Double)map.get(word);
							  out.write(s + "\n");
						  }
						  out.close();
					  }catch (Exception e){//Catch exception if any
					  System.err.println("Error: " + e.getMessage());
					  }
				   }
			
				
				
				
				
				/**
				 * Prints the contents of a 3 Maps containing
				 * create_map_Pmin_Pmax: GeneId, a string value
	   			 * create_FC_Support:	GeneId, a double FC support
	   			 * create_1_FC_Support:	GeneId, a double 1/FC support	
				 * The readIds are keys and their respective alignment the values.
				 */
					void write_all_3maps(Map map1, Map map2, Map map3, String output_file) {  //convert this to write to file
						try{
							  // Create file 
								
							  FileWriter fstream = new FileWriter(output_file);
							  BufferedWriter out = new BufferedWriter(fstream);
							  
							  for(Object word : map1.keySet()) {
								  out.write(word.toString()+ "  ");
								  String s  = (String)map1.get(word);
								  out.write(s + "  ");	
								  if(map2.containsKey(word)) {
									  String s1 = String.valueOf((String)map2.get(word).toString());
									  out.write(s1 + "  ");
								  }
								  if(map3.containsKey(word)) {
									  String s2 = String.valueOf((String)map3.get(word).toString());
									  out.write(s2 + "\n");
								  }
							  }
							  out.close();
						  }catch (Exception e){//Catch exception if any
						  System.err.println("Error: " + e.getMessage());
						  }
					   } 	
					
			
				
				
				
				
				
			
			
			/**
			 * Prints the contents of a Map containing
			 * readIds and alignment.	   
			 * The readIds are keys and their respective alignment the values.
			 */
				void write_686_genes(Map map, String output_file) {  //convert this to write to file
					try{
						  // Create file 
							
						  FileWriter fstream = new FileWriter(output_file);
						  //FileWriter fstream = new FileWriter("Genes_Brain_Uhr_ratio.txt");
						  //FileWriter fstream = new FileWriter("New_90_conf_Interval_BRAIN.txt");
						  BufferedWriter out = new BufferedWriter(fstream);
						  
						  for(Object word : map.keySet()) {
							  out.write(word.toString()+ "  ");
							  List<Double> s  = (List)map.get(word);
							  Collections.sort(s);
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
//sahar July 12th, 2016 isoem2. Now we generate 200 bootstrap samples			
			String pairing = "M";
			// String pairing = args[6];
			
			//double support = 95;
			double support = Double.parseDouble(args[2]);
			
			
			//double FC= 2;
			double FC = Double.parseDouble(args[3]);
			
			// change uhr and brain to sam1 and sam2
			//String filenameB = "One_run_sam2/sam2_output.gene_estimates";
			//String repB = "Execution_SAM2";
			
			
			//String filenameB = "C:/Users/ytematetiagueu1/Desktop/Scripts_test/New_Test_Data/Gene_Expression/BRAIN_result.gene_estimates";// will be results1
			//String repB = "C:/Users/ytematetiagueu1/Desktop/Scripts_test/New_Test_Data/Gene_estimates/BRAIN";
			  
			String filenameB = args[4];
			String repB = args[0];
			  
			  
			  //String filenameU = "One_run_sam1/sam1_output.gene_estimates";
			  //String repU = "Execution_SAM1";
			 
			//String filenameU = "C:/Users/ytematetiagueu1/Desktop/Scripts_test/New_Test_Data/Gene_Expression/UHR_result.gene_estimates"; // will be results1
			//String repU = "C:/Users/ytematetiagueu1/Desktop/Scripts_test/New_Test_Data/Gene_estimates/UHR";
			
			  String filenameU = args [5];
			  String repU = args [1];
			  
			  
			  
			  //Log_Lower_Upper_Matching_Ratio
			  Log_Lower_Upper_Matching_Ratio llumr = new Log_Lower_Upper_Matching_Ratio();
			  
			  Map map_geneU = llumr.createMapGene(filenameU);
			  Map map200U = llumr.create200Expre(map_geneU, repU);
			  
			  Map map_geneB = llumr.createMapGene(filenameB);
			  Map map200B = llumr.create200Expre(map_geneB, repB);
			  
			  
			  System.out.println("map ready before pairing !"); 
			  
			  
			  Map map_U_B_ratio = null;
			  if (pairing.equals("M"))
				  map_U_B_ratio = llumr.create_map_geneID_log_2_matching_ratio(map200U, map200B); //for M=matching pairs
			  else	  
			      map_U_B_ratio = llumr.all_Pair_matching_ratio(map200U, map200B); // for all matching
			  
			  
			  Map mPP = llumr.create_map_Pmin_Pmax(map_U_B_ratio, support);
			  
			  Map FCS = llumr.create_FC_Support(map_U_B_ratio, FC);
			  //llumr.write_map_genes(FCS, "FC_Sup_Gene.txt");

			  Map FC_1_S = llumr.create_1_FC_Support(map_U_B_ratio, FC);
			  //llumr.write_map_genes(FC_1_S, "FC_1_Sup_Gene.txt");
			  
			  //llumr.write_log2_FC(mPP, "log2_FC_Pmin_Pmax.txt");
			  //using the same name(previous) as in the script
			  llumr.write_all_3maps(mPP, FCS, FC_1_S, "log2_FC_Pmin_Pmax.txt");
			  
			  System.out.println("Done !");
			  
		}  
		
		
      }
