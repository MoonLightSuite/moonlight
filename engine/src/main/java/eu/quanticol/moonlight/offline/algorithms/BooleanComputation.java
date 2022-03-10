package eu.quanticol.moonlight.offline.algorithms;

import eu.quanticol.moonlight.offline.signal.Signal;
import eu.quanticol.moonlight.offline.signal.SignalCursor;

import java.util.function.BiFunction;
import java.util.function.Function;

public class BooleanComputation {
    private BooleanComputation() {} // Hidden constructor

    /**
     * return a signal, given two signals and a binary function
     */
    public static <T, R> Signal<R> applyBinary(Signal<T> s1, BiFunction<T, T, R> f, Signal<T> s2) {
        Signal<R> newSignal = new Signal<>();
        if (!s1.isEmpty() && !s2.isEmpty()) {
            SignalCursor<T> c1 = s1.getIterator(true);
            SignalCursor<T> c2 = s2.getIterator(true);
            double time = Math.max(s1.start(), s2.start());
            c1.move(time);
            c2.move(time);
            while (!c1.completed() && !c2.completed()) {
                newSignal.add(time, f.apply(c1.value(), c2.value()));
                time = Math.min(c1.nextTime(), c2.nextTime());
                c1.move(time);
                c2.move(time);
            }
            if (!newSignal.isEmpty()) {
                newSignal.endAt(Math.min(s1.end(), s2.end()));
            }
        }
        return newSignal;
    }

    /**
     * return a signal, given a signal and a function
     */
    public static <T, R> Signal<R> applyUnary(Signal<T> s, Function<T, R> f) {
        Signal<R> newSignal = new Signal<>();
        SignalCursor<T> cursor = s.getIterator(true);
        while (!cursor.completed()) {
            newSignal.add(cursor.time(), f.apply(cursor.value()));
            cursor.forward();
        }
        newSignal.endAt(s.end());
        return newSignal;
    }
}
