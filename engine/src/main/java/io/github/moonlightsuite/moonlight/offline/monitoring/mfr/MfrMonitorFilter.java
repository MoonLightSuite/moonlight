package io.github.moonlightsuite.moonlight.offline.monitoring.mfr;

import io.github.moonlightsuite.moonlight.offline.algorithms.mfr.MfrAlgorithm;
import io.github.moonlightsuite.moonlight.offline.signal.SpatialTemporalSignal;
import io.github.moonlightsuite.moonlight.offline.signal.mfr.MfrSignal;

import java.util.function.IntFunction;
import java.util.function.Predicate;

public class MfrMonitorFilter<S, T, V> implements MfrSetMonitor<S, T, V> {
    private final MfrSetMonitor<S, T, V> argMonitor;
    private final Predicate<V> predicate;


    public MfrMonitorFilter(Predicate<V> predicate,
                            MfrSetMonitor<S, T, V> argMonitor) {
        this.predicate = predicate;
        this.argMonitor = argMonitor;
    }

    @Override
    public IntFunction<MfrSignal<V>> monitor(SpatialTemporalSignal<T> signal,
                                             IntFunction<int[]> locations) {
        MfrAlgorithm<V> sp = new MfrAlgorithm<>();
        var arg = argMonitor.monitor(signal, locations);
        return i -> sp.filterAlgorithm(predicate, arg.apply(i));
    }
}
