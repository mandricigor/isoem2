package edu.uconn.engr.dna.isoem.accuracy;

import java.io.FileInputStream;

import edu.uconn.engr.dna.format.Isoform;
import edu.uconn.engr.dna.format.Isoforms;
import edu.uconn.engr.dna.io.DefaultIsoformsParser;
import edu.uconn.engr.dna.io.IsoformsParser;
import edu.uconn.engr.dna.util.Intervals;

public class IsoformAmbiguity {

	public static void main(String[] args) throws Exception {
		String isoformFile = args[0];
//		String isoformSequencesFile = args[1];
	
		System.out.println("Reading isoforms... ");
		IsoformsParser ip = new DefaultIsoformsParser();
		Isoforms isoforms = ip.parse(new FileInputStream(isoformFile));
		System.out.println("Read " + isoforms.size() + " isoforms");

//		System.out.println("reading isoform sequences... ");
//		TaggedSequences isoformSequences = new IsoformSequencesFromFile(
//				isoformSequencesFile, isoforms);
		
		System.out.println("checking inclusions");
		for (Isoform i1 : isoforms.isoformIterator()) {
			Intervals e1 = i1.getExons();
			for (Isoform i2 : isoforms.isoformIterator()) {
				Intervals e2 = i2.getExons();
				if (i1.getChromosome().equals(i2.getChromosome())
						&& i1 != i2 
						&& e1.getStart() <= e2.getStart()
						&& e2.getEnd()   <= e1.getEnd()) {
					if (included(e2, e1)) {
						System.out.println("Isoform " + i1.getName() 
								+ " fully contains " + i2.getName());
					}
				}
			}
		}
	}

	private static boolean included(Intervals small, Intervals large) {
		final int n = large.size();
		long s = small.getStart();
		int i = 0;
		while (i < n && s >= large.getStart(i)) {
			++i;
		}
		--i;
		if (large.getEnd(i) < s) {
			// falls within intron
			return false;
		}
		int m = small.size()-1;
		int j=0;
		for (; j < m && i < n-1; ++j, ++i) {
			if (small.getEnd(j) != large.getEnd(i)) {
				return false;
			}
			if (small.getStart(j+1) != large.getStart(i+1)) {
				return false;
			}
		}
		if (j < m) {
			return false;
		}
		return small.getEnd(j) <= large.getEnd(i);
	}
	
//	public static void main(String[] args) {
//		Intervals small = new Intervals(new Long[]{65378881L,65382143L,}, new Long[]{65380289L,65382677L,});
//		Intervals large = new Intervals(new Long[]{65378860L,65379700L,65379981L,65382143L,}, 
//				new Long[]{65379495L,65379777L,65380289L,65382677L,});
//		System.out.println(included(small, large));
//	}
}
