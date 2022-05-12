package eu.quanticol.moonlight.offline.algorithms.mfr;

import eu.quanticol.moonlight.core.space.DistanceStructure;

import java.util.Arrays;
import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @param <V> as
 */
public class MfrAlgorithm<V, R> {
    private final boolean parallel;

    public MfrAlgorithm(boolean isParallel) {
        parallel = isParallel;
    }

    public IntFunction<V> mapAlgorithm(UnaryOperator<V> mapper,
                                       IntFunction<V> data) {
        return i -> mapper.apply(data.apply(i));
    }

    public IntFunction<V> filterAlgorithm(Predicate<V> predicate,
                                          IntFunction<V> data) {
        return i -> predicate.test(data.apply(i)) ? data.apply(i) : null;
    }

    public <T> Stream<V> formulaAlgorithm() {
        // TODO
        return null;
    }

    /**
     * <pre>{@code
     *         <U> U reduce(U identity,
     *                  BiFunction<U, ? super T, U> accumulator,
     *                  BinaryOperator<U> combiner);
     * }</pre>
     *
     * @param aggregator
     */
    public List<R> reduceAlgorithm(Function<List<V>, R> aggregator,
                                   IntFunction<V> argSignal,
                                   int size,
                                   DistanceStructure<?, ?> distance) {

        return streamAllWithinDistance(size, distance)
                .mapMulti(evaluate(argSignal, aggregator))
                //.map(x -> argSignal.apply(x))
                .collect(Collectors.toList());


//        return test;
//        int[] data = valueStream(spatialSignal);
//        try {
//            V[] values = (V[]) data.toArray();
//            return aggregator.apply(values);
//        } catch (ClassCastException e) {
//            throw illegalState(data);
//        }
    }

    private BiConsumer<int[], Consumer<R>> evaluate(
            IntFunction<V> argSignal,
            Function<List<V>, R> aggregator) {
        return (ls, arg) -> {
            List<V> inner = Arrays.stream(ls).mapToObj(argSignal).toList();
            arg.accept(aggregator.apply(inner));
        };
    }

    private Stream<int[]> streamAllWithinDistance(int size,
                                                  DistanceStructure<?, ?> distance) {
        return locationsStream(size)
                .mapToObj(l -> getAllWithinDistance(l, size, distance));
    }

    private int[] getAllWithinDistance(int i, int size,
                                       DistanceStructure<?, ?> distance) {
        return IntStream.range(0, size)
                .filter(j -> distance.areWithinBounds(i, j))
                .toArray();
    }

    private IntStream locationsStream(int size) {
        var stream = IntStream.range(0, size);
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
