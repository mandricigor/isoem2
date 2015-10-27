/**
 * 
 */
package edu.uconn.engr.dna.util;

/**
 * @author marius
 *
 */
public interface GenericHandler<R, T> {

	public R handle(T item); 
}
