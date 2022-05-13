package eu.quanticol.moonlight.offline.monitoring.mfr;

import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.offline.algorithms.mfr.MfrAlgorithm;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;

import java.util.function.Predicate;

public class MfrMonitorFilter<S, T, V> implements MfrSetMonitor<S, T, V> {
    private final MfrSetMonitor<S, T, V> m;
    private final Predicate<V> predicate;


    public MfrMonitorFilter(Predicate<V> predicate,
                            MfrSetMonitor<S, T, V> argMonitor) {
        this.predicate = predicate;
        m = argMonitor;
    }

    @Override
    public SpatialTemporalSignal<V> monitor(
            LocationService<Double, S> locationService,
            SpatialTemporalSignal<T> signal) {
        MfrAlgorithm<V> sp = new MfrAlgorithm<>(false);
        var x = m.monitor(locationService, signal);
        return sp.filterAlgorithm(predicate, x);
    }
}
