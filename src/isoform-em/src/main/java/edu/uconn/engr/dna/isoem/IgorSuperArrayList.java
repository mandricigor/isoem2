package edu.uconn.engr.dna.isoem;

import java.util.*;

class IgorSuperArrayList<T> extends ArrayList<T> {

    private int bootstrapId;

    public void setBootstrapId(int id) {
        bootstrapId = id;
    }

    public int getBootstrapId() {
        return bootstrapId;
    }
}


