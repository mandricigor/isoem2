/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uconn.engr.dna.format;

import java.io.PrintWriter;
import java.util.Collection;

/**
 *
 * @author Sahar
 */
public class ExtrractUniqIsoformSubsequences {

	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.out.println("Arguments: transcriptsFastaFile outputFile outputSequenceLength");
			return;
		}


		System.out.println("reading transcript Sequences " + args[0] + "...");
		TaggedSequences ts = new ChromosomeSequences(args[0]);

		ts = new UniqIsoformSubSequences(ts,Integer.parseInt(args[2]));

		System.out.println("writing uniq isoform sequences to " + args[1] + "...");
		PrintWriter pw = new PrintWriter(args[1]);
                Collection<CharSequence> tags = ts.getAllTags();
                for (CharSequence isoformTag : tags) {
			CharSequence isoSequence = ts.getSequence(isoformTag);
			if (isoSequence == null) {
				continue;
			}
			pw.write(isoformTag.toString());
                        pw.write('\n');
                        pw.write(isoSequence.toString());
//            for (int i = 0; i < isoSequence.length(); i += 50) {
//			    pw.write(isoSequence.subSequence(i, Math.min(i+50, isoSequence.length())).toString());
//    			pw.write('\n');
//            }
		}
		pw.close();
		System.out.println("done.");
	}

}
