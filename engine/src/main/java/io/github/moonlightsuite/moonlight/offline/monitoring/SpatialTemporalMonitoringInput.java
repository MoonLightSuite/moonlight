/**
 *
 */
package io.github.moonlightsuite.moonlight.offline.monitoring;

import io.github.moonlightsuite.moonlight.core.space.LocationService;
import io.github.moonlightsuite.moonlight.offline.signal.SpatialTemporalSignal;

/**
 * TODO: what is this class for?
 * @author loreti
 *
 */
@Deprecated
public class SpatialTemporalMonitoringInput<S,T> {

	private final SpatialTemporalSignal<S> signal;
	private final LocationService<Double, T> locationService;
	private final String[] locationId;

	public SpatialTemporalMonitoringInput(SpatialTemporalSignal<S> signal, LocationService<Double, T> locationService, String[] locationId) {
		super();
		this.signal = signal;
		this.locationService = locationService;
		this.locationId = locationId;
	}

	public SpatialTemporalSignal<S> getSignal() {
		return signal;
	}

	public LocationService<Double, T> getLocationService() {
		return locationService;
	}



}
