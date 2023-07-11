package io.github.moonlightsuite.moonlight.offline.monitoring.mfr;

import io.github.moonlightsuite.moonlight.offline.signal.SpatialTemporalSignal;

public interface MfrMonitor<S, T, R> extends MfrSetMonitor<S, T, R> {

    SpatialTemporalSignal<R> monitor(SpatialTemporalSignal<T> signal);

}
