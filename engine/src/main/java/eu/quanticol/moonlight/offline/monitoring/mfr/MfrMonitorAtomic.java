package eu.quanticol.moonlight.offline.monitoring.mfr;

import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.offline.signal.mfr.MfrSignal;

import java.util.function.Function;
import java.util.function.IntFunction;

public class MfrMonitorAtomic<S, T, R> implements MfrMonitor<S, T, R> {
    private final Function<T, R> atomic;

    public MfrMonitorAtomic(Function<T, R> atomic) {
        this.atomic = atomic;
    }

    @Override
    public SpatialTemporalSignal<R> monitor(SpatialTemporalSignal<T> signal) {
        return signal.apply(atomic);
    }

    @Override
    public IntFunction<MfrSignal<R>> monitor(SpatialTemporalSignal<T> signal,
                                             IntFunction<int[]> locations) {
        return i -> signal.selectApply(atomic, locations.apply(i));
    }

}
