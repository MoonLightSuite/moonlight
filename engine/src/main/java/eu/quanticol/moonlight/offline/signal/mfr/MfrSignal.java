package eu.quanticol.moonlight.offline.signal.mfr;

import eu.quanticol.moonlight.offline.algorithms.BooleanOp;
import eu.quanticol.moonlight.offline.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.offline.signal.Signal;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;

import java.util.function.*;

public class MfrSignal<T> extends SpatialTemporalSignal<T> {
    private final int[] locations;

    public MfrSignal(int size, IntFunction<Signal<T>> generator,
                     int[] locations) {
        super(size, partialFunction(generator, locations));
        this.locations = locations;
    }

    private static <T, R> Signal<R> binaryOpSignal(int i,
                                                   BiFunction<T, T, R> f,
                                                   SpatialTemporalSignal<T> s1,
                                                   SpatialTemporalSignal<T> s2) {
        BooleanOp<T, R> booleanOp = new BooleanOp<>();
        return booleanOp.applyBinary(s1.getSignals().get(i), f,
                s2.getSignals().get(i));
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

    @Override
    public <R> MfrSignal<R> apply(Function<T, R> f) {
        return new MfrSignal<>(size(), i -> mappedSignal(i, f), locations);
    }

    private <R> Signal<R> mappedSignal(int l, Function<T, R> f) {
        BooleanOp<T, R> booleanOp = new BooleanOp<>();
        return booleanOp.applyUnary(getSignals().get(l), f);
    }

    public MfrSignal<T> filter(Predicate<T> p) {
        return new MfrSignal<>(size(), i -> filteredSignal(i, p), locations);
    }

    private Signal<T> filteredSignal(int l, Predicate<T> p) {
        var booleanOp = new BooleanOp<>();
        return booleanOp.filterUnary(getSignals().get(l), p);
    }

    public MfrSignal<T> combine(
            BinaryOperator<T> f,
            SpatialTemporalSignal<T> s,
            int[] locations) {
        checkSize(this.size(), s.size());
        return new MfrSignal<>(size(),
                i -> binaryOpSignal(i, f, this, s),
                locations);
    }


    @Override
    public ParallelSignalCursor<T> getSignalCursor(boolean forward) {
        return new ParallelSignalCursor<>(locations.length,
                i -> getSignals().get(locations[i]).getIterator(forward)
        );
    }

}
