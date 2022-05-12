package eu.quanticol.moonlight.offline.monitoring.mfr;

import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;

public interface MfrMonitor<S, T, R> {

    SpatialTemporalSignal<R> monitor(LocationService<Double, S> locationService,
                                     SpatialTemporalSignal<T> signal);

}
