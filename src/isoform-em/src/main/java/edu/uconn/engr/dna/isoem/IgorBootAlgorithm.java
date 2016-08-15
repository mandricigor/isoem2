package edu.uconn.engr.dna.isoem;

import edu.uconn.engr.dna.util.Utils;

import java.util.*;

public class IgorBootAlgorithm implements BootAlgorithm {

        List<List<IsoformList>> clusters;
        Integer bootCount;
        Map<String, Integer> m;
        List<String> bootArray;

	public IgorBootAlgorithm(List<List<IsoformList>> clusters, Integer bootCount, Map<String, Integer> m, List<String> bootArray) {
            this.clusters = clusters;
            /*
            this.clusters = new ArrayList<List<IsoformList>>();
            for (List<IsoformList> cluster: clusters) {
                List<IsoformList> cluster2 = new ArrayList<IsoformList>();
                for (IsoformList iso: cluster) {
                    ArrayIsoformList iso2 = new ArrayIsoformList((ArrayIsoformList)iso);
                    cluster2.add(iso2);
                }
                clusters.add(cluster2);
            }
            */
            this.bootCount = bootCount;
            this.m = m;
            this.bootArray = bootArray;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
        public List<List<IsoformList>> doBootstrapClusters(Integer bootIterationId) {
            // map m stands for multiplicities of each read
            List<List<IsoformList>> igor_clusters = new ArrayList<List<IsoformList>>();
            Map<String, Integer> bootMultiplicities = new HashMap<String, Integer>();
            for (Map.Entry<String, Integer> entry: m.entrySet()) {
                bootMultiplicities.put(entry.getKey(), 0);
            }
            for (int k = 0; k < bootCount; k ++) {
                int p = generateRandomNber(0, bootCount - 1);
                String pickedRead = bootArray.get(p);
                int currentCount = bootMultiplicities.get(pickedRead);
                bootMultiplicities.put(pickedRead, currentCount + 1);
            }
            for (int ii = 0; ii < clusters.size(); ii ++) {
                ArrayList<IsoformList> super_igor_isoform_list = new ArrayList<IsoformList>();
                for (int jj = 0; jj < clusters.get(ii).size(); jj ++) {
                    ArrayIsoformList aiso = (ArrayIsoformList) (clusters.get(ii).get(jj));
                    String aisoName = Integer.toString(ii) + "_" + Integer.toString(jj);
                    if (bootMultiplicities.get(aisoName) > 0) {
                        ArrayIsoformList new_aiso = new ArrayIsoformList(aiso);
                        new_aiso.setMultiplicity(bootMultiplicities.get(aisoName));
                        new_aiso.bootstrapId = bootIterationId;
                        super_igor_isoform_list.add(new_aiso);
                    }
                }
                igor_clusters.add(super_igor_isoform_list);
            }
            return igor_clusters;
        }

        public static int generateRandomNber(int aStart, int aEnd){
                Random random = new Random();
            long range = (long)aEnd - (long)aStart + 1; //get the range, casting to long to avoid overflow problems         
            long fraction = (long)(range * random.nextDouble()); // compute a fraction of the range, 0 <= frac < range
            int randomNumber =  (int)(fraction + aStart);
            return randomNumber;
        }

}
