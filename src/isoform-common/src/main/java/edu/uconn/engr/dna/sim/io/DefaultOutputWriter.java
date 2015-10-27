package edu.uconn.engr.dna.sim.io;

import edu.uconn.engr.dna.genread.io.OutputWriter;
import edu.uconn.engr.dna.sim.format.NamingConvention;
import edu.uconn.engr.dna.sim.format.ReadFormatter;
import edu.uconn.engr.dna.sim.Read;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;


public class DefaultOutputWriter implements OutputWriter {

	private ReadFormatter rof;
	private NamingConvention nc;
	private boolean openedFiles;
	private Writer[] writers;
	private OutputWriter decorator;
	private boolean showHeader;

	/**
	 * Decorator version of the constructor - also forwards
	 * reads to the decorator OutputWriter for writing
	 *
	 * @param rof
	 * @param nc
	 * @param decorator
	 * @param showTableHeader
	 */
	public DefaultOutputWriter(ReadFormatter rof, NamingConvention nc,
			OutputWriter decorator, boolean showTableHeader) {
		this.rof = rof;
		this.nc = nc;
		this.decorator = decorator;
		this.showHeader = showTableHeader;
	}

	@Override
	public void writeReads(Read... reads) throws IOException {
		if (!openedFiles) {
			openFiles();
			if (showHeader) {
				for (int i = 0; i < reads.length; ++i) {
					writers[i].write(rof.getTableHeader());
				}				
			}
		}
		for (int i = 0; i < reads.length; ++i) {
			writers[i].write(rof.format(reads[i]));
		}
		if (decorator != null) {
			decorator.writeReads(reads);
		}
	}

	private void openFiles() throws IOException {
        if (nc == null) {
            writers = new PrintWriter[1];
            writers[0] = new PrintWriter(System.out);
        } else {
		    String[] files = nc.getFileNames();
            writers = new PrintWriter[files.length];
		    for (int i = 0; i < files.length; ++i) {
			    writers[i] = new PrintWriter(files[i]);
		    }
        }
		openedFiles = true;
	}
	
	@Override
	public void close() throws IOException {
		if (openedFiles) {
			for (int i = 0; i < writers.length; ++i) {
			    writers[i].close();
			}
		}
		if (decorator != null) {
			decorator.close();
		}
	}

}
