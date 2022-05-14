package eu.quanticol.moonlight.offline.monitoring.mfr;

import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.offline.signal.mfr.MfrSignal;

import java.util.function.IntFunction;

public interface MfrSetMonitor<S, T, V> {

    IntFunction<MfrSignal<V>> monitor(
            SpatialTemporalSignal<T> signal,
            IntFunction<int[]> locations);
}
