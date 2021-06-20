/**
 * 
 */
package eu.quanticol.moonlight.monitoring.spatialtemporal;

import java.util.function.BiFunction;

import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitorPastOperator;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

/**
 * @author loreti
 *
 */
public class SpatialTemporalMonitorPastOperator<E,S,T> implements SpatialTemporalMonitor<E, S, T> {

	private SpatialTemporalMonitor<E, S, T> m;
	private Interval interval;
	private BiFunction<T, T, T> op;
	private T init;

	public SpatialTemporalMonitorPastOperator(SpatialTemporalMonitor<E, S, T> m, Interval interval, BiFunction<T,T,T> op,
                                              T init) {
		this.m = m;
		this.interval = interval;
		this.op = op;
		this.init = init;
	}

	@Override
	public SpatialTemporalSignal<T> monitor(LocationService<E> locationService, SpatialTemporalSignal<S> signal) {
		return m.monitor(locationService, signal).applyToSignal(s -> TemporalMonitorPastOperator.computeSignal(s, interval, op,init));
	}

}
