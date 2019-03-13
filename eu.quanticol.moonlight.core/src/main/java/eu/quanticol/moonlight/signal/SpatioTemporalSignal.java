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
public class SpatioTemporalSignal<T> {
	
	private ArrayList<Signal<T>> signals;
	private int size; 

	public SpatioTemporalSignal( int size ) {
		this( size, i -> new Signal<T>() );
	}
	
	public SpatioTemporalSignal( int size , Function<Integer,Signal<T>> f ) {
		this.signals = new ArrayList<Signal<T>>(size);
		this.size = size;
		init( f );
	}
	
	public SpatioTemporalSignal( int size, double[] t, Function<Double,T[]> f ) {
		this( size );
		for( int i=0 ; i<t.length ; i++ ) {
			add( t[i], f.apply(t[i]) );
		}
	}

	public SpatioTemporalSignal( int size, double[] t, T[][] m ) {
		this.signals = new ArrayList<Signal<T>>(size);
		this.size = size;
		for( int i=0 ; i<t.length ; i++ ) {
			add( t[i], m[i]);
		}
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

	public void add(double time, ArrayList<T> values) {
		if (values.size()!=this.size) {
			throw new IllegalArgumentException();//TODO: Add message here!
		}
		add(time,i -> values.get(i));
	}

	
	public void add( double t, Function<Integer,T> f ) {
		for( int i=0 ; i<size ; i++ ) {
			signals.get(i).add(t, f.apply(i));
		}
	}


	public ArrayList<Signal<T>> getSignals(){return signals;}

	//public <T> values(int i, double t){return signals.get(i).;}

	public <R> SpatioTemporalSignal<R> apply( Function<T,R> f ) {
		return new SpatioTemporalSignal<R>(this.size, (i -> signals.get(i).apply(f)));
	}
	
	public static <T,R> SpatioTemporalSignal<R> apply( SpatioTemporalSignal<T> s1, BiFunction<T,T,R> f , SpatioTemporalSignal<T> s2 ) {
		if (s1.size != s2.size) {
			throw new IllegalArgumentException();//TODO: Add message here!
		}
		return new SpatioTemporalSignal<R>( s1.size , (i -> Signal.apply(s1.signals.get(i),f,s2.signals.get(i)) ));
	}

	public static <T,R> SpatioTemporalSignal<R> applyToSignal( SpatioTemporalSignal<T> s1, BiFunction<Signal<T>,Signal<T>,Signal<R>> f , SpatioTemporalSignal<T> s2 ) {
		if (s1.size != s2.size) {
			throw new IllegalArgumentException();//TODO: Add message here!
		}
		return new SpatioTemporalSignal<R>( s1.size , (i -> f.apply( s1.signals.get(i), s2.signals.get(i)) ));
	}

	public <R> SpatioTemporalSignal<R> applyToSignal( Function<Signal<T>,Signal<R>> f ) {
		return new SpatioTemporalSignal<R>(this.size, (i -> f.apply(signals.get(i))));
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
