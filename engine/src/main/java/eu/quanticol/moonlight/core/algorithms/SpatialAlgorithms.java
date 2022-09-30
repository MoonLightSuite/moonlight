package eu.quanticol.moonlight.core.algorithms;

import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.space.DistanceStructure;

import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public class SpatialAlgorithms<E, M, R> {
    private final DistanceStructure<E, M> ds;
    private final boolean parallel;
    private final SignalDomain<R> domain;

    public SpatialAlgorithms(DistanceStructure<E, M> distanceStructure,
                             SignalDomain<R> signalDomain,
                             boolean isParallel) {
        ds = distanceStructure;
        domain = signalDomain;
        parallel = isParallel;
    }

    public static <E, M, R> IntFunction<R> reach(SignalDomain<R> signalDomain,
                                                 IntFunction<R> leftSpatialSignal,
                                                 IntFunction<R> rightSpatialSignal,
                                                 DistanceStructure<E, M> distStr) {
        return new ReachAlgorithm<>(distStr, signalDomain,
                leftSpatialSignal, rightSpatialSignal)
                .compute();
    }

    public IntFunction<R> somewhere(IntFunction<R> s) {
        return unaryOperation(allLocations(), this::neighbourhood,
                domain::disjunction, domain.min(), domain.max(), s);
    }

    public IntFunction<R> unaryOperation(int[] range,
                                         IntFunction<IntPredicate> filter,
                                         BinaryOperator<R> domainOp,
                                         R identity,
                                         R bound,
                                         IntFunction<R> spatialSignal) {
        IntFunction<R> algorithm = filterReduce(filter, domainOp,
                identity, bound, spatialSignal);
        return i -> locationStream(range[0], range[1])
                .mapToObj(algorithm)
                .toList().get(i);
    }

    private IntFunction<R> filterReduce(IntFunction<IntPredicate> neighbourhood,
                                        BinaryOperator<R> op,
                                        R id,
                                        R bound,
                                        IntFunction<R> s) {
        IntFunction<IntStream> closeEnoughLocs = i -> {
            int[] range = ds.getBoundingBox(i);
            return locationStream(range[0], range[1]);
        };
        IntFunction<IntStream> locStream =
                i -> closeEnoughLocs.apply(i).filter(neighbourhood.apply(i));
        IntFunction<int[]> inRangeLocs = i -> locStream.apply(i).toArray();

        return i -> reduceToBound(inRangeLocs.apply(i), s, id, bound, op);
    }

    private IntStream locationStream(int start, int end) {
        if (parallel)
            return IntStream.range(start, end).parallel();
        return IntStream.range(start, end);
    }

    private R reduceToBound(int[] locations, IntFunction<R> signal,
                            R start, R toBound, BinaryOperator<R> op) {
        R result = start;
        for (int element : locations) {
            result = op.apply(result, signal.apply(element));
            if (result.equals(toBound))
                break;
        }
        return result;
    }

    private int[] allLocations() {
        return new int[]{0, ds.getModel().size()};
    }

    private IntPredicate neighbourhood(int i) {
        return j -> ds.areWithinBounds(i, j);
    }

    public IntFunction<R> everywhere(IntFunction<R> s) {
        return unaryOperation(allLocations(), this::neighbourhood,
                domain::conjunction, domain.max(), domain.min(), s);
    }
}
