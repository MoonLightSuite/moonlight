/**
 * 
 */
package eu.quanticol.moonlight.monitoring;

import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

/**
 * @author loreti
 *
 */
public class SpatialTemporalMonitoringInput<S,T> {
	
	private final SpatialTemporalSignal<S> signal;
	private final LocationService<T> locationService;
	private final String[] locationId;
	
	public SpatialTemporalMonitoringInput(SpatialTemporalSignal<S> signal, LocationService<T> locationService, String[] locationId) {
		super();
		this.signal = signal;
		this.locationService = locationService;
		this.locationId = locationId;
	}

	public SpatialTemporalSignal<S> getSignal() {
		return signal;
	}

	public LocationService<T> getLocationService() {
		return locationService;
	}
	
	

}
