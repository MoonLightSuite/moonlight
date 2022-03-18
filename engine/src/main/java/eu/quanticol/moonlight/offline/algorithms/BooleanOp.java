package eu.quanticol.moonlight.offline.algorithms;

import eu.quanticol.moonlight.offline.signal.Signal;
import eu.quanticol.moonlight.offline.signal.SignalCursor;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BooleanOp<T, R> {
    private Signal<R> output;
    private double time;

    public Signal<R> applyUnary(Signal<T> s, Function<T, R> op) {
        return applyOp(cursors -> op.apply(cursors.get(0).value()), s);
    }

    public Signal<R> applyBinary(Signal<T> s1,
                                 BiFunction<T, T, R> op,
                                 Signal<T> s2)
    {
        return applyOp(cursors -> op.apply(cursors.get(0).value(),
                                           cursors.get(1).value()),
                       s1, s2);
    }

    @SafeVarargs
    private final void setStartingTime(Signal<T>... signals) {
        time = Arrays.stream(signals)
                     .map(Signal::start)
                     .reduce(Math::max)
                     .orElseGet(BooleanOp::error);
    }

    @SafeVarargs
    private final void setEndingTime(Signal<T>... signals) {
        if (!output.isEmpty()) {
            double end = Arrays.stream(signals)
                               .map(Signal::end)
                               .reduce(Math::min)
                               .orElseGet(BooleanOp::error);
            output.endAt(end);
        }
    }

    @SafeVarargs
    private final Signal<R> applyOp(Function<List<SignalCursor<T>>, R> op,
                                    Signal<T>... signals)
    {
        output = new Signal<>();
        setStartingTime(signals);
        List<SignalCursor<T>> cs = prepareCursors(signals);
        apply(cs, () -> op.apply(cs));
        setEndingTime(signals);
        return output;
    }

    private void apply(List<SignalCursor<T>> cursors, Supplier<R> value) {
        while (isNotCompleted(cursors.stream())) {
            addResult(value.get());
            moveCursorsForward(cursors);
        }
    }

    @SafeVarargs
    private final List<SignalCursor<T>> prepareCursors(Signal<T>... signals) {
        return Arrays.stream(signals).map(s -> {
            SignalCursor<T> c = s.getIterator(true);
            c.move(time);
            return c;
        }).collect(Collectors.toList());
    }

    private void addResult(R value) {
        output.add(time, value);
    }

    private boolean isNotCompleted(Stream<SignalCursor<T>> cursors) {
        return cursors.map(c -> !c.completed())
                      .reduce( true, (c1, c2) -> c1 && c2);
    }

    private void moveCursorsForward(List<SignalCursor<T>> cursors) {
        time = cursors.stream()
                .map(SignalCursor::nextTime)
                .reduce(Math::min)
                .orElseGet(BooleanOp::error);
        cursors.forEach(c -> c.move(time));
    }

    private static <T> T error() {
        throw new UnsupportedOperationException("signal data structure " +
                "failed irreparably");
    }
}
