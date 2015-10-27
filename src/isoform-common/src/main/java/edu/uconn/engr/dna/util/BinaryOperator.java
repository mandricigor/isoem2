package edu.uconn.engr.dna.util;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jun 22, 2010
 * Time: 2:52:00 PM
 * To change this template use File | Settings | File Templates.
 */
public interface BinaryOperator<A, B, C> {
    C compute(A a, B b);
}
