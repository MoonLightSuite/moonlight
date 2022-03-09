/**
 * 
 */
package eu.quanticol.moonlight.offline.monitoring.spatialtemporal;

import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;

/**
 * @author loreti
 * @deprecated never implemented
 */
@Deprecated
public class SpatialTemporalMonitorSurround<E,S,T> implements SpatialTemporalMonitor<E, S, T> {

	@Override
	public SpatialTemporalSignal<T> monitor(LocationService<Double, E> locationService, SpatialTemporalSignal<S> signal) {
		// TODO Auto-generated method stub
		return null;
	}

}
