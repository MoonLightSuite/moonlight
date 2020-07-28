package eu.quanticol.moonlight.monitoring.temporal.online;

import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.formula.OnlineSlidingWindow;
import eu.quanticol.moonlight.formula.SlidingWindow;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.Signal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;

/**
 * Strategy to interpret online temporal operators on the past (except Since)
 *
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see TemporalMonitor
 */
public class OnlineMonitorPastOperator<T, R> implements TemporalMonitor<T, R> {
    private final TemporalMonitor<T, R> m;
    private final BinaryOperator<R> op;
    private final R init;
    private final R unknown;
    private final Interval interval;
    private final Interval horizon;
    private final List<Signal<R>> worklist;
    private double signalEnd = 0;

    public OnlineMonitorPastOperator(TemporalMonitor<T, R> m,
                                     BinaryOperator<R> op, R init, R unknown,
                                     Interval definitionInterval,
                                     Interval parentHorizon)
    {
        this.m = m;
        this.op = op;
        this.init = init;
        this.unknown = unknown;
        this.interval = definitionInterval;
        this.horizon = parentHorizon;
        this.worklist = new ArrayList<>();
    }

    @Override
    public Signal<R> monitor(Signal<T> signal) {
        return computeSignal(m.monitor(signal));
    }

    protected Signal<R> computeSignal(Signal<R> signal)
    {
        if (interval ==  null || interval.isEmpty()) {
            throw new UnsupportedOperationException("Not Implemented Yet!");
            //return signal.iterateForward(op, init);
        } else {
            SlidingWindow<R> sw = new OnlineSlidingWindow<>(interval.getStart(),
                                                            interval.getEnd(),
                                                            op, false,
                                                            unknown,
                                                            horizon.getEnd());
            return sw.apply(signal);
        }
    }
}
