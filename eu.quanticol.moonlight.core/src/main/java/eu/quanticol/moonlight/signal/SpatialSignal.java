/**
 * 
 */
package eu.quanticol.moonlight.signal;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author loreti
 *
 */
public class SpatialSignal<T> {
	
	private ArrayList<Signal<T>> signals;
	private int size; 

	public SpatialSignal( int size ) {
		this( size, i -> new Signal<T>() );
	}
	
	public SpatialSignal( int size , Function<Integer,Signal<T>> f ) {
		this.signals = new ArrayList<Signal<T>>(size);
		this.size = size;
		init( f );
	}

	private void init(Function<Integer,Signal<T>> initFunction) {
		for( int i=0 ; i<size ; i++ ) {
			signals.add(i, initFunction.apply(i));
		}
	}

	public int getNumberOfLocations() {
		return size;
	}
	
	public void add( double t , T[] values ) {
		if (values.length != size) {
			throw new IllegalArgumentException();//TODO: Add message here!
		}
		add(t,(i -> values[i]));
	}
	
	public void add( double t, Function<Integer,T> f ) {
		for( int i=0 ; i<size ; i++ ) {
			signals.get(i).add(t, f.apply(i));
		}
	}
	
	public <R> SpatialSignal<R> apply( Function<T,R> f ) {
		return new SpatialSignal<R>(this.size, (i -> signals.get(i).apply(f)));
	}
	
	public static <T,R> SpatialSignal<R> apply( SpatialSignal<T> s1, BiFunction<T,T,R> f , SpatialSignal<T> s2 ) {
		if (s1.size != s2.size) {
			throw new IllegalArgumentException();//TODO: Add message here!
		}
		return new SpatialSignal<R>( s1.size , (i -> Signal.apply(s1.signals.get(i),f,s2.signals.get(i)) ));
	}

	public static <T,R> SpatialSignal<R> applyToSignal( SpatialSignal<T> s1, BiFunction<Signal<T>,Signal<T>,Signal<R>> f , SpatialSignal<T> s2 ) {
		if (s1.size != s2.size) {
			throw new IllegalArgumentException();//TODO: Add message here!
		}
		return new SpatialSignal<R>( s1.size , (i -> f.apply( s1.signals.get(i), s2.signals.get(i)) ));
	}

	public <R> SpatialSignal<R> applyToSignal( Function<Signal<T>,Signal<R>> f ) {
		return new SpatialSignal<R>(this.size, (i -> f.apply(signals.get(i))));
	}
	
	public ParallelSignalCursor<T> getSignalCursor( boolean forward ) {
		return new ParallelSignalCursor<>(signals.size(), (i -> signals.get(i).getIterator( forward ) ));
	}

	public double start() {
		double start = Double.NEGATIVE_INFINITY;
		for (Signal<T> signal : signals) {
			start = Math.max( start , signal.start());
		}
		return start;
	}

	public double end() {
		double end = Double.POSITIVE_INFINITY;
		for (Signal<T> signal : signals) {
			end = Math.min( end , signal.end());
		}
		return end;
	}
}
