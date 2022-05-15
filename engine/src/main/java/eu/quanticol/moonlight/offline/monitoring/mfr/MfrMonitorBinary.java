package eu.quanticol.moonlight.offline.monitoring.mfr;

import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.offline.signal.mfr.MfrSignal;

import java.util.function.BinaryOperator;
import java.util.function.IntFunction;

public class MfrMonitorBinary<S, T, R> implements MfrMonitor<S, T, R> {
    private final BinaryOperator<R> operator;
    private final MfrMonitor<S, T, R> leftArg;
    private final MfrMonitor<S, T, R> rightArg;

    public MfrMonitorBinary(BinaryOperator<R> operator,
                            MfrMonitor<S, T, R> leftArg,
                            MfrMonitor<S, T, R> rightArg) {
        this.operator = operator;
        this.leftArg = leftArg;
        this.rightArg = rightArg;
    }

    @Override
    public SpatialTemporalSignal<R> monitor(
            SpatialTemporalSignal<T> signal) {
        var left = leftArg.monitor(signal);
        var right = rightArg.monitor(signal);
        return left.apply(operator, right);
    }

    @Override
    public IntFunction<MfrSignal<R>> monitor(SpatialTemporalSignal<T> signal,
                                             IntFunction<int[]> locations) {

        var left = leftArg.monitor(signal, locations);
        var right = rightArg.monitor(signal, locations);
        return i -> left.apply(i).combine(operator, right.apply(i),
                locations.apply(i));
    }
}
