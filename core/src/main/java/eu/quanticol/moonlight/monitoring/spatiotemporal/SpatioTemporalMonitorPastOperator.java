/**
 * 
 */
package eu.quanticol.moonlight.monitoring.spatiotemporal;

import java.util.function.BiFunction;

import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitorFutureOperator;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitorPastOperator;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;

/**
 * @author loreti
 *
 */
public class SpatioTemporalMonitorPastOperator<E,S,T> implements SpatioTemporalMonitor<E, S, T> {

	private SpatioTemporalMonitor<E, S, T> m;
	private Interval interval;
	private BiFunction<T, T, T> op;
	private T init;

	public SpatioTemporalMonitorPastOperator(SpatioTemporalMonitor<E, S, T> m, Interval interval, BiFunction<T,T,T> op,
			T init) {
		this.m = m;
		this.interval = interval;
		this.op = op;
		this.init = init;
	}

	@Override
	public SpatioTemporalSignal<T> monitor(LocationService<E> locationService, SpatioTemporalSignal<S> signal) {
		return m.monitor(locationService, signal).applyToSignal(s -> TemporalMonitorPastOperator.computeSignal(s, interval, op,init));
	}

}
