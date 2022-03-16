package eu.quanticol.moonlight.core.algorithms;

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.offline.algorithms.EscapeAlgorithm;
import eu.quanticol.moonlight.offline.algorithms.ReachAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SpatialAlgorithms {
    private SpatialAlgorithms() {} // Hidden constructor

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

    public static <E, M, R> List<R> everywhere(SignalDomain<R> domain,
                                               IntFunction<R> s,
                                               DistanceStructure<E, M> ds)
    {
        return new SpatialOperator<>(ds, domain, false).everywhere(s);
    }

    public static <E, M, R> List<R> somewhere(SignalDomain<R> domain,
                                              IntFunction<R> s,
                                              DistanceStructure<E, M> ds)
    {
//        int size = ds.getModel().size();
//        List<R> values = domain.createArray(size);
//
//        for (int i = 0; i < size; i++) {
//            R v = domain.min();
//            for (int j = 0; j < size; j++) {
//                if (ds.areWithinBounds(i, j)) {
//                    v = domain.disjunction(v, s.apply(j));
//                }
//            }
//            values.set(i, v);
//        }
//        return values;
        return new SpatialOperator<>(ds, domain, false).somewhere(s);
    }

    public static <E, M, R> List<R> somewhereParallel(SignalDomain<R> domain,
                                                      IntFunction<R> s,
                                                      DistanceStructure<E, M> ds)
    {
        return new SpatialOperator<>(ds, domain, true).somewhere(s);
    }

    public static <E, M, R> List<R> everywhereParallel(
            SignalDomain<R> domain,
            IntFunction<R> s,
            DistanceStructure<E, M> ds)
    {
        return new SpatialOperator<>(ds, domain, true).somewhere(s);
    }
}
