package eu.quanticol.moonlight.offline.signal.mfr;

import eu.quanticol.moonlight.offline.algorithms.BooleanOp;
import eu.quanticol.moonlight.offline.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.offline.signal.STSignal;
import eu.quanticol.moonlight.offline.signal.Signal;

import java.util.function.*;
import java.util.stream.IntStream;

public class MfrSignal<T> extends STSignal<T> {
    private final int[] locations;

    public MfrSignal(int size, IntFunction<Signal<T>> generator,
                     int[] locations) {
        super(size, partialFunction(generator, locations));
        this.locations = locations;
    }

    private static <A> IntFunction<A> partialFunction(IntFunction<A> f,
                                                      int[] set) {
        return i -> {
            if (contains(i, set)) {
                return f.apply(i);
            }
            return null;
        };
    }

    private static boolean contains(int target, int[] elements) {
        for (int element : elements) {
            if (element == target) {
                return true;
            }
        }
        return false;
    }

    public MfrSignal(int size, IntFunction<Signal<T>> generator) {
        super(size, generator);
        this.locations = IntStream.range(1, size).toArray();
    }

    @Override
    public <R> MfrSignal<R> apply(Function<T, R> f) {
        return new MfrSignal<>(getNumberOfLocations(),
                i -> mappedSignal(i, f), locations);
    }

    private <R> Signal<R> mappedSignal(int l, Function<T, R> f) {
        BooleanOp<T, R> booleanOp = new BooleanOp<>();
        return booleanOp.applyUnary(getSignalAtLocation(l), f);
    }

    @Override
    public Signal<T> getSignalAtLocation(int location) {
        return getSignals().get(locations[location]);
    }

    public MfrSignal<T> filter(Predicate<T> p) {
        return new MfrSignal<>(getNumberOfLocations(),
                i -> filteredSignal(i, p), locations);
    }

    private Signal<T> filteredSignal(int l, Predicate<T> p) {
        var booleanOp = new BooleanOp<>();
        return booleanOp.filterUnary(getSignalAtLocation(l), p);
    }

    @Override
    public ParallelSignalCursor<T> getSignalCursor(boolean forward) {
        return new ParallelSignalCursor<>(locations.length,
                i -> getSignalAtLocation(i).getIterator(forward)
        );
    }

    public MfrSignal<T> combine(
            BinaryOperator<T> f,
            STSignal<T> s,
            int[] locations) {
        checkSize(s.getNumberOfLocations());
        return new MfrSignal<>(getNumberOfLocations(),
                i -> binaryOpSignal(i, f, this, s),
                locations);
    }

    private static <T, R> Signal<R> binaryOpSignal(int i,
                                                   BiFunction<T, T, R> f,
                                                   STSignal<T> s1,
                                                   STSignal<T> s2) {
        BooleanOp<T, R> booleanOp = new BooleanOp<>();
        return booleanOp.applyBinary(s1.getSignalAtLocation(i), f,
                s2.getSignalAtLocation(i));
    }

}
