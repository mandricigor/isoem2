package edu.uconn.engr.dna.isoem;

import java.io.Reader;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jul 13, 2010
 * Time: 8:52:15 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IsoEMFlow {
    Map<String, Double> computeFpkms(Reader inputFile) throws Exception;
}
