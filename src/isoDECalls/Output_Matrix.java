
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
public class Output_Matrix {


	
	
	
	/**
	   *  Create a map
	   * Map: key: geneID, value: list containing geneId value
	   * @param filename: file contains gene ID , and geneId data
	   * @return
	   */
	  Map createMapGene(String filename) {
	    
		Map<String,List<String>> map = new HashMap<String,List<String>>();
	    int error = 0;
	    
	    try{		  
			  
			  FileInputStream fstream = new FileInputStream(filename);
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  
			 
			  String strLine;
			  String geneId = "";
			  String delims = "[\\s]+"; //myString.split("\\s+");
			  String exprgene;
			  while ((strLine = br.readLine()) != null)   {
				  String[] tokens = strLine.split(delims);
			      geneId = tokens[0];
		     
			      if(map.containsKey(geneId)) {
			    	    error ++;
				      }
				  else {
				    	 List<String> s = new ArrayList<String>();
				    	 
				    	 for (int i=1; i<tokens.length; i++){
				    		  exprgene = String.valueOf(tokens[i].toString());
				    		  s.add(exprgene);
				          }
				    	  
				    	  //s.clear();
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
	
	
	  
		/**
		 Create a map which contains the gene ID and 5, 6 and 7
		5.	log_2 fc = log_2(2ndFPKM/1stFPKM) (Exp FC) 
		6.	1stFPKM = original IsoEM (FPKM_1)
		7.	2ndFPKM  = original IsoEM (FPKM_2)
		 */
		Map merge_map (Map map1, Map map2) {  //convert this to write to file
			//int nberGenes=0;
			Map<String,List<String>> output_map = new HashMap<String,List<String>>();	
			
			for(Object word: map1.keySet()) {
				List<String> output_list= new ArrayList<String>();
				List<String> list1 = (List)map1.get(word);
				
				Iterator itr = list1.iterator(); 
				while(itr.hasNext()) {
					  String element = String.valueOf(itr.next()); 
					  output_list.add(element);
				}
				
				if(map2.containsKey(word)) {
					List<String> list2 = (List)map2.get(word);					
					Iterator itr2 = list2.iterator(); 
					while(itr2.hasNext()) {
						  String element2 = String.valueOf(itr2.next()); 
						  output_list.add(element2);
					}
				} //end if geneId in mapBrain
				
				output_map.put(word.toString(), output_list);	
				
			} // end for word in mapUhr
			
			//System.out.println("Nber of genes (create_map_geneID_ratio): " + nberGenes );	
		    Map sortedMap = new TreeMap(output_map); //sort the map
			return sortedMap;
				
	}
	  
	  
	
		/**
		 * Prints the contents of a Map containing
		 * readIds and alignment.	   
		 * The readIds are keys and their respective alignment the values.
		 */
			void write_output_matrix(Double B, Double DFC, Map map, String output_file) {  //convert this to write to file
				try{
					  // Create file 
						
					  FileWriter fstream = new FileWriter(output_file);
					  BufferedWriter out = new BufferedWriter(fstream);
					  out.write("Gene              " + "B=" + B + "%             "+ "FC=" + DFC + "_in%     "+ "FC=1/" + DFC + "_in%      "+"Exp_FC              " + "FPKM_1             " + "FPKM_2           \n");
					  
					  for(Object word : map.keySet()) {
						  out.write(word.toString()+ "  ");
						  List<String> s  = (List)map.get(word);
						  //Collections.sort(s);
						  Iterator itr = s.iterator(); 
						  while(itr.hasNext()) {
							  String element = (String)itr.next(); 
							  out.write(element + "      ");
						  }
						  out.write("\n");
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
				void write_gene_Direction_DE(Map map) {  //convert this to write to file
					map = clean_map(map);
					try{
						  // Create file 
							
						  FileWriter fstream = new FileWriter("Direction_De.csv");
						  BufferedWriter out = new BufferedWriter(fstream);
						  //out.write("Gene              " + "B=" + B + "%             "+ "FC=" + DFC + "_in%     "+ "FC=1/" + DFC + "_in%      "+"Exp_FC              " + "FPKM_1             " + "FPKM_2           \n");
						  
						  for(Object word : map.keySet()) {
							  out.write(word.toString());
							  List<String> s  = (List)map.get(word);
							  //Collections.sort(s);
							  Iterator itr = s.iterator(); 
							  while(itr.hasNext()) {
								  String element = (String)itr.next(); 
								  out.write( " , " + element);
							  }
							  out.write("\n");
						  }
						  out.close();
					  }catch (Exception e){//Catch exception if any
					  System.err.println("Error: " + e.getMessage());
					  }
				   } 	
					
		//remove empty space/element from the map
		Map clean_map (Map map){
			Map<String,List<String>> clean_map = new HashMap<String,List<String>>();
			
			  for(Object word : map.keySet()) {
				  List<String> s  = (List)map.get(word);
				  List<String> new_s = new ArrayList<String>();
				  Iterator itr = s.iterator(); 
				  while(itr.hasNext()) {
					  String element = (String)itr.next(); 
					  if (element.equals("U") || element.equals("B") || element.equals("1") || element.equals("0"))
					  	new_s.add(element);						  	
				  }
				  clean_map.put(word.toString().trim(),new_s);
			  }
			  return clean_map;
		}
		
		  /**
		   * Prints the contents of a Map containing
		   * geneIds and list containing the gene attribute.
		   * Just for debugging
		   */
		  void showMapDouble(Map map) {
			  
			  for(Object word : map.keySet()) {
				  System.out.print( word.toString()); System.out.print( "  :word ");
				  List<String> s  = (List)map.get(word);
				  Iterator itr = s.iterator(); 
				  while(itr.hasNext()) {
					  String element = (String)itr.next(); 
					  System.out.print( " , " + element); System.out.print( "  : 1 item ");
				  }
				  System.out.println();
			  }
		  } 		
			
		public static void main(String[] args) {
			
			  //Double B = 50.0;
			  Double B = Double.valueOf(args[0]);
			
			  //Double DFC = 2.0;
			  Double DFC = Double.valueOf(args[1]);

			  //String output_matrix="output_matrix";
			  String output_matrix = args[2];
			  
			  // change uhr and brain to sam1 and sam2
			  String filename1 = "log2_FC_Pmin_Pmax.txt";
			  
			  //String filename1 = "C:/Users/ytematetiagueu1/Dropbox/Java-Git/Local-RnaPhase/Allele_Frequency/log2_FC_Pmin_Pmax.txt";// will be results1
			  //String filename1 = "C:/Users/ytematetiagueu1/Dropbox/Java-Git/Local-RnaPhase/Allele_Frequency/gene_Direction.csv";// will be results1
			  		  
			  String filename2 = "Exp_log2_FC.txt";
			  
			  //String filename2 = "C:/Users/ytematetiagueu1/Dropbox/Java-Git/Local-RnaPhase/Allele_Frequency/Exp_log2_FC.txt"; // will be results1
			  //String filename2 = "C:/Users/ytematetiagueu1/Dropbox/Java-Git/Local-RnaPhase/Allele_Frequency/gene_DE.csv"; // will be results1
							
			  
			  //Log_Lower_Upper_Matching_Ratio
			  Output_Matrix elfc = new Output_Matrix();
			  
			  Map map_file1 = elfc.createMapGene(filename1);
			  Map map_file2 = elfc.createMapGene(filename2);
			  
			  System.out.println("Merging the two files !"); 
			  
			  Map merged_map = elfc.merge_map(map_file1, map_file2);
			  
			  elfc.write_output_matrix(B, DFC, merged_map, output_matrix);
			  //elfc.showMapDouble(elfc.clean_map(merged_map)); Just for debugging
			  //elfc.write_gene_Direction_DE(merged_map);
			  
			  System.out.println("Done !");
			  
		}  
		
		
      }
