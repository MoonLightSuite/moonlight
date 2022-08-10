package eu.quanticol.moonlight.core.algorithms;

import eu.quanticol.moonlight.core.base.Box;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.space.DistanceStructure;

import java.util.function.*;
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

    public IntFunction<R> unaryOperation(Box<Integer> range,
                                         IntFunction<IntPredicate> filter,
                                         BinaryOperator<R> domainOp,
                                         R identity,
                                         R bound,
                                         IntFunction<R> spatialSignal) {
        Function<Integer, R> algorithm = filterReduce(filter, domainOp,
                identity, bound, spatialSignal);
        return i -> locationStream(range).boxed()
                .map(algorithm)
                .toList().get(i);
    }

    private Function<Integer, R> filterReduce(IntFunction<IntPredicate> filter,
                                              BinaryOperator<R> op,
                                              R id,
                                              R bound,
                                              IntFunction<R> s) {
        IntFunction<IntStream> allLocs = i -> locationStream(allLocations());
        IntFunction<IntStream> locStream =
                i -> allLocs.apply(i).filter(filter.apply(i));
        IntFunction<int[]> locs = i -> locStream.apply(i).toArray();

        return i -> reduceToBound(locs.apply(i), s, id, bound, op);
//        return i -> locationStream(allLocations()).filter(filter.apply(i))
//                .boxed()
//                .reduce(id, accumulator(s, op), op);
    }

    private IntStream locationStream(Box<Integer> range) {
        if (parallel)
            return IntStream.range(range.getStart(), range.getEnd()).parallel();
        return IntStream.range(range.getStart(), range.getEnd());
    }

    private Box<Integer> allLocations() {
        return new Box<>(0, ds.getModel().size());
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

    private IntPredicate neighbourhood(int i) {
        return j -> ds.areWithinBounds(i, j);
    }

    private BiFunction<R, Integer, R> accumulator(IntFunction<R> s,
                                                  BinaryOperator<R> op) {
        return (acc, j) -> op.apply(acc, s.apply(j));
    }

    public IntFunction<R> everywhere(IntFunction<R> s) {
        return unaryOperation(allLocations(), this::neighbourhood,
                domain::conjunction, domain.max(), domain.min(), s);
    }
}
