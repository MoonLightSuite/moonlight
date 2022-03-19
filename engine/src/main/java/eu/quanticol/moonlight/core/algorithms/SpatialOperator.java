package eu.quanticol.moonlight.core.algorithms;

import eu.quanticol.moonlight.core.base.Box;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.space.DistanceStructure;

import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SpatialOperator<E, M, R> {
    private final DistanceStructure<E, M> ds;
    private final boolean parallel;
    private final SignalDomain<R> domain;

    public SpatialOperator(DistanceStructure<E, M> distanceStructure,
                           SignalDomain<R> signalDomain,
                           boolean isParallel)
    {
        ds = distanceStructure;
        domain = signalDomain;
        parallel = isParallel;
    }

    public List<R> somewhere(IntFunction<R> s) {
        return unaryOperation(allLocations(), this::neighbourhood,
                              domain::disjunction, domain.min(), s);
    }


    public List<R> everywhere(IntFunction<R> s) {
        return unaryOperation(allLocations(), this::neighbourhood,
                              domain::conjunction, domain.max(), s);
    }

    public List<R> unaryOperation(Box<Integer> range,
                                  IntFunction<IntPredicate> filter,
                                  BinaryOperator<R> domainOp,
                                  R identity,
                                  IntFunction<R> spatialSignal)
    {
        Function<Integer, R> algorithm = filterReduce(filter, domainOp,
                                                      identity, spatialSignal);
        return locationStream(range).boxed()
                                    .map(algorithm)
                                    .collect(Collectors.toList());
    }

    private IntStream locationStream(Box<Integer> range) {
        if(parallel)
            return IntStream.range(range.getStart(), range.getEnd()).parallel();
        return IntStream.range(range.getStart(), range.getEnd());
    }

    private Function<Integer, R> filterReduce(IntFunction<IntPredicate> filter,
                                              BinaryOperator<R> op,
                                              R id,
                                              IntFunction<R> s)
    {
        return i -> locationStream(allLocations()).filter(filter.apply(i))
                                         .boxed()
                                         .reduce(id, accumulator(s, op) , op);
    }

    private R accumulate(Stream<Integer> stream, R id, BinaryOperator<R> op, IntFunction<R> s) {
        return stream.reduce(id, accumulator(s, op), op);
    }

    private Box<Integer> allLocations() {
        return new Box<>(0, ds.getModel().size());
    }

    private BiFunction<R, Integer, R> accumulator(IntFunction<R> s,
                                                  BinaryOperator<R> op)
    {
        return (acc, j) -> op.apply(acc, s.apply(j));
    }

    private IntPredicate neighbourhood(int i) {
        return j -> ds.areWithinBounds(i, j);
    }
}