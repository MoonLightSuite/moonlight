package io.github.moonlightsuite.moonlight.offline.monitoring.mfr;

import io.github.moonlightsuite.moonlight.offline.algorithms.mfr.MfrAlgorithm;
import io.github.moonlightsuite.moonlight.offline.signal.SpatialTemporalSignal;
import io.github.moonlightsuite.moonlight.offline.signal.mfr.MfrSignal;

import java.util.function.IntFunction;
import java.util.function.UnaryOperator;

public class MfrMonitorMap<S, T, V> implements MfrSetMonitor<S, T, V> {
    private final MfrSetMonitor<S, T, V> argMonitor;
    private final UnaryOperator<V> mapper;

    public MfrMonitorMap(UnaryOperator<V> mapper,
                         MfrSetMonitor<S, T, V> argMonitor) {
        this.mapper = mapper;
        this.argMonitor = argMonitor;
    }

    @Override
    public IntFunction<MfrSignal<V>> monitor(SpatialTemporalSignal<T> signal,
                                             IntFunction<int[]> locations) {
        IntFunction<MfrSignal<V>> arg = argMonitor.monitor(signal, locations);
        MfrAlgorithm<V> algorithm = new MfrAlgorithm<>();
        return i -> algorithm.mapAlgorithm(mapper, arg.apply(i));
    }
}
