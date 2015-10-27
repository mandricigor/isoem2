package edu.uconn.engr.dna.isoem;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

public interface CoordParser {

	Map<String, List<Coord>> parse(Reader input) throws IOException;
}
