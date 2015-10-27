package edu.uconn.engr.dna.isoem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uconn.engr.dna.format.Isoform;
import edu.uconn.engr.dna.format.Isoforms;
import edu.uconn.engr.dna.sort.Sorter;
import edu.uconn.engr.dna.util.Intervals;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

public class EmUtils extends edu.uconn.engr.dna.util.EmUtils {
	public static final int BUFF_SIZE = 1 << 16;

	public static List<Coord> addCoords(Object objectName, Intervals exons,
					List<Coord> coordinates) {
		if (coordinates == null) {
			coordinates = new ArrayList<Coord>();
		}
		Coord previousCoord = null;
		int n = exons.size();
		for (int i = 0; i < n; ++i) {
			int pos = (int) exons.getStart(i);
			previousCoord = Coord.newNamedInstanceWithStoredLastGap(pos,
							objectName, previousCoord, CoordType.START);
			coordinates.add(previousCoord);

			pos = (int) exons.getEnd(i);
			previousCoord = Coord.newNamedInstanceWithStoredLastGap(pos,
							objectName, previousCoord, CoordType.END);
			coordinates.add(previousCoord);
		}
		return coordinates;
	}

	public static Map<String, List<Coord>> getIsoformCoords(Isoforms isoforms,
					Map<String, List<Coord>> coordinatesByReference) {
		return getIsoformCoords(isoforms.isoformIterator(),
						coordinatesByReference);
	}

	public static Map<String, List<Coord>> getIsoformCoords(
					Iterable<Isoform> isoformIterator,
					Map<String, List<Coord>> coordinatesByReference) {
		if (coordinatesByReference == null) {
			coordinatesByReference = new HashMap<String, List<Coord>>();
		}
		for (Isoform isoform : isoformIterator) {
			List<Coord> coordinates = coordinatesByReference.get(isoform.getChromosome());
			if (coordinates == null) {
				coordinatesByReference.put(isoform.getChromosome(),
								coordinates = new ArrayList<Coord>());
			}
			EmUtils.addCoords(isoform.getName(), isoform.getExons(),
							coordinates);
		}
		return coordinatesByReference;
	}

	public static void addTo(Map<String, List<Coord>> dest,
					Map<String, List<Coord>> source) {
		for (Map.Entry<String, List<Coord>> entry : source.entrySet()) {
			List<Coord> original = dest.get(entry.getKey());
			if (original == null) {
				dest.put(entry.getKey(), original = new ArrayList<Coord>());
			}
			original.addAll(entry.getValue());
		}
	}

	public static void sortIsoCoords(Map<String, List<Coord>> isoformCoords) {
		Sorter<Coord> s = new IsoformCoordSorter();
		for (Map.Entry<String, List<Coord>> entry : isoformCoords.entrySet()) {
			entry.setValue(s.sort(entry.getValue()));
		}
	}
	
	public static Reader getSamReader(String samReadsFile) throws IOException {
//		System.out.printf("Getting sam reader for <%s>\n", samReadsFile);
		Reader samReader;
		if (samReadsFile.endsWith(".gz")) {
			samReader = new InputStreamReader(new GZIPInputStream(new FileInputStream(samReadsFile)));
		} else {
                        if ("stdin".equals(samReadsFile)) { // yeah, I know it is a stupid hardcode
                            samReader = new InputStreamReader(System.in);
                        }
                        else {
			    samReader = new FileReader(samReadsFile);
                        }
		}
		samReader = new BufferedReader(samReader, BUFF_SIZE);
		return samReader;
	}

	public static Map<String, Double> computeTpms(Map<String, Double> frequencies) {
		Map<String, Double> tpms = new HashMap<String, Double>();
                double freqSum = 0.0;
		for (Map.Entry<String, Double> entry : frequencies.entrySet()) {
		    freqSum += entry.getValue();
		}
                for (Map.Entry<String, Double> entry: frequencies.entrySet()) {
                    double tpm = (entry.getValue() / freqSum) * 1000000; // tpm = fpkm * 10^6 / (sum of fpkms)
                    tpms.put(entry.getKey(), tpm);
                }
                return tpms;
	}




}
