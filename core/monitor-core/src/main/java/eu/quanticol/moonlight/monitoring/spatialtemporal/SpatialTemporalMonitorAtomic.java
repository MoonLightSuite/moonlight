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
public class SpatialTemporalMonitorAtomic<E,S,T> implements SpatialTemporalMonitor<E, S, T> {

	private Function<S, T> atomic;

	public SpatialTemporalMonitorAtomic(Function<S, T> atomic) {
		this.atomic = atomic;
	}

	@Override
	public SpatialTemporalSignal<T> monitor(LocationService<E> locationService, SpatialTemporalSignal<S> signal) {
		return signal.apply(atomic);
	}

}
