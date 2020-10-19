/**
 * 
 */
package eu.quanticol.moonlight.signal;

import eu.quanticol.moonlight.formula.AndFormula;
import eu.quanticol.moonlight.formula.FormulaVisitor;
import eu.quanticol.moonlight.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * @author loreti
 *
 */
public class SpatialTemporalSignal<T> {
	
	private ArrayList<Signal<T>> signals;
	private int size; 

	public SpatialTemporalSignal(int size ) {
		this( size, i -> new Signal<T>() );
	}
	
	public SpatialTemporalSignal(int size , Function<Integer,Signal<T>> f ) {
		this.signals = new ArrayList<>(size);
		this.size = size;
		init( f );
	}
	
	public SpatialTemporalSignal(int size, double[] t, Function<Double,T[]> f ) {
		this( size );
		for( int i=0 ; i<t.length ; i++ ) {
			add( t[i], f.apply(t[i]) );
		}
	}

	public SpatialTemporalSignal(int size, double[] t, T[][] m ) {
		this.signals = new ArrayList<>(size);
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

	public void add(double time, List<T> values) {
		if (values.size()!=this.size) {
			throw new IllegalArgumentException();//TODO: Add message here!
		}
		add(time, values::get);
	}

	
	public void add( double t, Function<Integer,T> f ) {
		for( int i=0 ; i<size ; i++ ) {
			signals.get(i).add(t, f.apply(i));
		}
	}


	public List<Signal<T>> getSignals(){return signals;}

	public List<T> valuesatT(double t){
		List<T> spSignal = new ArrayList<T>(size);
		for( int i=0 ; i<size ; i++ ) {
			spSignal.add(signals.get(i).valueAt(t));
		}
		return spSignal;
	}

	public <R> SpatialTemporalSignal<R> apply(Function<T,R> f ) {
		return new SpatialTemporalSignal<R>(this.size, (i -> signals.get(i).apply(f)));
	}

	/**
	 * @see Signal#applyHorizon(Function, Object, double, double)
	 */
	public <R> SpatialTemporalSignal<R> applyHorizon(Function<T,R> f,
													 R undefined,
													 double horizonStart,
													 double horizonEnd)
	{
		return new SpatialTemporalSignal<>(this.size,
				(i -> signals.get(i).applyHorizon(f, undefined,
												  horizonStart, horizonEnd)));
	}
	
	public static <T,R> SpatialTemporalSignal<R> apply(SpatialTemporalSignal<T> s1, BiFunction<T,T,R> f , SpatialTemporalSignal<T> s2 ) {
		if (s1.size != s2.size) {
			throw new IllegalArgumentException();//TODO: Add message here!
		}
		return new SpatialTemporalSignal<R>( s1.size , (i -> Signal.apply(s1.signals.get(i),f,s2.signals.get(i)) ));
	}

	public static <T,R> SpatialTemporalSignal<R> applyToSignal(SpatialTemporalSignal<T> s1, BiFunction<Signal<T>,Signal<T>,Signal<R>> f , SpatialTemporalSignal<T> s2 ) {
		if (s1.size != s2.size) {
			throw new IllegalArgumentException();//TODO: Add message here!
		}
		return new SpatialTemporalSignal<R>( s1.size , (i -> f.apply( s1.signals.get(i), s2.signals.get(i)) ));
	}

	public <R> SpatialTemporalSignal<R> applyToSignal(Function<Signal<T>,Signal<R>> f ) {
		return new SpatialTemporalSignal<R>(this.size, (i -> f.apply(signals.get(i))));
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

	public void endAt(double end) {
		for (Signal<T> signal : signals) {
			signal.endAt(end);
		}
	}


	public double[][][] toArray(FunctionToDouble<T> f) {
		double[] timePoints = getTimeArray();
		double[][][] toReturn = new double[size][][];
		IntStream.range(0, size).forEach(i -> toReturn[i] = signals.get(i).arrayOf(timePoints,f));
		return toReturn;
	}

	public double[] getTimeArray() {
		Set<Double> timeSet = new HashSet<>();
		for (Signal<T> s : this.signals) {
			timeSet.addAll(s.getTimeSet());
		}
		return timeSet.stream().sorted().distinct().mapToDouble(d -> d.doubleValue()).toArray();
	}

	public <R> void fill( double[] timePoints, R[][] data, Function<T,R> f) {
		for( int i=0 ; i<size ; i++ ) {
			signals.get(i).fill(timePoints,data[i],f);
		}
	}


	public int size() {
		return size;
	}
}