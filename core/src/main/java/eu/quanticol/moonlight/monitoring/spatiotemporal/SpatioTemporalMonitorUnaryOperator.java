/**
 * 
 */
package eu.quanticol.moonlight.monitoring.spatiotemporal;

import java.util.function.Function;

import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;

/**
 * @author loreti
 *
 */
public class SpatioTemporalMonitorUnaryOperator<E,S,T> implements SpatioTemporalMonitor<E, S, T> {

	private SpatioTemporalMonitor<E, S, T> m;
	private Function<T, T> op;

	public SpatioTemporalMonitorUnaryOperator(SpatioTemporalMonitor<E, S, T> m, Function<T, T> op) {
		this.m = m;
		this.op = op;
	}

	@Override
	public SpatioTemporalSignal<T> monitor(LocationService<E> locationService, SpatioTemporalSignal<S> signal) {
		return m.monitor(locationService, signal).apply(op);
	}

}
