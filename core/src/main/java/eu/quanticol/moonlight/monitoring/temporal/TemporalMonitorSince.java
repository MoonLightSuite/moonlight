/**
 * 
 */
package eu.quanticol.moonlight.monitoring.temporal;

import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.monitoring.TemporalMonitoring;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCursor;

/**
 * @author loreti
 *
 */
public class TemporalMonitorSince<S, T> extends TemporalMonitor<S, T> {

	private TemporalMonitor<S, T> m1;
	private Interval interval;
	private TemporalMonitor<S, T> m2;
	private SignalDomain<T> domain;

	public TemporalMonitorSince(TemporalMonitor<S, T> m1, TemporalMonitor<S, T> m2, SignalDomain<T> domain) {
		this(m1,null,m2,domain);
	}

	public TemporalMonitorSince(TemporalMonitor<S, T> m1, Interval interval, TemporalMonitor<S, T> m2, 
			SignalDomain<T> domain) {
		this.m1 = m1;
		this.interval = interval;
		this.m2 = m2;
		this.domain = domain;
	}

	@Override
	public Signal<T> monitor(Signal<S> signal) {
		return computeSince(domain,m1.monitor(signal),interval,m2.monitor(signal));
	}

	public static <T> Signal<T> computeSince(SignalDomain<T> domain, Signal<T> s1 , Interval interval, Signal<T> s2) {
		Signal<T> unboundedMonitoring = computeSince(domain,s1,s2);
		if (interval == null) {
			return unboundedMonitoring;
		}
		Signal<T> onceMonitoring = TemporalMonitorPastOperator.computeSignal(s2, interval, domain::disjunction, domain.max());
		return Signal.apply(unboundedMonitoring,domain::conjunction,onceMonitoring);
	}

	public static <T> Signal<T> computeSince(SignalDomain<T> domain, Signal<T> s1 , Signal<T> s2) {
		Signal<T> result = new Signal<T>();
		SignalCursor<T> c1 = s1.getIterator(true);
		SignalCursor<T> c2 = s2.getIterator(true);
		double start = Math.max( c1.time() , c2.time() );
		double end = Math.min(s1.end(), s2.end());
		double time = start;
		T current = domain.min();
		c1.move(time);
		c2.move(time);
		while (!c1.completed()&&!c2.completed()) {
			result.add(time, domain.disjunction(c2.value(), domain.conjunction(c1.value(), current)));
			time = Math.min(c1.nextTime(), c2.nextTime());
			c1.move(time);
			c2.move(time);
		} 
		result.endAt(end);
		return result;
	}

}
