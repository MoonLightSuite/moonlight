package eu.quanticol.moonlight.algorithms;

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.signal.SignalDomain;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpaceUtilities {
    private SpaceUtilities() {} // Hidden constructor

    public static <E, M, R> List<R> reach(SignalDomain<R> signalDomain,
                                          IntFunction<R> leftSpatialSignal,
                                          IntFunction<R> rightSpatialSignal,
                                          DistanceStructure<E, M> distStr)
    {
        return new ReachAlgorithm<>(distStr, signalDomain,
                                    leftSpatialSignal, rightSpatialSignal)
                        .compute();
    }

    public static <E, M, R> List<R> escape(SignalDomain<R> signalDomain,
                                           IntFunction<R> spatialSignal,
                                           DistanceStructure<E, M> distStr)
    {
        return new EscapeAlgorithm<>(distStr, signalDomain, spatialSignal)
                        .compute();
    }

    public static <E, M, R> List<R> everywhere(SignalDomain<R> dModule,
                                               IntFunction<R> s,
                                               DistanceStructure<E, M> ds)
    {
        ArrayList<R> values = dModule.createArray(ds.getModel().size());
        for (int i = 0; i < ds.getModel().size(); i++) {
            R v = dModule.max();
            for (int j = 0; j < ds.getModel().size(); j++) {
                if (ds.areWithinBounds(i, j)) {
                    v = dModule.conjunction(v, s.apply(j));
                }
            }
            values.set(i, v);
        }
        return values;
    }

    public static <E, M, R> List<R> somewhere(SignalDomain<R> dModule,
                                              IntFunction<R> s,
                                              DistanceStructure<E, M> ds)
    {
        ArrayList<R> values = dModule.createArray(ds.getModel().size());
        for (int i = 0; i < ds.getModel().size(); i++) {
            R v = dModule.min();
            for (int j = 0; j < ds.getModel().size(); j++) {
                if (ds.areWithinBounds(i, j)) {
                    v = dModule.disjunction(v, s.apply(j));
                }
            }
            values.set(i, v);
        }
        return values;
    }

    public static <E, M, R> List<R> somewhereParallel(
            SignalDomain<R> dModule,
            IntFunction<R> s,
            DistanceStructure<E, M> ds)
    {
        return IntStream
                .range(0, ds.getModel().size())
                .boxed()
                .parallel()
                .map(i -> {
                    R v = dModule.min();
                    for (int j = 0; j < ds.getModel().size(); j++) {
                        if (ds.areWithinBounds(i, j)) {
                            v = dModule.disjunction(v, s.apply(j));
                        }
                    }
                    return v;
                }).collect(Collectors.toList());
    }

    public static <E, M, R> List<R> everywhereParallel(
            SignalDomain<R> dModule,
            IntFunction<R> s,
            DistanceStructure<E, M> ds)
    {
        return IntStream
                .range(0, ds.getModel().size())
                .boxed()
                .parallel()
                .map(i -> {
                    R v = dModule.max();
                    for (int j = 0; j < ds.getModel().size(); j++) {
                        if (ds.areWithinBounds(i, j)) {
                            v = dModule.conjunction(v, s.apply(j));
                        }
                    }
                    return v;
                }).collect(Collectors.toList());
    }
}
