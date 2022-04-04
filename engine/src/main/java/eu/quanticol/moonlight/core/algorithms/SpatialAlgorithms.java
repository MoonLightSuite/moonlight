package eu.quanticol.moonlight.core.algorithms;

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.signal.SignalDomain;

import java.util.List;

public class SpatialAlgorithms {
    private SpatialAlgorithms() {} // Hidden constructor

    public static <E, M, R> List<R> reach(SignalDomain<R> signalDomain,
                                          List<R> leftSpatialSignal,
                                          List<R> rightSpatialSignal,
                                          DistanceStructure<E, M> distStr)
    {
        return new ReachAlgorithm<>(distStr, signalDomain,
                                    leftSpatialSignal, rightSpatialSignal)
                        .compute();
    }

    public static <E, M, R> List<R> escape(SignalDomain<R> signalDomain,
                                           List<R> spatialSignal,
                                           DistanceStructure<E, M> distStr)
    {
        return new EscapeAlgorithm<>(distStr, signalDomain, spatialSignal)
                        .compute();
    }

    public static <E, M, R> List<R> everywhere(SignalDomain<R> domain,
                                               List<R> s,
                                               DistanceStructure<E, M> ds)
    {
        return new SpatialOperator<>(ds, domain, false).everywhere(s);
    }

    public static <E, M, R> List<R> somewhere(SignalDomain<R> domain,
                                              List<R> s,
                                              DistanceStructure<E, M> ds)
    {
        return new SpatialOperator<>(ds, domain, false).somewhere(s);
    }

    public static <E, M, R> List<R> somewhereParallel(SignalDomain<R> domain,
                                                      List<R> s,
                                                      DistanceStructure<E, M> ds)
    {
        return new SpatialOperator<>(ds, domain, true).somewhere(s);
    }

    public static <E, M, R> List<R> everywhereParallel(SignalDomain<R> domain,
                                                       List<R> s,
                                                       DistanceStructure<E, M> ds)
    {
        return new SpatialOperator<>(ds, domain, true).everywhere(s);
    }
}
