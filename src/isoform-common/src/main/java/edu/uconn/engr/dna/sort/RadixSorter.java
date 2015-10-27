package edu.uconn.engr.dna.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A sorter implementing radix sorting
 *
 * @author marius
 * @param <T>
 * <p/>
 * TODO: fix it!
 */
public class RadixSorter<T> implements Sorter<T> {

    private final ItemValueGetter<T> valueGetter;
    private final int bucketCount;
    private final int passes;
    private final int bits;
    private final int bitMask;

    /**
     * Creates a new instance of this class
     *
     * @param bucketCount the number of buckets (should be a power of 2 such that the
     *                    algorithm performs bit operations instead of divisions)
     * @param passes      number of passes needed to ensure that the sorting is done
     *                    properly (should be 1 + the logarithm in base bucketCount of
     *                    the maximum value that will appear in the sorted list - e.g.
     *                    for base 10, and numbers between 1 and 9999 there should be 4
     *                    passes)
     * @param valueGetter object used to extract the relevant numbers from the items in
     *                    the collection; these numbers are actually sorted - their
     *                    order gives the order of the items in the final collection)
     */
    public RadixSorter(int bucketCount, int passes,
                       ItemValueGetter<T> valueGetter) {
        this.bucketCount = bucketCount;
        if (!isPowerOfTwo(bucketCount)) {
            throw new IllegalArgumentException(
                    "Bucket count should be a power of 2");
        }
        this.valueGetter = valueGetter;
        this.passes = passes;
        this.bits = getPowerOfTwo(bucketCount);
        this.bitMask = bucketCount - 1;

    }

    @Override
    public List<T> sort(List<T> collection) {
        if (passes == 0 || collection.isEmpty()) {
            return collection;
        }

        int[] buckets = new int[bucketCount];
        int[] buckets2 = new int[bucketCount];

        int n = collection.size();
        int[] numbers = new int[n + 1];
        int[] next = new int[n + 1];

        // first pass
        int bm = bitMask;
        for (int i = 0; i < n; ++i) {
            T item = collection.get(i);
            int number = valueGetter.getNumber(item);
            int poz = i + 1;
            numbers[poz] = number;
            int bucket = number & bm;
            next[poz] = buckets[bucket];
            buckets[bucket] = poz;
        }
        boolean increasing = false;

        // following passes
        int shiftBits = 0;
        for (int pass = 1; pass < passes; ++pass) {
            bm <<= bits;
            shiftBits += bits;
            int start;
            int end;
            if (increasing) {
                start = 0;
                end = bucketCount;
            } else {
                start = -bucketCount;
                end = 0;
            }
            for (int b = start; b < end; ++b) {
                int bb;
                if (increasing) {
                    bb = b;
                } else {
                    bb = -b - 1;
                }

                int poz = buckets[bb];
                buckets[bb] = 0;
                while (poz != 0) {
                    int bucket = (numbers[poz] & bm) >> shiftBits;
                    int nextPoz = next[poz];
                    next[poz] = buckets2[bucket];
                    buckets2[bucket] = poz;
                    poz = nextPoz;
                }
            }
            int[] tmp = buckets;
            buckets = buckets2;
            buckets2 = tmp;
            increasing = !increasing;
        }

        // collect results
        ArrayList<T> result = new ArrayList<T>(n);
        int start;
        int end;
        if (increasing) {
            start = 0;
            end = bucketCount;
        } else {
            start = -bucketCount;
            end = 0;
        }
        for (int b = start; b < end; ++b) {
            int bb;
            if (increasing) {
                bb = b;
            } else {
                bb = -b - 1;
            }
            int poz = buckets[bb];
            while (poz != 0) {
                result.add(collection.get(poz - 1));
                poz = next[poz];
            }
        }
        if (!increasing) {
            Collections.reverse(result);
        }
        return result;
    }

    private int getPowerOfTwo(int k) {
        int count = 0;
        while (k > 1) {
            k >>= 1;
            ++count;
        }
        return count;
    }

    private boolean isPowerOfTwo(int k) {
        return (k & (k - 1)) == 0;
    }

}
