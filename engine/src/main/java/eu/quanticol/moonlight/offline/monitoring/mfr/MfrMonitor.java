package eu.quanticol.moonlight.offline.monitoring.mfr;

import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;

public interface MfrMonitor<S, T, R> extends MfrSetMonitor<S, T, R> {

    SpatialTemporalSignal<R> monitor(SpatialTemporalSignal<T> signal);

}
