/**
 * 
 */
package eu.quanticol.moonlight.tests;

import java.util.function.BiFunction;
import java.util.function.Function;

import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatialSignal;

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
	
	
	public static <T> SpatialSignal<T> createSpatialSignal(int size, double start, double dt, double end, BiFunction<Double,Integer,T> f ) {
		SpatialSignal<T> s = new SpatialSignal<>(size);
		double time = start;
		while ( time<end ) {
			double current = time;
			s.add(time, (i -> f.apply(current,i)));
			time += dt;
		}		
		return s;
	}
	
	

}
