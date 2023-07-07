package io.github.moonlightsuite.moonlight.offline.monitoring.mfr;

import io.github.moonlightsuite.moonlight.offline.signal.SpatialTemporalSignal;
import io.github.moonlightsuite.moonlight.offline.signal.mfr.MfrSignal;

import java.util.function.IntFunction;

public interface MfrSetMonitor<S, T, V> {

    IntFunction<MfrSignal<V>> monitor(
            SpatialTemporalSignal<T> signal,
            IntFunction<int[]> locations);
}
