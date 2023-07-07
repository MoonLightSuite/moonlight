package io.github.moonlightsuite.moonlight.offline.signal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public abstract class STSignal<T> {
    private final List<Signal<T>> signals;

    protected STSignal(int size, IntFunction<Signal<T>> f) {
        this.signals = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            getSignals().add(i, f.apply(i));
        }
    }

    public List<Signal<T>> getSignals() {
        return signals;
    }

    protected void checkSize(int inputSize) {
        if (inputSize != getNumberOfLocations()) {
            var error = "Input mismatch with signal size.";
            throw new IllegalArgumentException(error);
        }
    }

    public int getNumberOfLocations() {
        return signals.size();
    }

    public abstract ParallelSignalCursor1<T> getSignalCursor(boolean forward);

    public abstract Signal<T> getSignalAtLocation(int location);

    public abstract <R> STSignal<R> apply(Function<T, R> f);

    @Override
    public String toString() {
        var output = IntStream.range(0, getNumberOfLocations())
                .mapToObj(loc -> "loc=" + loc +
                        ", signals=" + signals.get(loc).toString() + "; ")
                .reduce(String::concat);
        return "[" + output.orElse("<Empty Signal>") + "]";
    }

}
