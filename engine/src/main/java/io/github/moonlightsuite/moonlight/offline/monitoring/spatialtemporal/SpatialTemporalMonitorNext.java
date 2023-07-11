/**
 *
 */
package io.github.moonlightsuite.moonlight.offline.monitoring.spatialtemporal;

import io.github.moonlightsuite.moonlight.core.space.LocationService;
import io.github.moonlightsuite.moonlight.offline.signal.SpatialTemporalSignal;

/**
 * @author loreti
 * @deprecated never implemented
 */
@Deprecated
public class SpatialTemporalMonitorNext<E,S,T> implements SpatialTemporalMonitor<E, S, T> {

	@Override
	public SpatialTemporalSignal<T> monitor(LocationService<Double, E> locationService, SpatialTemporalSignal<S> signal) {
		// TODO Auto-generated method stub
		return null;
	}

}
