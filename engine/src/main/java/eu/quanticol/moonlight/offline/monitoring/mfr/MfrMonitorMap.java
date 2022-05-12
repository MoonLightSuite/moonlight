package eu.quanticol.moonlight.offline.monitoring.mfr;

import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.offline.algorithms.mfr.MfrAlgorithm;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;

import java.util.function.IntFunction;
import java.util.function.UnaryOperator;

public class MfrMonitorMap<S, T, R> implements MfrSetMonitor<S, T, R> {
    private final MfrSetMonitor<S, T, R> m;
    private final UnaryOperator<R> mapper;

    public MfrMonitorMap(UnaryOperator<R> mapper,
                         MfrSetMonitor<S, T, R> argMonitor) {
        this.mapper = mapper;
        m = argMonitor;
    }

    @Override
    public <V> IntFunction<R> monitor(
            LocationService<Double, S> locationService,
            SpatialTemporalSignal<T> signal) {
        var arg = m.monitor(locationService, signal);
        MfrAlgorithm<R, V> sp = new MfrAlgorithm<>(false);
        return sp.mapAlgorithm(mapper, arg);
    }
}
