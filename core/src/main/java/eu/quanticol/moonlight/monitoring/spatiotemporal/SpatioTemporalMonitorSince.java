/**
 * 
 */
package eu.quanticol.moonlight.monitoring.spatiotemporal;

import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitorSince;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitorUntil;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;

/**
 * @author loreti
 *
 */
public class SpatioTemporalMonitorSince<E,S,T> implements SpatioTemporalMonitor<E, S, T> {

	private SpatioTemporalMonitor<E, S, T> m1;
	private SpatioTemporalMonitor<E, S, T> m2;
	private Interval interval;
	private SignalDomain<T> domain;

	public SpatioTemporalMonitorSince(SpatioTemporalMonitor<E, S, T> m1, Interval interval,
			SpatioTemporalMonitor<E, S, T> m2, SignalDomain<T> domain) {
		this.m1 = m1;
		this.m2 = m2;
		this.interval = interval;
		this.domain = domain;
	}

	@Override
	public SpatioTemporalSignal<T> monitor(LocationService<E> locationService, SpatioTemporalSignal<S> signal) {
		return SpatioTemporalSignal.applyToSignal(m1.monitor(locationService, signal), (s1,s2) -> TemporalMonitorSince.computeSince(domain, s1, interval, s2), m2.monitor(locationService, signal));
	}

}
