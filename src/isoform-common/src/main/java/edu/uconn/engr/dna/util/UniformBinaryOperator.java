package edu.uconn.engr.dna.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jun 22, 2010
 * Time: 2:52:00 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class UniformBinaryOperator<A> implements BinaryOperator<A, A, A> {

	@Override
	public abstract A compute(A a, A b);
	public static final UniformBinaryOperator<Integer> IntegerSum = new UniformBinaryOperator<Integer>() {

		@Override
		public Integer compute(Integer a, Integer b) {
			return a + b;
		}
	};

	public static <R, V> UniformBinaryOperator<Map<R, V>> mapMerger(
					final boolean useFirstMapAsDestination,
					final UniformBinaryOperator<V> valueMerger) {
		return new UniformBinaryOperator<Map<R, V>>() {

			@Override
			public Map<R, V> compute(Map<R, V> a, Map<R, V> b) {
				Map<R, V> dest = useFirstMapAsDestination ? a : new HashMap<R, V>(a);
				Utils.mergeMaps(dest, b, valueMerger);
				return dest;
			}
		};
	}

	public static <K, V> UniformBinaryOperator<Map<K, V>> mapReunion() {
		return new UniformBinaryOperator<Map<K, V>>() {

			@Override
			public Map<K, V> compute(Map<K, V> a, Map<K, V> b) {
				Map<K, V> r = new HashMap(a);
				r.putAll(b);
				return r;
			}
		};
	}

	public static <T> UniformBinaryOperator<T[]> arrayMerge(final UniformBinaryOperator<T> itemMerger) {
		return new UniformBinaryOperator<T[]>() {

			@Override
			public T[] compute(T[] a, T[] b) {
				if (a == null) {
					return b;
				}

				if (b == null) {
					return a;
				}

				if (a.length > b.length) {
					T[] t = a;
					a = b;
					b = t;
				}

				T[] r = Arrays.copyOf(b, b.length);
				int i;
				for (i = 0; i < a.length; ++i) {
					if (a[i] != null) {
						if (b[i] == null) {
							r[i] = a[i];
						} else {
							r[i] = itemMerger.compute(a[i], b[i]);
						}
					}
				}

				return r;
			}
		};
	}
}
