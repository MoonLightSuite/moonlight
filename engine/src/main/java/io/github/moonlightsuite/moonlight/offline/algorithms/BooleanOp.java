package io.github.moonlightsuite.moonlight.offline.algorithms;

import io.github.moonlightsuite.moonlight.offline.signal.Signal;
import io.github.moonlightsuite.moonlight.offline.signal.SignalCursor;

import java.util.Arrays;
import java.util.List;
import java.util.function.*;
import java.util.stream.Stream;

import static io.github.moonlightsuite.moonlight.offline.signal.SignalCursor.isNotCompleted;

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

    public Signal<R> applyUnaryWithBound(Signal<T> s, BiFunction<T, R, R> op, R init) {
        return applyOpWith1StepMemory(
                (cursors, prev) -> op.apply(cursors.get(0).getCurrentValue(), prev),
                init, s);
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

    @SafeVarargs
    private Signal<R> applyOpWith1StepMemory(
            BiFunction<List<SignalCursor<Double, T>>, R, R> op,
            R init,
            Signal<T>... signals) {
        output = new Signal<>();
        setStartingTime(signals);
        List<SignalCursor<Double, T>> cs = prepareCursors(signals);
        applyWithOneStepMemory(cs, prev -> op.apply(cs, prev), init);
        setEndingTime(signals);
        return output;
    }



    public Signal<R> filterUnary(Signal<R> s, Predicate<R> p) {
        return filterOp(cursors -> cursors.get(0).getCurrentValue(), p, s);
    }

    @SafeVarargs
    private Signal<R> filterOp(
            Function<List<SignalCursor<Double, R>>, R> op,
            Predicate<R> p,
            Signal<R>... signals) {
        output = new Signal<>();
        setStartingTime(signals);
        List<SignalCursor<Double, R>> cs = prepareCursors(signals);

        applyFilter(cs, p, () -> op.apply(cs));

        setEndingTime(signals);
        return output;
    }

    private void applyFilter(List<SignalCursor<Double, R>> cursors,
                                 Predicate<R> p,
                                 Supplier<R> value) {
        while (isNotCompleted(cursors)) {
            addResult(p.test(value.get()) ? value.get() : null);
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

    private <K> void applyWithOneStepMemory(List<SignalCursor<Double, K>> cursors,
                                            UnaryOperator<R> value, R init) {
        R prev = init;
        while (isNotCompleted(cursors)) {
            prev = value.apply(prev);
            addResult(prev);
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
