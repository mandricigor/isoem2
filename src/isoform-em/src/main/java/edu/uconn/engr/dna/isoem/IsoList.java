package edu.uconn.engr.dna.isoem;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class IsoList {

    public static final IsoList EmptyIsoList = new IsoList();
    private static final Comparable[] emptyArray = new Comparable[0];
    private int multiplicity;
    private Comparable[] isoform;
    private double[] value;

    public IsoList() {
        this.multiplicity = 1;
        this.isoform = emptyArray;
    }

    public IsoList(IsoList isoList) {
        this.copyFrom(isoList);
    }

    public IsoList(Collection<Object> isoforms, double defaultValue) {
        isoform = isoforms.toArray(new Comparable[isoforms.size()]);
        Arrays.sort(isoform);
        value = new double[isoform.length];
        Arrays.fill(value, defaultValue);
    }

    public boolean isEmpty() {
        return isoform.length == 0;
    }

    public int size() {
        return isoform.length;
    }

    public void clear() {
        this.isoform = emptyArray;
        this.value = null;
    }

    public Object getIsoform(int i) {
        return isoform[i];
    }

    public double getValue(int i) {
        return value[i];
    }

    public void setValue(int i, double val) {
        value[i] = val;
    }

    public int getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(int m) {
        this.multiplicity = m;
    }

    public void addAll(IsoList other) {
        if (other == null || other.isEmpty()) {
            return;
        }
        if (this.isEmpty()) {
            copyFrom(other);
            return;
        }

        // merge the two sorted arrays
        Comparable[] riso = new Comparable[isoform.length + other.isoform.length];
        double[] rval = new double[riso.length];
        int i, j, k;
        for (i = 0, j = 0, k = 0; i < isoform.length && j < other.isoform.length;) {
            int c = isoform[i].compareTo(other.isoform[j]);
            if (c < 0 || (c == 0 && value[i] < other.value[j])) {
                riso[k] = isoform[i];
                rval[k++] = value[i++];
            } else {
                riso[k] = other.isoform[j];
                rval[k++] = other.value[j++];
            }
        }
        while (i < isoform.length) {
            riso[k] = isoform[i];
            rval[k++] = value[i++];
        }
        while (j < other.isoform.length) {
            riso[k] = other.isoform[j];
            rval[k++] = other.value[j++];
        }
        this.isoform = riso;
        this.value = rval;
    }

    public void intersectWith(IsoList other) {
        if (this.isEmpty()) {
            return;
        }
        if (other.isEmpty()) {
            this.clear();
            return;
        }

        // merge the two sorted arrays and keep only common isoforms
        Comparable[] riso = new Comparable[isoform.length + other.isoform.length];
        double[] rval = new double[riso.length];
        int i, j, k;
        for (i = 0, j = 0, k = 0; i < isoform.length && j < other.isoform.length;) {
            int c = isoform[i].compareTo(other.isoform[j]);
            if (c < 0)
                ++i;
            else if (c > 0)
                ++j;
            else {
                // see how many positions have this same isoform in
                // the first and second isoform lists
                int ii = i+1;
                while (ii < isoform.length && 0 == isoform[i].compareTo(isoform[ii]))
                    ++ii;
                int jj = j+1;
                while (jj < other.isoform.length && 0 == other.isoform[j].compareTo(other.isoform[jj]))
                    ++jj;
                while (i < ii && j < jj) {
                    if (value[i] < other.value[j]) {
                        riso[k] = isoform[i];
                        rval[k++] = value[i++];
                    } else {
                        riso[k] = other.isoform[j];
                        rval[k++] = other.value[j++];
                    }
                }
                while (i < ii) {
                    riso[k] = isoform[i];
                    rval[k++] = value[i++];
                }
                while (j < jj) {
                    riso[k] = other.isoform[j];
                    rval[k++] = other.value[j++];
                }
            }
        }
        this.isoform = Arrays.copyOf(riso, k);
        this.value = Arrays.copyOf(rval, k);
    }


    private void copyFrom(IsoList isoList) {
        this.multiplicity = isoList.multiplicity;
        this.isoform = Arrays.copyOf(isoList.isoform, isoList.isoform.length);
        this.value = Arrays.copyOf(isoList.value, isoList.value.length);
    }

    public void markForRemoval(int i) {
        //To change body of created methods use File | Settings | File Templates.
    }

    public void finishRemovals() {
    }
}