package eu.quanticol.moonlight.core.algorithms;

import eu.quanticol.moonlight.core.base.Box;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.space.IntManhattanDistanceStructure;

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

    public SpatialAlgorithms(DistanceStructure<E, M> distanceStructure,
                             SignalDomain<R> signalDomain) {
        ds = distanceStructure;
        domain = signalDomain;
        parallel = false;
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
        IntFunction<R> algorithm = filterReduce(filter, domainOp,
                identity, bound, spatialSignal);
        return i -> locationStream(range.getStart(), range.getEnd())
                .mapToObj(algorithm)
                .toList().get(i);
    }

    private IntFunction<R> filterReduce(IntFunction<IntPredicate> neighbourhood,
                                        BinaryOperator<R> op,
                                        R id,
                                        R bound,
                                        IntFunction<R> s) {

        IntFunction<int[]> inRangeLocs;

        if (ds instanceof IntManhattanDistanceStructure) {
            inRangeLocs = ds::getNeighbourhood;
        } else {
            IntFunction<IntStream> closeEnoughLocs = i -> {
                int[] range = ds.getBoundingBox(i);
                return locationStream(range[0], range[1]);
            };
            IntFunction<IntStream> locStream = i -> closeEnoughLocs.apply(i)
                    .filter(neighbourhood.apply(i));
            inRangeLocs = i -> locStream.apply(i).toArray();
        }

        return i -> reduceToBound(inRangeLocs.apply(i), s, id, bound, op);
    }

    private IntStream locationStream(int start, int end) {
        if (parallel)
            return IntStream.range(start, end).parallel();
        return IntStream.range(start, end);
    }

    private R reduceToBound(int[] locations, IntFunction<R> signal,
                            R start, R toBound, BinaryOperator<R> op) {
//        return Arrays.stream(locations).boxed().reduce(start, (acc, elem) -> {
//            R s = signal.apply(elem);
//            return op.apply(acc, s);
//        }, op);
        R result = start;
        for (int element : locations) {
            result = op.apply(result, signal.apply(element));
            if (result.equals(toBound))
                break;
        }
        return result;
    }

    private Box<Integer> allLocations() {
        return new Box<>(0, ds.getModel().size());
    }

    private IntPredicate neighbourhood(int i) {
        return j -> ds.areWithinBounds(i, j);
    }

    private Supplier<R> baseValue(R start) {
        return () -> start;

    }

    public IntFunction<R> everywhere(IntFunction<R> s) {
        return unaryOperation(allLocations(), this::neighbourhood,
                domain::conjunction, domain.max(), domain.min(), s);
    }

    private class AccumulatedValue implements ObjIntConsumer<R> {
        private final IntFunction<R> signal;
        private final BinaryOperator<R> op;
        private R value;

        public AccumulatedValue(BinaryOperator<R> op, IntFunction<R> signal) {
            this.op = op;
            this.signal = signal;
        }

        public R getValue() {
            return value;
        }

        @Override
        public void accept(R r, int value) {
            this.value = op.apply(r, signal.apply(value));
        }
    }
}
