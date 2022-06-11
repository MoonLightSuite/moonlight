package eu.quanticol.moonlight.offline.algorithms.mfr;

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.offline.signal.mfr.MfrSignal;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

/**
 * @param <V> as
 */
public class MfrAlgorithm<V> {
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
}
