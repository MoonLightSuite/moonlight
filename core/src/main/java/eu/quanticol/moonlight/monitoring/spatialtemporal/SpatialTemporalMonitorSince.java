/**
 * 
 */
package eu.quanticol.moonlight.monitoring.spatialtemporal;

import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitorSince;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

/**
 * @author loreti
 *
 */
public class SpatialTemporalMonitorSince<E,S,T> implements SpatialTemporalMonitor<E, S, T> {

	private SpatialTemporalMonitor<E, S, T> m1;
	private SpatialTemporalMonitor<E, S, T> m2;
	private Interval interval;
	private SignalDomain<T> domain;

	public SpatialTemporalMonitorSince(SpatialTemporalMonitor<E, S, T> m1, Interval interval,
                                       SpatialTemporalMonitor<E, S, T> m2, SignalDomain<T> domain) {
		this.m1 = m1;
		this.m2 = m2;
		this.interval = interval;
		this.domain = domain;
	}

	@Override
	public SpatialTemporalSignal<T> monitor(LocationService<E> locationService, SpatialTemporalSignal<S> signal) {
		return SpatialTemporalSignal.applyToSignal(m1.monitor(locationService, signal), (s1, s2) -> TemporalMonitorSince.computeSince(domain, s1, interval, s2), m2.monitor(locationService, signal));
	}

}
