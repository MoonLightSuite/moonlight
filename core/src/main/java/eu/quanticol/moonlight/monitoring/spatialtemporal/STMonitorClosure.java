/**
 * 
 */
package eu.quanticol.moonlight.monitoring.spatialtemporal;

import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

/**
 * @author loreti
 *
 */
public class STMonitorClosure<E,S,T> implements SpatialTemporalMonitor<E, S, T> {

	@Override
	public SpatialTemporalSignal<T> monitor(LocationService<E> locationService, SpatialTemporalSignal<S> signal) {
		// TODO Auto-generated method stub
		return null;
	}

}
