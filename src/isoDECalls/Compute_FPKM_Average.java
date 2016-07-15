
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * 
 * @author Charly Blanche Temate
 * Class generates the mean of all "One run" of fpkm values for all the replicates inside a given condition
 * Input is a directory containing "one run" FPKM of the different replicates (i.e. without bootstrapping)
 * output: generate a file containing the average of the FPKM from all those replicates
 */
public class Compute_FPKM_Average {
	
	
	/**
	   * Create a map from the input file to store geneID
	   * Map: key: geneID, value: empty list (to later contain the FPKM values)
	   * @param filename: file contains gene ID , and the 1 FPKM value from one run of IsoEm
	   *  The file to use to extract gene Id is the first file in the directory
	   * @return
	   */
	  Map<String,List<Double>> createMapGene(String dir) {
		
		Map<String,List<Double>> map = new HashMap<String,List<Double>>();
	    int error = 0;
		

		  File directory = new File(dir);      
		  File[] myarray;
		  myarray=directory.listFiles();

		  try{		  

				  File path= myarray[0]; 
				  FileReader fr = new FileReader(path);
				  BufferedReader br = new BufferedReader(fr);

				  
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
							 map.put(geneId,s);
					      }
				  }
				  fr.close();
				  
				  if (error != 0) {
					  System.out.println("create200Expre: nber of error before end " + error );
				  }

		  }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
		  }
		  

	    //System.out.println("Size of map (createMapGene): " + map.size() );	    //no error
	    Map<String,List<Double>> sortedMap = new TreeMap(map); //sort the map
		return sortedMap;
	  }
	
	


	  
		//Create a map with geneId as key and value is a set containing all the fpkm for one run files 
		//The output is the map
		
		  Map<String,List<Double>> create_List_Onerun_FPKM(Map<String,List<Double>> map, String dir) {


			  File directory = new File(dir);      
			  File[] myarray;
			  myarray=directory.listFiles();
			  int error=0;
			  
			  System.out.println("Nber of files in dir (create_List_Onerun_FPKM): " + myarray.length);
			  
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
					  System.out.println("createFPKMAverage: nber of error before end " + error );
					  break;
				  }
			  }
			  System.out.println("createFPKMAverage: nber of error  " + error );
			  return map;
		  }
	  
	  
	  
			//Create a map with geneId as key and value is the average of FPKM value of One run
			//The output is a map
			
		Map<String,Double> create_Average_FPKM(Map<String,List<Double>> map) {
			Map<String,Double> outMap = new HashMap<String,Double>();
		    int nberGenes=0;
		    double total;
		    
		    for(Object word: map.keySet()) {
				
				List<Double> listFPKM = (List) map.get(word);
				nberGenes++;
				total=0.0;
				
				for (int i =0; i< listFPKM.size(); i++){	 				//listUhr.size() = nber of runs of bootstrap
					total = total + listFPKM.get(i);
				}
				outMap.put(word.toString(), total/listFPKM.size());
			} 
				
			System.out.println("create_Average_FPKM nber of genes:  " + nberGenes );
			return outMap;
		 }	  
	  
	  
		/**
		 * Prints the contents of the One_run Map containing
		 *  GeneId, average FPKM
		 */
			void write_OneRun_File(Map<String,Double> map, String output_file) {  //convert this to write to file
				try{
					  // Create file 
						
					  FileWriter fstream = new FileWriter(output_file);
					  BufferedWriter out = new BufferedWriter(fstream);
					  
					  for(Object word : map.keySet()) {
						  out.write(word.toString()+ "  ");
						  Double s  = map.get(word);
						  out.write(s.toString() + "\n");
					  }
					  out.close();
				  }catch (Exception e){//Catch exception if any
				  System.err.println("Error: " + e.getMessage());
				  }
			   } 	
		  
		
	
			
		public static void main(String[] args) {
			
			//String dir= "C:/Users/ytematetiagueu1/Desktop/Scripts_test_new/Sam2_Brain";
			
			String dir= args[0];
			
			Compute_FPKM_Average cfa = new Compute_FPKM_Average();
			
			Map<String,List<Double>> map = cfa.createMapGene(dir);
			
			Map <String,List<Double>> mapList = cfa.create_List_Onerun_FPKM(map, dir);
			
			Map<String,Double> mapFPKM = cfa.create_Average_FPKM(mapList);
			
			cfa.write_OneRun_File(mapFPKM, "average.gene_estimates");
			
			System.out.println("Done !");
			  
		}  
		
		
      }