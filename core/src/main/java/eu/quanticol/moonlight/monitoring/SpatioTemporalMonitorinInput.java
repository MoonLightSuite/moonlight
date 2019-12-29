/**
 * 
 */
package eu.quanticol.moonlight.monitoring;

import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;

/**
 * @author loreti
 *
 */
public class SpatioTemporalMonitorinInput<S,T> {
	
	private final SpatioTemporalSignal<S> signal;
	private final LocationService<T> locationService;
	private final String[] locationId;
	
	public SpatioTemporalMonitorinInput(SpatioTemporalSignal<S> signal, LocationService<T> locationService, String[] locationId) {
		super();
		this.signal = signal;
		this.locationService = locationService;
		this.locationId = locationId;
	}

	public SpatioTemporalSignal<S> getSignal() {
		return signal;
	}

	public LocationService<T> getLocationService() {
		return locationService;
	}
	
	

}
