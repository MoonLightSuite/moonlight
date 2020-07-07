/**
 * 
 */
package eu.quanticol.moonlight.monitoring.spatialtemporal;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitorFutureOperator;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

/**
 * @author loreti
 *
 */
public class SpatialTemporalMonitorFutureOperator<E,S,T> implements SpatialTemporalMonitor<E, S, T> {

	private SpatialTemporalMonitor<E, S, T> m;
	private Interval interval;
	private BinaryOperator<T> op;
	private T init;

	public SpatialTemporalMonitorFutureOperator(SpatialTemporalMonitor<E, S, T> m,
												Interval interval,
												BinaryOperator<T> op,
												T init) {
		this.m = m;
		this.interval = interval;
		this.op = op;
		this.init = init;
	}

	@Override
	public SpatialTemporalSignal<T> monitor(LocationService<E> locationService, SpatialTemporalSignal<S> signal) {
		return m.monitor(locationService, signal).applyToSignal(s -> TemporalMonitorFutureOperator.computeSignal(s, interval, op,init) );
	}

}
