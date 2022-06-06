package eu.quanticol.moonlight.offline.algorithms;

import eu.quanticol.moonlight.offline.signal.Signal;
import eu.quanticol.moonlight.offline.signal.SignalCursor;

import java.util.Arrays;
import java.util.List;
import java.util.function.*;
import java.util.stream.Stream;

import static eu.quanticol.moonlight.offline.signal.SignalCursor.isNotCompleted;

public class BooleanOp<T, R> {
    private static final String
            ERROR = "signal data structure failed irreparably";
    private final boolean forward;
    private Signal<R> output;
    private double time;

    public BooleanOp() {
        forward = true;
    }

    public BooleanOp(boolean isForward) {
        forward = isForward;
    }

    private static <T> T error() {
        throw new UnsupportedOperationException(ERROR);
    }

    public Signal<R> applyUnary(Signal<T> s, Function<T, R> op) {
        return applyOp(cursors ->
                op.apply(cursors.get(0).getCurrentValue()), s);
    }

    public Signal<R> applyBinary(Signal<T> s1,
                                 BiFunction<T, T, R> op,
                                 Signal<T> s2) {
        return applyOp(cursors -> op.apply(cursors.get(0).getCurrentValue(),
                        cursors.get(1).getCurrentValue()),
                s1, s2);
    }

    @SafeVarargs
    private Signal<R> applyOp(
            Function<List<SignalCursor<Double, T>>, R> op,
            Signal<T>... signals) {
        output = new Signal<>();
        setStartingTime(signals);
        List<SignalCursor<Double, T>> cs = prepareCursors(signals);
        apply(cs, () -> op.apply(cs));
        setEndingTime(signals);
        return output;
    }

    public <K> Signal<K> filterUnary(Signal<K> s, Predicate<K> p) {
        return filterOp(cursors -> cursors.get(0).getCurrentValue(), p, s);
    }

    //TODO: unsafe castings that are fine as long as T == R, 
    // but should be refactored asap!!
    @SafeVarargs
    private <K> Signal<K> filterOp(
            Function<List<SignalCursor<Double, K>>, K> op,
            Predicate<K> p,
            Signal<K>... signals) {
        output = new Signal<>();
        setStartingTime(signals);
        List<SignalCursor<Double, K>> cs = prepareCursors(signals);

        applyFilter(cs, p, () -> (R) op.apply(cs));

        setEndingTime(signals);
        return (Signal<K>) output;
    }

    //TODO: refactor to remove cast
    private <K> void applyFilter(List<SignalCursor<Double, K>> cursors,
                                 Predicate<K> p,
                                 Supplier<R> value) {
        while (isNotCompleted(cursors)) {
            addResult(p.test((K) value.get()) ? value.get() : null);
            moveCursorsForward(cursors);
        }
    }

    @SafeVarargs
    private <K> void setStartingTime(Signal<K>... signals) {
        if (forward)
            time = maxStart(Arrays.stream(signals));
        else
            time = minEnd(Arrays.stream(signals));
    }

    private <K> double maxStart(Stream<Signal<K>> stream) {
        return stream.map(Signal::getStart)
                .reduce(Math::max)
                .orElseGet(BooleanOp::error);
    }

    private <K> double minEnd(Stream<Signal<K>> stream) {
        return stream.map(Signal::getEnd)
                .reduce(Math::min)
                .orElseGet(BooleanOp::error);
    }

    @SafeVarargs
    private <K> void setEndingTime(Signal<K>... signals) {
        if (!output.isEmpty()) {
            double end = minEnd(Arrays.stream(signals));
            output.endAt(end);
        }
    }

    private <K> void apply(List<SignalCursor<Double, K>> cursors,
                           Supplier<R> value) {
        while (isNotCompleted(cursors)) {
            addResult(value.get());
            moveCursorsForward(cursors);
        }
    }

    @SafeVarargs
    private <K> List<SignalCursor<Double, K>> prepareCursors(
            Signal<K>... signals) {
        return Arrays.stream(signals).map(s -> {
            var c = s.getIterator(forward);
            c.move(time);
            return c;
        }).toList();
    }

    private void addResult(R value) {
        if (forward) {
            output.add(time, value);
        } else {
            output.addBefore(time, value);
        }
    }

    private <K> void moveCursorsForward(List<SignalCursor<Double, K>> cursors) {
        time = cursors.stream()
                .map(this::moveTime)
                .reduce(rightEndingTime())
                .orElseGet(BooleanOp::error);
        cursors.forEach(c -> c.move(time));
    }

    private BinaryOperator<Double> rightEndingTime() {
        if (forward)
            return Math::min;
        return Math::max;
    }

    private <K> double moveTime(SignalCursor<Double, K> cursor) {
        if (forward)
            return cursor.nextTime();
        return cursor.previousTime();
    }
}
