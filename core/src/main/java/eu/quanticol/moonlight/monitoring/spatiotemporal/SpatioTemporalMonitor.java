/**
 * 
 */
package eu.quanticol.moonlight.monitoring.spatiotemporal;

import java.util.function.BiFunction;
import java.util.function.Function;

import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;

/**
 * @author loreti
 *
 */
public interface SpatioTemporalMonitor<E,S,T> {
	
	public SpatioTemporalSignal<T> monitor(LocationService<E> locationService, SpatioTemporalSignal<S> signal);
	
	public static <E,S,T> SpatioTemporalMonitor<E,S,T> atomicMonitor( Function<S,T> atomic ) {
		return new SpatioTemporalMonitorAtomic<>( atomic ); 
	}

	public static <E,S,T> SpatioTemporalMonitor<E,S,T> andMonitor(  SpatioTemporalMonitor<E,S,T> m1, SignalDomain<T> domain, SpatioTemporalMonitor<E,S,T> m2) {
		return new SpatioTemporalMonitorBinaryOperator<E,S,T>(m1,domain::conjunction,m2);
	}
	
	public static <E,S,T> SpatioTemporalMonitor<E,S,T> orMonitor(  SpatioTemporalMonitor<E,S,T> m1, SignalDomain<T> domain, SpatioTemporalMonitor<E,S,T> m2) {
		return new SpatioTemporalMonitorBinaryOperator<E,S,T>(m1,domain::disjunction,m2);
	}

	public static <E,S,T> SpatioTemporalMonitor<E,S,T> impliesMonitor(  SpatioTemporalMonitor<E,S,T> m1, SignalDomain<T> domain, SpatioTemporalMonitor<E,S,T> m2) {
		return new SpatioTemporalMonitorBinaryOperator<E,S,T>(notMonitor(m1, domain),domain::disjunction,m2);
	}
	
	public static <E,S,T> SpatioTemporalMonitor<E,S,T> notMonitor(SpatioTemporalMonitor<E,S,T> m, SignalDomain<T> domain ) {
		return new SpatioTemporalMonitorUnaryOperator<E,S,T>(m,domain::negation); 
	}

	public static <E,S,T> SpatioTemporalMonitor<E,S,T> eventuallyMonitor( SpatioTemporalMonitor<E,S,T> m , Interval interval , SignalDomain<T> domain ) {
		return new SpatioTemporalMonitorFutureOperator<E,S,T>(m,interval,domain::disjunction,domain.min()); 
	}

	public static <E,S,T> SpatioTemporalMonitor<E,S,T> eventuallyMonitor( SpatioTemporalMonitor<E,S,T> m , SignalDomain<T> domain ) {
		return eventuallyMonitor(m,null,domain);
	}

	public static <E,S,T> SpatioTemporalMonitor<E,S,T> globallyMonitor( SpatioTemporalMonitor<E,S,T> m , Interval interval , SignalDomain<T> domain ) {
		return new SpatioTemporalMonitorFutureOperator<E,S,T>(m,interval,domain::conjunction,domain.max()); 
	}

	public static <E,S,T> SpatioTemporalMonitor<E,S,T> globallyMonitor( SpatioTemporalMonitor<E,S,T> m , SignalDomain<T> domain ) {
		return globallyMonitor(m,null,domain);
	}
	
	public static <E,S,T> SpatioTemporalMonitor<E,S,T> untilMonitor( SpatioTemporalMonitor<E,S,T> m1 , Interval interval, SpatioTemporalMonitor<E,S,T> m2 , SignalDomain<T> domain ) {
		return new SpatioTemporalMonitorUntil<E,S,T>(m1,interval,m2,domain);
	}
	
	public static <E,S,T> SpatioTemporalMonitor<E,S,T> untilMonitor( SpatioTemporalMonitor<E,S,T> m1 , SpatioTemporalMonitor<E,S,T> m2 , SignalDomain<T> domain ) {
		return untilMonitor(m1,null,m2,domain);
	}

	public static <E,S,T> SpatioTemporalMonitor<E,S,T> sinceMonitor( SpatioTemporalMonitor<E,S,T> m1 , Interval interval, SpatioTemporalMonitor<E,S,T> m2 , SignalDomain<T> domain ) {
		return new SpatioTemporalMonitorSince<E,S,T>(m1,interval,m2,domain);
	}
	
	public static <E,S,T> SpatioTemporalMonitor<E,S,T> sinceMonitor( SpatioTemporalMonitor<E,S,T> m1 , SpatioTemporalMonitor<E,S,T> m2 , SignalDomain<T> domain ) {
		return sinceMonitor(m1,null,m2,domain);
	}
	
	public static <E,S,T> SpatioTemporalMonitor<E,S,T> onceMonitor( SpatioTemporalMonitor<E,S,T> m , Interval interval , SignalDomain<T> domain ) {
		return new SpatioTemporalMonitorPastOperator<E,S,T>(m,interval,domain::disjunction,domain.min()); 
	}

	public static <E,S,T> SpatioTemporalMonitor<E,S,T> onceMonitor( SpatioTemporalMonitor<E,S,T> m , SignalDomain<T> domain ) {
		return onceMonitor(m,null,domain);
	}

	public static <E,S,T> SpatioTemporalMonitor<E,S,T> historicallyMonitor( SpatioTemporalMonitor<E,S,T> m , Interval interval , SignalDomain<T> domain ) {
		return new SpatioTemporalMonitorPastOperator<E,S,T>(m,interval,domain::conjunction,domain.max()); 
	}

	public static <E,S,T> SpatioTemporalMonitor<E,S,T> historicallyMonitor( SpatioTemporalMonitor<E,S,T> m , SignalDomain<T> domain ) {
		return historicallyMonitor(m,null,domain);
	}
	
	public static <E,S,T> SpatioTemporalMonitor<E,S,T> somewhereMonitor( SpatioTemporalMonitor<E,S,T> m , Function<SpatialModel<E>, DistanceStructure<E, ?>> distance, SignalDomain<T> domain ) {
		return new SpatioTemporalMonitorSomewhere<E,S,T>(m,distance,domain);
	}

	public static <E,S,T> SpatioTemporalMonitor<E,S,T> everywhereMonitor( SpatioTemporalMonitor<E,S,T> m , Function<SpatialModel<E>, DistanceStructure<E, ?>> distance, SignalDomain<T> domain ) {
		return new SpatioTemporalMonitorEverywhere<E,S,T>(m,distance,domain);
	}

	public static <E,S,T> SpatioTemporalMonitor<E,S,T> escapeMonitor( SpatioTemporalMonitor<E,S,T> m , Function<SpatialModel<E>, DistanceStructure<E, ?>> distance, SignalDomain<T> domain ) {
		return new SpatioTemporalMonitorEscape<E,S,T>(m,distance,domain);
	}

	public static <E,S,T> SpatioTemporalMonitor<E,S,T> reachMonitor( SpatioTemporalMonitor<E,S,T> m1 , Function<SpatialModel<E>, DistanceStructure<E, ?>> distance, SpatioTemporalMonitor<E,S,T> m2, SignalDomain<T> domain ) {
		return new SpatioTemporalMonitorReach<E, S, T>(m1,distance,m2,domain);
	}

	public static <E,S,T> SpatioTemporalMonitor<E,S,T> surroundMonitor( SpatioTemporalMonitor<E,S,T> m , Interval interval , SignalDomain<T> domain ) {
		return null;
	}

	public static <E,S,T> SpatioTemporalMonitor<E,S,T> nextMonitor( SpatioTemporalMonitor<E,S,T> m , Interval interval , SignalDomain<T> domain ) {
		return null;
	}

	public static <E,S,T> SpatioTemporalMonitor<E,S,T> closureMonitor( SpatioTemporalMonitor<E,S,T> m , Interval interval , SignalDomain<T> domain ) {
		return null;
	}


}
