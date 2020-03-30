/**
 * 
 */
package eu.quanticol.moonlight.monitoring.spatialtemporal;

import java.util.function.Function;

import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

/**
 * @author loreti
 *
 */
public class SpatialTemporalMonitorUnaryOperator<E,S,T> implements SpatialTemporalMonitor<E, S, T> {

	private SpatialTemporalMonitor<E, S, T> m;
	private Function<T, T> op;

	public SpatialTemporalMonitorUnaryOperator(SpatialTemporalMonitor<E, S, T> m, Function<T, T> op) {
		this.m = m;
		this.op = op;
	}

	@Override
	public SpatialTemporalSignal<T> monitor(LocationService<E> locationService, SpatialTemporalSignal<S> signal) {
		return m.monitor(locationService, signal).apply(op);
	}

}
