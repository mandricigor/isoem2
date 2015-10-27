/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uconn.engr.dna.io;

import edu.uconn.engr.dna.common.Utils;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Random;
import edu.uconn.engr.dna.probability.CustomWeightItemPicker;

/**
 *
 * @author Sahar
 */
public class FastaParser extends AbstractParser implements Parser{
private CustomWeightItemPicker<String> SNP_TF_ItemPicker;
private Random randomNumberGenerator;
private FileInputStream inputStream;
private FileOutputStream outputStream;
private static int line_num;
private static String Chr;
private static int NumberOfChrBasesParsed;


    public FastaParser(Random randomNumberGenerator, CustomWeightItemPicker<String> SNP_TF_ItemPicker) {
        this.randomNumberGenerator = randomNumberGenerator;
        this.SNP_TF_ItemPicker = SNP_TF_ItemPicker;
        line_num = 0;

    }

    @Override
    public Integer parse(String inFileName) throws Exception {
        StringBuilder outFileName = new StringBuilder();
        outFileName.append(inFileName);
        outFileName.append(".SNV");
        inputStream = new FileInputStream(inFileName);
        outputStream = new FileOutputStream(outFileName.toString());
	super.read(inputStream);
        outputStream.flush();
        inputStream.close();
        outputStream.close();
        return (0);
    }

    @Override
    public void processLine(String line) throws Exception {
        StringBuilder newLine = new StringBuilder();
	if (line.isEmpty()) {
		return ;
	}
        if (line.charAt(0) == '>') {
            writeLine(line);
            line_num++;
            Chr= line.substring(1);
            NumberOfChrBasesParsed = 0;
            return;
        }
        line_num++;
        for (int i= 0; i < line.length(); i++) {
            char iBase = Utils.computeBase(line.charAt(i),SNP_TF_ItemPicker,randomNumberGenerator);
            newLine.append(iBase);
            if (iBase != line.charAt(i))
                //System.out.println("SNP at line number "+line_num+" column "+(i+1));
                System.out.println(Chr+"\t"+(NumberOfChrBasesParsed+i+1)+"\t"+line.charAt(i)+"\t"+iBase);
        }
        NumberOfChrBasesParsed+=line.length();
        writeLine(newLine.toString());


    }
    protected void writeLine(String line) throws Exception {
        String newLine = "\n";
        outputStream.write(line.getBytes(),0,line.length());
        outputStream.write(newLine.getBytes(),0,newLine.length());

}

    @Override
    protected void handleException(String line, Exception e) {
	System.err.println("Unexpected read position number");
        System.err.println(line);
	e.printStackTrace();
}

}
