package eu.quanticol.moonlight.offline.signal.mfr;

import eu.quanticol.moonlight.offline.algorithms.BooleanOp;
import eu.quanticol.moonlight.offline.signal.ParallelSignalCursor1;
import eu.quanticol.moonlight.offline.signal.STSignal;
import eu.quanticol.moonlight.offline.signal.Signal;
import eu.quanticol.moonlight.offline.signal.SignalCursor;

import java.util.Arrays;
import java.util.function.*;
import java.util.stream.IntStream;

public class MfrSignal<T> extends STSignal<T> {
    private final int[] locations;

    public MfrSignal(int size, IntFunction<Signal<T>> generator) {
        super(size, generator);
        this.locations = IntStream.range(1, size).toArray();
    }

    public MfrSignal(int size, IntFunction<Signal<T>> generator,
                     int[] locations) {
        super(size, generator);
        this.locations = locations;
    }

    private static boolean contains(int target, int[] elements) {
        for (int element : elements) {
            if (element == target) {
                return true;
            }
        }
        return false;
    }

    //    private static <A> IntFunction<A> partialFunction(IntFunction<A> f,
//                                                      int[] set) {
//        return i -> {
//            if (contains(i, set)) {
//                return f.apply(i);
//            } else {
//                var error = "Signal undefined at location " + i + "!";
//                throw new IllegalArgumentException(error);
//            }
//        };
//    }

    public int[] getLocationsSet() {
        return locations;
    }

    @Override
    public <R> MfrSignal<R> apply(Function<T, R> f) {
        return new MfrSignal<>(getNumberOfLocations(),
                i -> partialFunction(i, j -> mappedSignal(j, f)), locations);
    }

    private <A> A partialFunction(int location, IntFunction<A> partialValue) {
        if (hasLocation(location) >= 0) {
            return partialValue.apply(location);
        } else {
            var error = "Signal undefined at location " + location + "!";
            //throw new IllegalArgumentException(error);
            // TODO: we should handle this in some way
            return null;
        }
    }

    private int hasLocation(int location) {
        return Arrays.binarySearch(locations, location);
    }

    private <R> Signal<R> mappedSignal(int l, Function<T, R> f) {
        BooleanOp<T, R> booleanOp = new BooleanOp<>();
        return booleanOp.applyUnary(getSignalAtLocation(l), f);
    }

    @Override
    public Signal<T> getSignalAtLocation(int location) {
        var position = hasLocation(location);
        if (position >= 0) {
            return getSignals().get(position);
        }
        var error = "The signal is not defined at the given location";
        throw new IllegalArgumentException(error);
    }

    public MfrSignal<T> filter(Predicate<T> p) {
        return new MfrSignal<>(getNumberOfLocations(),
                i -> partialFunction(i, j -> filteredSignal(i, p)), locations);
    }

    private Signal<T> filteredSignal(int l, Predicate<T> p) {
        var booleanOp = new BooleanOp<>();
        return booleanOp.filterUnary(getSignalAtLocation(l), p);
    }

    @Override
    public ParallelSignalCursor1<T> getSignalCursor(boolean forward) {
        IntFunction<SignalCursor<Double, T>> cursor =
                l -> getSignalAtLocation(l).getIterator(forward);
        return new MfrCursor<>(locations, cursor);
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
