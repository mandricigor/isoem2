
// Author: Igor Mandric

package edu.uconn.engr.dna.isoem;

import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.HashMap;
import java.lang.Math;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;


import edu.uconn.engr.dna.util.Pair;

class IntervalTracker {
 
        private int confidence_rank;
        private LinkedList<Double> minGuys;
        private LinkedList<Double> maxGuys;

        public IntervalTracker(int confidence_rank) {
            this.confidence_rank = confidence_rank;
            minGuys = new LinkedList<Double>();
            maxGuys = new LinkedList<Double>();
        }
        public void push_value(double value) {
            push_min(value);
            push_max(value);
        }
        public double get_min() {
            return minGuys.get(0);
        }
        public double get_max() {
            return maxGuys.get(0);
        }
        @Override
        public String toString() {
            return get_min() + "\t" + get_max();
        }
        private void push_min(double value) {
            if (minGuys.size() == 0) {
                minGuys.add(value);
            }
            else {
                int position = 0;
                while (position < minGuys.size()) {
                    if (value >= minGuys.get(position)) {
                        break;
                    }
                    position ++; 
                }
                minGuys.add(position, value);
                if (minGuys.size() > confidence_rank) {
                    minGuys.pollFirst();
                }
            }
        }
        private void push_max(double value) {
            if (maxGuys.size() == 0) {
                maxGuys.add(value);
            }
            else {
                int position = 0;
                while (position < maxGuys.size()) {
                    if (value <= maxGuys.get(position)) {
                        break;
                    }
                    position ++; 
                }
                maxGuys.add(position, value);
                if (maxGuys.size() > confidence_rank) {
                    maxGuys.pollFirst();
                }
            }
        }
}



public class ConfidenceIntervalCalculator {
        private static final int sigturn = 63;

        private int counter; // used to see whether we did not overcome 200 bootstraps
        private int small_counter;
        private int confidence;

        private Map<String, IntervalTracker> gene_fpkm;
        private Map<String, IntervalTracker> gene_tpm;
        private Map<String, IntervalTracker> gene_ecpm;

        private Map<String, IntervalTracker> iso_fpkm;
        private Map<String, IntervalTracker> iso_tpm;
        private Map<String, IntervalTracker> iso_ecpm;

        private void initializeStorage(Map<String, IntervalTracker> storage, Map<String, Double> freq) {
            int confidence_rank = (int) Math.ceil(100.0 * (1.0 - 0.01 * confidence));
            for (Map.Entry<String, Double> entry: freq.entrySet()) {
                storage.put(entry.getKey(), new IntervalTracker(confidence_rank));
            }
        }


        private void updateStorage(Map<String, IntervalTracker> storage, Map<String, Double> freq, int signature) {
           /*
            * signature is used to check if we updated all the storages
            */
            if (storage.size() == 0) {
                initializeStorage(storage, freq);
            }
            for (Map.Entry<String, Double> entry: freq.entrySet()) {
                IntervalTracker iTracker = storage.get(entry.getKey());
                if (iTracker != null) {
                    iTracker.push_value(entry.getValue());
                }
            }
            small_counter += signature;
            if (small_counter == sigturn) {
                counter ++;
                small_counter = 0; // reset it here
            }
        }

        private Map<String, Pair<Double, Double>> getCI(Map<String, IntervalTracker> storage) {
            Map<String, Pair<Double, Double>> storage_ci = new HashMap<String, Pair<Double, Double>>();
            for (Map.Entry<String, IntervalTracker> entry: storage.entrySet()) {
                IntervalTracker iTracker = entry.getValue();
                Pair<Double, Double> pair = new Pair<Double, Double>(iTracker.get_min(), iTracker.get_max());
                storage_ci.put(entry.getKey(), pair);
            }
            return storage_ci;
        }

        public ConfidenceIntervalCalculator(int confidence) {
            this.confidence = confidence;
            this.counter = 0;
            this.small_counter = 0;
            gene_fpkm = new HashMap<String, IntervalTracker>();
            gene_tpm = new HashMap<String, IntervalTracker>();
            gene_ecpm = new HashMap<String, IntervalTracker>();
            iso_fpkm = new HashMap<String, IntervalTracker>();
            iso_tpm = new HashMap<String, IntervalTracker>();
            iso_ecpm = new HashMap<String, IntervalTracker>();
        }

        public void reset() {
            counter = 0;
            small_counter = 0;
            gene_fpkm.clear();
            gene_tpm.clear();
            gene_ecpm.clear();
            iso_fpkm.clear();
            iso_tpm.clear();
            iso_ecpm.clear();
        }

        public void updateGeneFpkm(Map<String, Double> freq) {
            if (counter < 200) {
                updateStorage(gene_fpkm, freq, 1);
            }
        }

        public void updateGeneTpm(Map<String, Double> freq) {
            if (counter < 200) {
                updateStorage(gene_tpm, freq, 2);
            }
        }

        public void updateGeneEcpm(Map<String, Double> freq) {
            if (counter < 200) {
                updateStorage(gene_ecpm, freq, 4);
            }
        }

        public void updateIsoFpkm(Map<String, Double> freq) {
            if (counter < 200) {
                updateStorage(iso_fpkm, freq, 8);
            }
        }

        public void updateIsoTpm(Map<String, Double> freq) {
            if (counter < 200) {
                updateStorage(iso_tpm, freq, 16);
            }
        }

        public void updateIsoEcpm(Map<String, Double> freq) {
            if (counter < 200) {
                updateStorage(iso_ecpm, freq, 32);
            }
        }

        public Map<String, Pair<Double, Double>> getGeneFpkmCI() {
            return getCI(gene_fpkm);
        }

        public Map<String, Pair<Double, Double>> getGeneTpmCI() {
            return getCI(gene_tpm);
        }

        public Map<String, Pair<Double, Double>> getGeneEcpmCI() {
            return getCI(gene_ecpm);
        }

        public Map<String, Pair<Double, Double>> getIsoFpkmCI() {
            return getCI(iso_fpkm);
        }

        public Map<String, Pair<Double, Double>> getIsoTpmCI() {
            return getCI(iso_tpm);
        }

        public Map<String, Pair<Double, Double>> getIsoEcpmCI() {
            return getCI(iso_ecpm);
        }

        public void writeValues(String dirname) {
            writeValues(sortEntriesById(gene_fpkm), dirname + "gene_fpkm_ci");
            writeValues(sortEntriesById(gene_tpm), dirname + "gene_tpm_ci");            
            //writeValues(sortEntriesById(gene_ecpm), dirname + "gene_ecpm_ci");            
            writeValues(sortEntriesById(iso_fpkm), dirname + "iso_fpkm_ci");            
            writeValues(sortEntriesById(iso_tpm), dirname + "iso_tpm_ci");            
            //writeValues(sortEntriesById(iso_ecpm), dirname + "iso_ecpm_ci");            
        }

        public List<Map.Entry<String, IntervalTracker>> sortEntriesById(Map<String, IntervalTracker> freq) {
                List<Map.Entry<String, IntervalTracker>> entries = new ArrayList<Map.Entry<String, IntervalTracker>>(
                                freq.entrySet());
                Collections.sort(entries, new Comparator<Map.Entry<String, IntervalTracker>>() {
                        @Override
                        public int compare(Map.Entry<String, IntervalTracker> o1,
                                        Map.Entry<String, IntervalTracker> o2) {
                                return o1.getKey().compareTo(o2.getKey());
                        }
                });
                return entries;
        }

        public void writeValues(Collection<Map.Entry<String, IntervalTracker>> entries,
                        String outputFileName) {
                String dirName = null;
                int lastIndexOfSlash = outputFileName.lastIndexOf("/");
                if (lastIndexOfSlash != -1) {
                    dirName = outputFileName.substring(0, lastIndexOfSlash);
                }
                try {
                    File f = new File(dirName);
                    f.mkdirs();
                    Writer writer = new PrintWriter(outputFileName);
                    for (Map.Entry<?, ?> entry : entries) {
                        writer.write(String.valueOf(entry.getKey()));
                        writer.write("\t");
                        writer.write(String.valueOf(entry.getValue()));
                        writer.write("\n");
                    }
                    writer.close();
                } catch(IOException e) {
                    e.printStackTrace(System.out);
                }
        }
}


