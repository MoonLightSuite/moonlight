package eu.quanticol.moonlight.offline.algorithms.mfr;

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.offline.signal.mfr.MfrSignal;

import java.util.Arrays;
import java.util.List;
import java.util.function.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @param <V> as
 */
public class MfrAlgorithm<V> {
    private final boolean parallel;

    public MfrAlgorithm(boolean isParallel) {
        parallel = isParallel;
    }

    public static int[] getAllWithinDistance(int i, int size,
                                             DistanceStructure<?, ?> distance) {
        return IntStream.range(0, size)
                .filter(j -> distance.areWithinBounds(i, j))
                .toArray();
    }

    public MfrSignal<V> mapAlgorithm(UnaryOperator<V> mapper,
                                     MfrSignal<V> data) {
        return data.apply(mapper);
    }

    public MfrSignal<V> filterAlgorithm(Predicate<V> predicate,
                                        MfrSignal<V> data) {
        return data.filter(predicate);
    }

    /**
     * <pre>{@code
     *         <U> U reduce(U identity,
     *                  BiFunction<U, ? super T, U> accumulator,
     *                  BinaryOperator<U> combiner);
     * }</pre>
     *
     * @param aggregator aggregator
     */
    public <R> Function<int[], IntFunction<R>> reduceAlgorithm(
            Function<List<V>, R> aggregator,
            Function<int[], IntFunction<V>> argSignal,
            int size,
            DistanceStructure<?, ?> distance) {
        Function<int[], List<R>> results = locs -> streamAllWithinDistance(locs,
                size,
                distance)
                .mapMulti(evaluate(argSignal.apply(locs), aggregator)).toList();
        return locs -> results.apply(locs)::get;
    }

    private <R> BiConsumer<int[], Consumer<R>> evaluate(
            IntFunction<V> argSignal,
            Function<List<V>, R> aggregator) {
        return (ls, arg) -> {
            List<V> inner = Arrays.stream(ls).mapToObj(argSignal).toList();
            arg.accept(aggregator.apply(inner));
        };
    }

    public Stream<int[]> streamAllWithinDistance(
            int[] locationsSet,
            int size,
            DistanceStructure<?, ?> distance) {
        return locationsStream(locationsSet)
                .mapToObj(l -> getAllWithinDistance(l, size, distance));
    }

    private IntStream locationsStream(int[] locationsSet) {
        var stream = IntStream.of(locationsSet);
        return parallel ? stream.parallel() : stream;
    }

    private <T> RuntimeException illegalState(Stream<T> data) {
        return new IllegalStateException("Unable to reduce the set values:  " +
                data.toArray()[0] + "...");
    }

    private <T> Stream<T> valueStream(List<T> spatialSignal) {
        var stream = spatialSignal.stream();
        return parallel ? stream.parallel() : stream;
    }


    private BiFunction<V, Integer, V> accumulator(V[] s,
                                                  BinaryOperator<V> op) {
        return (acc, j) -> op.apply(acc, s[j]);
    }
}
