package edu.uconn.engr.dna.util;

public interface ParameterRunnable<T, R> {
    void run(T item) ;

    R done();

}
