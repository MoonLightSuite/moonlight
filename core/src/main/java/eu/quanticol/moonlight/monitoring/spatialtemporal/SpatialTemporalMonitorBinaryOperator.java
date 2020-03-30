/**
 * 
 */
package eu.quanticol.moonlight.monitoring.spatialtemporal;

import java.util.function.BiFunction;

import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

/**
 * @author loreti
 *
 */
public class SpatialTemporalMonitorBinaryOperator<E,S,T> implements SpatialTemporalMonitor<E, S, T> {

	private SpatialTemporalMonitor<E, S, T> m1;
	private BiFunction<T, T, T> op;
	private SpatialTemporalMonitor<E, S, T> m2;

	public SpatialTemporalMonitorBinaryOperator(SpatialTemporalMonitor<E, S, T> m1,
												BiFunction<T, T, T> op, SpatialTemporalMonitor<E, S, T> m2) {
		this.m1 = m1;
		this.op = op;
		this.m2 = m2;
	}

	@Override
	public SpatialTemporalSignal<T> monitor(LocationService<E> locationService, SpatialTemporalSignal<S> signal) {
		return SpatialTemporalSignal.apply(m1.monitor(locationService, signal), op, m2.monitor(locationService, signal));
	}

}
