package eu.quanticol.moonlight.offline.monitoring.mfr;

import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;

import java.util.function.IntFunction;

public interface MfrSetMonitor<S, T, R> {

    <V> IntFunction<R> monitor(
            LocationService<Double, S> locationService,
            SpatialTemporalSignal<T> signal);
}
