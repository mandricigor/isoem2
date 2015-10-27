/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uconn.engr.dna.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 *
 * @author Sahar
 */
public class FastatoGTFParser extends AbstractParser implements Parser{
private FileInputStream inputStream;
private FileOutputStream outputStream;
private static int SeqSize;
private static String SeqName;


    public FastatoGTFParser() {
        SeqSize = 0;

    }

    @Override
    public Integer parse(String inFileName) throws Exception {
        StringBuilder outFileName = new StringBuilder();
        outFileName.append(inFileName);
        outFileName.append(".GTF");
        inputStream = new FileInputStream(inFileName);
        outputStream = new FileOutputStream(outFileName.toString());
	super.read(inputStream);
        if (SeqSize != 0)  //write last reference sequence
                writeLine(SeqName,SeqSize);
        outputStream.flush();
        inputStream.close();
        outputStream.close();
        return (0);
    }

    @Override
    public void processLine(String line) throws Exception {
	if (line.isEmpty()) {
		return ;
	}
        if (line.charAt(0) == '>') {
            if (SeqSize != 0)
                writeLine(SeqName,SeqSize);
	     if (line.indexOf(' ') != -1)
	            SeqName = line.substring(1,line.indexOf(' '));
	     else
	            SeqName = line.substring(1);
            SeqSize = 0;
        }
        else
            SeqSize+=line.length();
    }
    protected void writeLine(String SeqName, int length) throws Exception {
        String line = SeqName+"\tfastaToGT\texon\t1\t"+Integer.toString(length)+"\t0.00\t +\t.\tgene_id \""+SeqName+"\"; transcript_id \""+SeqName+"\";\n";
        outputStream.write(line.getBytes(),0,line.length());

}

    @Override
    protected void handleException(String line, Exception e) {
	System.err.println("Unexpected read position number");
        System.err.println(line);
	e.printStackTrace();
}

}
