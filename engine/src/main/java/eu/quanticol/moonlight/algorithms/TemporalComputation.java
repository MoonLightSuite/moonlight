package eu.quanticol.moonlight.algorithms;

import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.signal.Signal;

import java.util.function.BinaryOperator;

public class TemporalComputation {

    private TemporalComputation() {} // Hidden constructor

    public static <T> Signal<T> computePastSignal(Signal<T> signal,
                                                  Interval interval,
                                                  BinaryOperator<T> op, T init)
    {
        if (interval == null) {
            return signal.iterateForward(op , init);
        } else {
            SlidingWindow<T> sw = new SlidingWindow<>(interval.getStart(),
                                                      interval.getEnd(),
                                                      op, false);
            return sw.apply(signal);
        }
    }

    public static <T> Signal<T> computeFutureSignal(Signal<T> signal,
                                                    Interval interval,
                                                    BinaryOperator<T> op,
                                                    T init)
    {
        if (interval == null) {
            return signal.iterateBackward(op, init);
        } else {
            SlidingWindow<T> sw = new SlidingWindow<>(interval.getStart(),
                                                      interval.getEnd(),
                                                      op, true);
            Signal<T> result = sw.apply(signal);
            //System.out.println("FutureOperator Result Signal@maxT= " +
            //				    signal.end() + ": " +
            //					result.toString());
            return result;
        }
    }
}
