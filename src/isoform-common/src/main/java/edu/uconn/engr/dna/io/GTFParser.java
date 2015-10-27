package edu.uconn.engr.dna.io;

import edu.uconn.engr.dna.format.Clusters;
import edu.uconn.engr.dna.format.Isoform;
import edu.uconn.engr.dna.format.Isoforms;
import edu.uconn.engr.dna.util.Pair;
import edu.uconn.engr.dna.util.SimpleTokenizer;

import java.io.InputStream;

public class GTFParser extends AbstractParser implements GenesAndIsoformsParser {

    private static final String lineFormat =
            "chrom\tsource\ttype\tstart\tend\tscore\tstrand\tframe\tgene_id \"gene\"; transcript_id \"isoform\";";

    private Clusters clusters;
    private Isoforms isoforms;

    private SimpleTokenizer tok;
    private SimpleTokenizer stok;
    private boolean storeIndividualExons;
		private String origin;

    public GTFParser(boolean storeIndividualExons) {
        this();
        this.storeIndividualExons = storeIndividualExons;
    }

    public GTFParser() {
        this.clusters = new Clusters();
        this.isoforms = new Isoforms();
        this.tok = new SimpleTokenizer('\t');
        this.stok = new SimpleTokenizer("\t ");
    }

    @Override
    public Pair<Clusters, Isoforms> parse(InputStream in) throws Exception {
        super.read(in);
        return new Pair<Clusters, Isoforms>(clusters, isoforms);
    }

    @Override
    protected void processLine(String line) {
        tok.setLine(line);
        String chrom = tok.nextString();
        if (origin == null) {
					origin = tok.nextString();
				} else {
					tok.skipNext(); 
				}
        String type = tok.nextString();
//        System.out.println("type "+type);
//	 System.out.println("type size"+type.length());
        if ("exon".equals(type)) {
            int start = tok.nextInt();
            int end = tok.nextInt();
            tok.skipNext(); // score
            char strand = tok.nextString().charAt(0);
            tok.skipNext(); // frame
            String geneAndIsoform = tok.rest().trim();

            stok.setLine(geneAndIsoform);
            stok.skipNext(); // gene_id
            String geneId = stok.nextString();
            geneId = geneId.substring(1, geneId.length() - 2);

            stok.skipNext(); // transcript_id
            String isoformId = stok.nextString();
             isoformId = isoformId.substring(1, isoformId.length() - 2);
            
            if (storeIndividualExons) {
                // HACK!
                isoformId += ":" + isoforms.size();
                // HACK!
            }


            Isoform isoform = isoforms.getValue(isoformId);
//            System.out.println("isoform"+isoformId);
            if (isoform == null) { // first exon
                isoforms.add(isoformId, isoform = new Isoform(isoformId,
                        chrom, strand, null, null));
            } else {
                if (strand != isoform.getStrand()
                        || !chrom.equals(isoform.getChromosome())) {
                    //throw new IllegalStateException
                    // changed to print warning and continue
                    System.out.println("Warning: Exons of isoform "
                            + isoformId
                            + " are not consistent with respect to strand and sequence name\n"
                            + "Expected seqname " + isoform.getChromosome()
                            + " and strand \"" + isoform.getStrand()
                            + "\"\n     got seqname " + chrom + " and strand \"" + strand + "\"\nin line:\n"
                            + line
                            + "\n exon ignored");
                            return;
                }
            }
//	     System.out.println(isoformId+" "+start+" "+end);
            isoform.addExon(start, end);

            clusters.add(geneId, isoformId);
//            System.out.println("size of clusters "+clusters.size());
        }
    }

    @Override
    protected void handleException(String line, Exception e) throws Exception {
        if (!(e instanceof IllegalStateException)) {
            System.err.println("Invalid file format! Expected: " + lineFormat);
            System.err.println("Got " + line);
            e.printStackTrace();
        }
        throw e;
    }

	public String getOrigin() {
		return origin;
	}

		
}
