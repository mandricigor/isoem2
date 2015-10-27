/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uconn.engr.dna.format;

import edu.uconn.engr.dna.util.Intervals;
import edu.uconn.engr.dna.util.Utils;
import java.util.Collection;


/**
 *
 * @author Sahar
 */
public class UniqIsoformSubSequences extends AbstractTaggedSequences{
	public UniqIsoformSubSequences(TaggedSequences transcripts, int length) {
            Collection<CharSequence> tags = transcripts.getAllTags();
            int count = 0;
            for (CharSequence isoformTag : tags) {
                int isoformLength = transcripts.getSequence(isoformTag).length();
                boolean uniqFound=false;
                for (int i = 1; i < isoformLength - length+1; i++) {
                    CharSequence candidate = transcripts.getSequence(isoformTag, i, i+length-1);
                    boolean unique = true;
                    for (CharSequence otherIsoformTag : tags) {
                        if (!otherIsoformTag.toString().equals(isoformTag))
                            if (transcripts.getSequence(otherIsoformTag).toString().contains(candidate)) {
                                unique = false;
                                break;
                            }
                     }
                    if (unique) {
                        sequencesByTag.put(isoformTag, candidate);
                        System.out.println(isoformTag+"\t"+candidate);
                        uniqFound = true;
                        break;
                    }

                 }
                 if (!uniqFound) {
                     sequencesByTag.put(isoformTag,"NONE");
                     System.out.println(isoformTag+"\t NONE");
                }
                 count++;
                 if ((count % 100) == 0)
                     System.out.println(count+" transcripts processed");
                }

    }
}
