/**
 * 
 */
package eu.quanticol.moonlight.monitoring.temporal;

import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCursor;

/**
 * @author loreti
 *
 */
public class TemporalMonitorUntil<S, T> extends TemporalMonitor<S, T> {

	private TemporalMonitor<S, T> m1;
	private Interval interval;
	private TemporalMonitor<S, T> m2;
	private SignalDomain<T> domain;

	public TemporalMonitorUntil(TemporalMonitor<S, T> m1, TemporalMonitor<S, T> m2, SignalDomain<T> domain) {
		this(m1,null,m2,domain);
	}

	public TemporalMonitorUntil(TemporalMonitor<S, T> m1, Interval interval, TemporalMonitor<S, T> m2, 
			SignalDomain<T> domain) {
		this.m1 = m1;
		this.interval = interval;
		this.m2 = m2;
		this.domain = domain;
	}

	@Override
	public Signal<T> monitor(Signal<S> signal) {
		Signal<T> s1 = m1.monitor(signal);
		Signal<T> s2 = m2.monitor(signal);
		return computeUntil(domain, s1, interval, s2);
	}

	public static <T> Signal<T> computeUntil(SignalDomain<T> domain, Signal<T> s1, Interval interval, Signal<T> s2) {
		Signal<T> unboundedMonitoring = computeUntil(domain,s1,s2);
		if (interval == null) {
			return unboundedMonitoring;
		}
		Signal<T> eventuallyMonitoring = TemporalMonitorFutureOperator.computeSignal(s2, interval, domain::disjunction, domain.min());
		return Signal.apply(unboundedMonitoring,domain::conjunction,eventuallyMonitoring);
	}
	
	public static <T> Signal<T> computeUntil(SignalDomain<T> domain, Signal<T> s1 , Signal<T> s2) {
		Signal<T> result = new Signal<T>();
		SignalCursor<T> c1 = s1.getIterator(false);
		SignalCursor<T> c2 = s2.getIterator(false);
		double t1 = c1.time();
		double t2 = c2.time();
		double end = Math.min( t1 , t2 );
		double time = end;
		T current = domain.min();
		c1.move(time);
		c2.move(time);
		while (!c1.completed()&&!c2.completed()) {
			current = domain.disjunction(c2.value(), domain.conjunction(c1.value(), current));
			result.addBefore(time, current);
			time = Math.max(c1.previousTime(), c2.previousTime());
			c1.move(time);
			c2.move(time);
		} 
		result.endAt(end);
		return result;
	}

}
