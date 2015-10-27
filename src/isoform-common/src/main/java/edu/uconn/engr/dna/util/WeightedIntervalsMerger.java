package edu.uconn.engr.dna.util;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jul 20, 2010
 * Time: 3:00:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class WeightedIntervalsMerger extends UniformBinaryOperator<WeightedIntervals> {
    @Override
    public WeightedIntervals compute(WeightedIntervals a, WeightedIntervals b) {
        a.merge(b);
        return a;
    }
}
