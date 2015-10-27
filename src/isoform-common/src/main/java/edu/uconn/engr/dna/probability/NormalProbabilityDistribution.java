package edu.uconn.engr.dna.probability;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NormalProbabilityDistribution extends
        CumulativeProbabilityDistribution {

    private final Random random;
    private final double mean;
    private final double stddev;
    private final Map<Integer, Double> cacheFragProb;
    private final Map<Integer, Double> cacheCumLTail;

    public NormalProbabilityDistribution(double mean, double stddev, long seed) {
        random = new Random(seed);
        this.mean = mean;
        this.stddev = stddev;
        cacheFragProb = new HashMap<Integer, Double>();
        cacheCumLTail = new HashMap<Integer, Double>();
    }

    @Override
    public int generateInt() {
        return (int) (mean + random.nextGaussian() * stddev);
    }

    @Override
    public String toString() {
        return String.format("normal_%.2f_%.2f", mean, stddev);
    }

    @Override
    public double getMean() {
        return mean;
    }

    @Override
    public double getVariance() {
        return stddev * stddev;
    }

    public double getStddev() {
        return stddev;
    }

    @Override
    public double getWeight(int i, int n) {
        if (Math.abs(i - mean) > 6.0 * stddev) {
            return 0.0;// guard against overflow
        }

        Double prob = cacheFragProb.get(i);
        if (prob == null) {
            double upperValue = (i + 0.5 - mean) / stddev;
            double lowerValue = (i - 0.5 - mean) / stddev;
            cacheFragProb.put(i, prob = inverseNormal(upperValue)
                    - inverseNormal(lowerValue));
        }
        return prob;
    }

    @Override
    public double cumulativeLowerTail(int r) {
//        guard against overflow
        if (r - mean > 6.0 * stddev) {
            return 1.0;
        }
        if (mean - r > 6.0 * stddev) {
            return 0.0;
        }

        Double prob = cacheCumLTail.get(r);
        if (prob == null) {
            synchronized (cacheCumLTail) {
                prob = cacheCumLTail.get(r);
                if (prob == null) {
                    cacheCumLTail.put(r, prob = inverseNormal((r - mean) / stddev));
                }
            }
        }
        return prob;
    }

    final double b1 = 0.31938153;
    final double b2 = -0.356563782;
    final double b3 = 1.781477937;
    final double b4 = -1.821255978;
    final double b5 = 1.330274429;
    final double p = 0.2316419;
    final double c2 = 0.3989423;

    private double inverseNormal(double z) {
        double a = Math.abs(z);
        double t = 1.0 / (1.0 + a * p);
        double b = c2 * Math.exp((-z) * (z / 2.0));
        double n = ((((b5 * t + b4) * t + b3) * t + b2) * t + b1) * t;
        n = 1.0 - b * n;
        if (z < 0.0) {
            n = 1.0 - n;
        }
        return n;

    }

    public static void main(String[] args) {
        ProbabilityDistribution p = new NormalProbabilityDistribution(250, 25,
                10);
        double sum = 0;
        for (int i = 200; i <= 300; ++i) {
            sum += p.getWeight(i, 0);
            System.out.println(i + " " + p.getWeight(i, 0));
        }
        System.out.println(sum);
    }
}
