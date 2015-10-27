package edu.uconn.engr.dna.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import edu.uconn.engr.dna.format.Clusters;
import edu.uconn.engr.dna.util.SimpleTokenizer;

public class DefaultClustersParser implements Parser<Clusters> {
	private static final String commentMarker = "#";
	
	@Override
	public Clusters parse(InputStream in) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		SimpleTokenizer st = new SimpleTokenizer('\t');
		Clusters clusters = new Clusters();
		line = br.readLine();
		if (line == null) {
			return clusters;
		}
//		int n = Integer.parseInt(line); // nr of clusters
		while (null != (line = br.readLine())) {
			if (line.startsWith(commentMarker)) {
				continue;
			}
			st.setLine(line);
			st.skipNext(); // skip number of read classes
			st.skipNext(); // skip number of reads
			int clusterSize = st.nextInt();
			String clusterName = "" + (1+clusters.size());
			for (int i = 0; i < clusterSize; ++i) {
				String iso = st.nextString();
				clusters.add(clusterName, iso);
			}
		}
		br.close();
		return clusters;
	}

}
