package eu.quanticol.moonlight.offline.monitoring.mfr;

import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.offline.algorithms.mfr.MfrAlgorithm;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;

import java.util.function.IntFunction;
import java.util.function.Predicate;

public class MfrMonitorFilter<S, T, R> implements MfrSetMonitor<S, T, R> {
    private final MfrSetMonitor<S, T, R> m;
    private final Predicate<R> predicate;


    public MfrMonitorFilter(Predicate<R> predicate,
                            MfrSetMonitor<S, T, R> argMonitor) {
        this.predicate = predicate;
        m = argMonitor;
    }

    @Override
    public <V> IntFunction<R> monitor(
            LocationService<Double, S> locationService,
            SpatialTemporalSignal<T> signal) {
        MfrAlgorithm<R, V> sp = new MfrAlgorithm<>(false);
        var x = m.monitor(locationService, signal);
        return sp.filterAlgorithm(predicate, x);
    }
}
