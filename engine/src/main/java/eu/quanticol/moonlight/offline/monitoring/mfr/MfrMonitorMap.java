package eu.quanticol.moonlight.offline.monitoring.mfr;

import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.offline.algorithms.mfr.MfrAlgorithm;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;

import java.util.function.UnaryOperator;

public class MfrMonitorMap<S, T, V> implements MfrSetMonitor<S, T, V> {
    private final MfrSetMonitor<S, T, V> m;
    private final UnaryOperator<V> mapper;

    public MfrMonitorMap(UnaryOperator<V> mapper,
                         MfrSetMonitor<S, T, V> argMonitor) {
        this.mapper = mapper;
        m = argMonitor;
    }

    @Override
    public SpatialTemporalSignal<V> monitor(
            LocationService<Double, S> locationService,
            SpatialTemporalSignal<T> signal) {
        var arg = m.monitor(locationService, signal);
        MfrAlgorithm<V> sp = new MfrAlgorithm<>(false);
        return sp.mapAlgorithm(mapper, arg);
    }
}
