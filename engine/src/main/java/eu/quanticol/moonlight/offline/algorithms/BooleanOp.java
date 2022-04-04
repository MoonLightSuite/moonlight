package eu.quanticol.moonlight.offline.algorithms;

import eu.quanticol.moonlight.offline.signal.Signal;
import eu.quanticol.moonlight.offline.signal.SignalCursor;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class BooleanOp<T, R> {
    private Signal<R> output;
    private double time;
    private final boolean forward;

    public BooleanOp() {
        forward = true;
    }

    public BooleanOp(boolean isForward) {
        forward = isForward;
    }

    public Signal<R> applyUnary(Signal<T> s, Function<T, R> op) {
        return applyOp(cursors ->
                            op.apply(cursors.get(0).getCurrentValue()), s);
    }

    public Signal<R> applyBinary(Signal<T> s1,
                                 BiFunction<T, T, R> op,
                                 Signal<T> s2)
    {
        return applyOp(cursors -> op.apply(cursors.get(0).getCurrentValue(),
                                           cursors.get(1).getCurrentValue()),
                       s1, s2);
    }

    @SafeVarargs
    private Signal<R> applyOp(
            Function<List<SignalCursor<Double, T>>, R> op,
            Signal<T>... signals)
    {
        output = new Signal<>();
        setStartingTime(signals);
        List<SignalCursor<Double, T>> cs = prepareCursors(signals);
        apply(cs, () -> op.apply(cs));
        setEndingTime(signals);
        return output;
    }

    @SafeVarargs
    private void setStartingTime(Signal<T>... signals) {
        if(forward)
            time = maxStart(Arrays.stream(signals));
        else
            time = minEnd(Arrays.stream(signals));
    }

    private double maxStart(Stream<Signal<T>> stream) {
        return stream.map(Signal::getStart)
                     .reduce(Math::max)
                     .orElseGet(BooleanOp::error);
    }

    private double minEnd(Stream<Signal<T>> stream) {
        return stream.map(Signal::getEnd)
                .reduce(Math::min)
                .orElseGet(BooleanOp::error);
    }

    @SafeVarargs
    private void setEndingTime(Signal<T>... signals) {
        if (!output.isEmpty()) {
            double end = minEnd(Arrays.stream(signals));
            output.endAt(end);
        }
    }

    private void apply(List<SignalCursor<Double, T>> cursors,
                       Supplier<R> value)
    {
        while (isNotCompleted(cursors.stream())) {
            addResult(value.get());
            moveCursorsForward(cursors);
        }
    }

    @SafeVarargs
    private List<SignalCursor<Double, T>> prepareCursors(
            Signal<T>... signals)
    {
        return Arrays.stream(signals).map(s -> {
            SignalCursor<Double, T> c = s.getIterator(forward);
            c.move(time);
            return c;
        }).toList();
    }

    private void addResult(R value) {
        if(forward) {
            output.add(time, value);
        } else {
            output.addBefore(time, value);
        }
    }

    private boolean isNotCompleted(Stream<SignalCursor<Double, T>> cursors) {
        return cursors.map(c -> !c.isCompleted())
                      .reduce( true, (c1, c2) -> c1 && c2);
    }

    private void moveCursorsForward(List<SignalCursor<Double, T>> cursors) {
        time = cursors.stream()
                      .map(this::moveTime)
                      .reduce(rightEndingTime())
                      .orElseGet(BooleanOp::error);
        cursors.forEach(c -> c.move(time));
    }

    private BinaryOperator<Double> rightEndingTime() {
        if(forward)
            return Math::min;
        return Math::max;
    }

    private double moveTime(SignalCursor<Double, T> cursor) {
        if(forward)
            return cursor.nextTime();
        return cursor.previousTime();
    }

    private static <T> T error() {
        throw new UnsupportedOperationException(ERROR);
    }

    private static final String
            ERROR = "signal data structure failed irreparably";
}
