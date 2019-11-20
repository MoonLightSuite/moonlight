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
public class SpatioTemporalMonitorAtomic<E,S,T> implements SpatioTemporalMonitor<E, S, T> {

	private Function<S, T> atomic;

	public SpatioTemporalMonitorAtomic(Function<S, T> atomic) {
		this.atomic = atomic;
	}

	@Override
	public SpatioTemporalSignal<T> monitor(LocationService<E> locationService, SpatioTemporalSignal<S> signal) {
		return signal.apply(atomic);
	}

}
