package edu.uconn.engr.dna.isoem;

import edu.uconn.engr.dna.util.RandomAccessMap;

import java.util.Collection;
import java.util.List;

public interface IsoformListClusteringAlgo {

    List<List<IsoformList>> findReadClassClusters(Collection<IsoformList> rcs, RandomAccessMap<String, ?> ids) ;
}
