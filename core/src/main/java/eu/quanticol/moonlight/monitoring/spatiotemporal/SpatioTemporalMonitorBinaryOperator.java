/**
 * 
 */
package eu.quanticol.moonlight.monitoring.spatiotemporal;

import java.util.function.BiFunction;

import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;

/**
 * @author loreti
 *
 */
public class SpatioTemporalMonitorBinaryOperator<E,S,T> implements SpatioTemporalMonitor<E, S, T> {

	private SpatioTemporalMonitor<E, S, T> m1;
	private BiFunction<T, T, T> op;
	private SpatioTemporalMonitor<E, S, T> m2;

	public SpatioTemporalMonitorBinaryOperator(SpatioTemporalMonitor<E, S, T> m1,
			BiFunction<T, T, T> op, SpatioTemporalMonitor<E, S, T> m2) {
		this.m1 = m1;
		this.op = op;
		this.m2 = m2;
	}

	@Override
	public SpatioTemporalSignal<T> monitor(LocationService<E> locationService, SpatioTemporalSignal<S> signal) {
		return SpatioTemporalSignal.apply(m1.monitor(locationService, signal), op, m2.monitor(locationService, signal));
	}

}
