/**
 * 
 */
package eu.quanticol.moonlight.tests;

import java.util.function.Function;

import eu.quanticol.moonlight.signal.Signal;

/**
 * @author loreti
 *
 */
public class TestUtils {
	
	public static <T> Signal<T> createSignal( double start, double end, double dt, Function<Double,T> f ) {
		Signal<T> signal = new Signal<>();
		double time = start;
		while (time<=end) {
			signal.add(time, f.apply(time));
			time += dt;
		}
		signal.endAt(end);
		return signal;
	}

}
