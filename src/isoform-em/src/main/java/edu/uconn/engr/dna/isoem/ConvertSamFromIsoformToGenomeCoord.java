package edu.uconn.engr.dna.isoem;

import edu.uconn.engr.dna.format.Clusters;
import edu.uconn.engr.dna.format.Isoforms;
import edu.uconn.engr.dna.io.GTFParser;
import edu.uconn.engr.dna.io.GenesAndIsoformsParser;
import edu.uconn.engr.dna.util.Pair;
import edu.uconn.engr.dna.util.ParameterRunnable;
import edu.uconn.engr.dna.util.ParameterRunnableFactory;
import edu.uconn.engr.dna.util.Utils;

import java.io.*;
import java.util.List;

public class ConvertSamFromIsoformToGenomeCoord {

	public static void main(String[] args) throws Exception {
                Integer polyATailLen = 0;
                Boolean ignorePairing = false;
// should new option be documented??
      		if (args.length < 2 || args.length > 3) {
			System.out.println("Usage: isoformsGTF outputSam [polyATailLength]");
			System.exit(1);
		}
                int args_index=0;
		GenesAndIsoformsParser giParser = new GTFParser();
		Pair<Clusters, Isoforms> p = giParser.parse(new FileInputStream(args[args_index++]));
		Isoforms isoforms = p.getSecond();
//sahar debug
//                System.err.println("before Adding fake polyA tail exons length of NR_003363 = "+isoforms.getValue("NR_003363").getExons().size());

		Reader samInIsoCoord;
		int buffSize = 1 << 16;
		samInIsoCoord = new BufferedReader(new InputStreamReader(System.in), buffSize);

		Writer samInGenomeCoord;
		samInGenomeCoord = new BufferedWriter(new FileWriter(args[args_index++]), buffSize);

		if (args.length > args_index) {
                        if (args[args_index].equals("--ignore-pairing")) {
                            ignorePairing = true;
                            args_index++;
                        }
		}
		if (args.length > args_index) {
			polyATailLen = Integer.parseInt(args[args_index]);
			Utils.addFakePolyAToExons(isoforms, polyATailLen.intValue());
		}

//sahar debug
//                System.err.println("after Adding fake polyA tail exons length of NR_003363 = "+isoforms.getValue("NR_003363").getExons().size());
		int nThreads = Runtime.getRuntime().availableProcessors();
//		int nThreads = 1 ;//Runtime.getRuntime().availableProcessors();
		new ThreadPooledSamParser<Void>(
						nThreads, Math.max(10, 3 * nThreads),
						1 << 16, ParameterRunnableFactory.instance(IsoToGenomeParameterRunnable.class,
						isoforms, samInGenomeCoord, polyATailLen,ignorePairing)).parse(samInIsoCoord);
		samInGenomeCoord.close();
	}

	public static class IsoToGenomeParameterRunnable implements ParameterRunnable<List<String>, Void> {

		private SamIsoformToGenomeConverter isoToGenomeConverter;
		private DuplicatesRemovingConverter duplicatesRemovingConverter;
		private final Writer out;
//sahar big fix for pairs of unequal length; pairing ignored because mate length is needed (and not available) to convert coordiates
		public IsoToGenomeParameterRunnable(Isoforms isoforms, Writer out, Boolean ignorePairs) {
			this.out = out;
			isoToGenomeConverter = new SamIsoformToGenomeConverter(
							isoforms, ignorePairs);
			duplicatesRemovingConverter = new DuplicatesRemovingConverter();
		}

//sahar big fix for pairs of unequal length; pairing ignored because mate length is needed (and not available) to convert coordiates
		public IsoToGenomeParameterRunnable(Isoforms isoforms, Writer out, Integer polyAtail, Boolean ignorePairs) {
			this.out = out;
			isoToGenomeConverter = new SamIsoformToGenomeConverter(
							isoforms, polyAtail.intValue(), ignorePairs);
			duplicatesRemovingConverter = new DuplicatesRemovingConverter();
		}

		@Override
		public void run(List<String> item) {
			int j = 0;
//			System.err.println("in run");
			synchronized (item) {
				for (int i = 0, n = item.size(); i < n; ++i) {
					String s = item.get(i);
					if (s == null || s.startsWith("@")) {
						continue;
					}

					try {
						//System.out.println("converting line " + s);
						s = isoToGenomeConverter.convert(s);
					} catch (Exception e) {
						System.err.println("Error converting line " + s);
//sahar
//bug fux - uncoverted alignment was printed if error occurs
						s = null;
						e.printStackTrace();
//sahar debug; exit if error occurs
//						return;						
					}
					if (s != null) {
						//System.out.println("converted to " + s);
						s = duplicatesRemovingConverter.convert(s);
						if (s != null) {
							//System.out.println("not a duplicate " + s);
							item.set(j++, s);
						}
					}
				}
			}
			synchronized (out) {
				try {
					for (int i = 0; i < j; ++i) {
						//System.out.println("writing " + item.get(i));
						out.write(item.get(i));
						out.write('\n');
					}
					out.flush();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			duplicatesRemovingConverter.clear();
		}

		@Override
		public Void done() {
			return null;
		}
	}
}
