/**
 * 
 */
package eu.quanticol.moonlight.monitoring.spatialtemporal;

import java.util.function.Function;

import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

/**
 * @author loreti
 *
 */
public interface SpatialTemporalMonitor<E,S,T> {
	
	public SpatialTemporalSignal<T> monitor(LocationService<E> locationService, SpatialTemporalSignal<S> signal);
	
	public static <E,S,T> SpatialTemporalMonitor<E,S,T> atomicMonitor(Function<S,T> atomic ) {
		return new SpatialTemporalMonitorAtomic<>( atomic );
	}

	public static <E,S,T> SpatialTemporalMonitor<E,S,T> andMonitor(SpatialTemporalMonitor<E,S,T> m1, SignalDomain<T> domain, SpatialTemporalMonitor<E,S,T> m2) {
		return new SpatialTemporalMonitorBinaryOperator<E,S,T>(m1,domain::conjunction,m2);
	}
	
	public static <E,S,T> SpatialTemporalMonitor<E,S,T> orMonitor(SpatialTemporalMonitor<E,S,T> m1, SignalDomain<T> domain, SpatialTemporalMonitor<E,S,T> m2) {
		return new SpatialTemporalMonitorBinaryOperator<E,S,T>(m1,domain::disjunction,m2);
	}

	public static <E,S,T> SpatialTemporalMonitor<E,S,T> impliesMonitor(SpatialTemporalMonitor<E,S,T> m1, SignalDomain<T> domain, SpatialTemporalMonitor<E,S,T> m2) {
		return new SpatialTemporalMonitorBinaryOperator<E,S,T>(m1,domain::implies,m2);
	}
	public static <E,S,T> SpatialTemporalMonitor<E,S,T> notMonitor(SpatialTemporalMonitor<E,S,T> m, SignalDomain<T> domain ) {
		return new SpatialTemporalMonitorUnaryOperator<E,S,T>(m,domain::negation);
	}

	public static <E,S,T> SpatialTemporalMonitor<E,S,T> eventuallyMonitor(SpatialTemporalMonitor<E,S,T> m , Interval interval , SignalDomain<T> domain ) {
		return new SpatialTemporalMonitorFutureOperator<E,S,T>(m,interval,domain::disjunction,domain.min());
	}

	public static <E,S,T> SpatialTemporalMonitor<E,S,T> eventuallyMonitor(SpatialTemporalMonitor<E,S,T> m , SignalDomain<T> domain ) {
		return eventuallyMonitor(m,null,domain);
	}

	public static <E,S,T> SpatialTemporalMonitor<E,S,T> globallyMonitor(SpatialTemporalMonitor<E,S,T> m , Interval interval , SignalDomain<T> domain ) {
		return new SpatialTemporalMonitorFutureOperator<E,S,T>(m,interval,domain::conjunction,domain.max());
	}

	public static <E,S,T> SpatialTemporalMonitor<E,S,T> globallyMonitor(SpatialTemporalMonitor<E,S,T> m , SignalDomain<T> domain ) {
		return globallyMonitor(m,null,domain);
	}
	
	public static <E,S,T> SpatialTemporalMonitor<E,S,T> untilMonitor(SpatialTemporalMonitor<E,S,T> m1 , Interval interval, SpatialTemporalMonitor<E,S,T> m2 , SignalDomain<T> domain ) {
		return new SpatialTemporalMonitorUntil<E,S,T>(m1,interval,m2,domain);
	}
	
	public static <E,S,T> SpatialTemporalMonitor<E,S,T> untilMonitor(SpatialTemporalMonitor<E,S,T> m1 , SpatialTemporalMonitor<E,S,T> m2 , SignalDomain<T> domain ) {
		return untilMonitor(m1,null,m2,domain);
	}

	public static <E,S,T> SpatialTemporalMonitor<E,S,T> sinceMonitor(SpatialTemporalMonitor<E,S,T> m1 , Interval interval, SpatialTemporalMonitor<E,S,T> m2 , SignalDomain<T> domain ) {
		return new SpatialTemporalMonitorSince<E,S,T>(m1,interval,m2,domain);
	}
	
	public static <E,S,T> SpatialTemporalMonitor<E,S,T> sinceMonitor(SpatialTemporalMonitor<E,S,T> m1 , SpatialTemporalMonitor<E,S,T> m2 , SignalDomain<T> domain ) {
		return sinceMonitor(m1,null,m2,domain);
	}
	
	public static <E,S,T> SpatialTemporalMonitor<E,S,T> onceMonitor(SpatialTemporalMonitor<E,S,T> m , Interval interval , SignalDomain<T> domain ) {
		return new SpatialTemporalMonitorPastOperator<E,S,T>(m,interval,domain::disjunction,domain.min());
	}

	public static <E,S,T> SpatialTemporalMonitor<E,S,T> onceMonitor(SpatialTemporalMonitor<E,S,T> m , SignalDomain<T> domain ) {
		return onceMonitor(m,null,domain);
	}

	public static <E,S,T> SpatialTemporalMonitor<E,S,T> historicallyMonitor(SpatialTemporalMonitor<E,S,T> m , Interval interval , SignalDomain<T> domain ) {
		return new SpatialTemporalMonitorPastOperator<E,S,T>(m,interval,domain::conjunction,domain.max());
	}

	public static <E,S,T> SpatialTemporalMonitor<E,S,T> historicallyMonitor(SpatialTemporalMonitor<E,S,T> m , SignalDomain<T> domain ) {
		return historicallyMonitor(m,null,domain);
	}
	
	public static <E,S,T> SpatialTemporalMonitor<E,S,T> somewhereMonitor(SpatialTemporalMonitor<E,S,T> m , Function<SpatialModel<E>, DistanceStructure<E, ?>> distance, SignalDomain<T> domain ) {
		return new SpatialTemporalMonitorSomewhere<E,S,T>(m,distance,domain);
	}

	public static <E,S,T> SpatialTemporalMonitor<E,S,T> everywhereMonitor(SpatialTemporalMonitor<E,S,T> m , Function<SpatialModel<E>, DistanceStructure<E, ?>> distance, SignalDomain<T> domain ) {
		return new SpatialTemporalMonitorEverywhere<E,S,T>(m,distance,domain);
	}

	public static <E,S,T> SpatialTemporalMonitor<E,S,T> escapeMonitor(SpatialTemporalMonitor<E,S,T> m , Function<SpatialModel<E>, DistanceStructure<E, ?>> distance, SignalDomain<T> domain ) {
		return new SpatialTemporalMonitorEscape<E,S,T>(m,distance,domain);
	}

	public static <E,S,T> SpatialTemporalMonitor<E,S,T> reachMonitor(SpatialTemporalMonitor<E,S,T> m1 , Function<SpatialModel<E>, DistanceStructure<E, ?>> distance, SpatialTemporalMonitor<E,S,T> m2, SignalDomain<T> domain ) {
		return new SpatialTemporalMonitorReach<E, S, T>(m1,distance,m2,domain);
	}

	public static <E,S,T> SpatialTemporalMonitor<E,S,T> surroundMonitor(SpatialTemporalMonitor<E,S,T> m , Interval interval , SignalDomain<T> domain ) {
		return null;
	}

	public static <E,S,T> SpatialTemporalMonitor<E,S,T> nextMonitor(SpatialTemporalMonitor<E,S,T> m , Interval interval , SignalDomain<T> domain ) {
		return null;
	}

	public static <E,S,T> SpatialTemporalMonitor<E,S,T> closureMonitor(SpatialTemporalMonitor<E,S,T> m , Interval interval , SignalDomain<T> domain ) {
		return null;
	}


}
