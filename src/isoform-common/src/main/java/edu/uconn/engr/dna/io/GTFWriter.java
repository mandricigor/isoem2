/*
 *  Copyright 2011 marius.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package edu.uconn.engr.dna.io;

import edu.uconn.engr.dna.util.Intervals;
import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author marius
 */
public class GTFWriter {

	private static final char fs = '\t';
	private final Writer w;
	private final String source;

	public GTFWriter(Writer w, String source) {
		this.w = w;
		this.source = source;
	}

//	chr1    hg19_ensGene    exon    66999066        66999090        0.000000        +       .       gene_id "ENST00000237247"; transcript_id "ENST00000237247";
// "chrom\tsource\ttype\tstart\tend\tscore\tstrand\tframe\tgene_id \"gene\"; transcript_id \"isoform\";";
	public void writeExon(String chromosome, int start, int end, double score, char strand, String geneId, String transcriptId) throws IOException {
		w.write(chromosome);
		w.write(fs);
		w.write(source);
		w.write(fs);
		w.write("exon");
		w.write(fs);
		w.write("" + start);
		w.write(fs);
		w.write("" + end);
		w.write(fs);
		w.write(""+score);
		w.write(fs);
		w.write(strand);
		w.write(fs);
		w.write('.');
		w.write(fs);
		w.write("gene_id \"");
		w.write(geneId);
		w.write("\"; transcript_id \"");
		w.write(transcriptId);
		w.write("\";\n");
	}

	public void writeExons(String chromosome, Intervals exons, double score, char strand, String geneId, String transcriptId) throws IOException {
		for (int i = 0; i < exons.size(); ++i) {
			writeExon(chromosome, exons.getStart(i), exons.getEnd(i), score,
					strand, geneId, transcriptId);
		}
	}
}
