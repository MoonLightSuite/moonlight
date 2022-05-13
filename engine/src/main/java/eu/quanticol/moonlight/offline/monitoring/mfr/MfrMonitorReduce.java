package eu.quanticol.moonlight.offline.monitoring.mfr;

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.offline.algorithms.mfr.MfrAlgorithm;
import eu.quanticol.moonlight.offline.algorithms.mfr.MfrOp;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

public class MfrMonitorReduce<S, T, R, V> implements MfrMonitor<S, T, R> {

    private final Function<SpatialModel<S>, DistanceStructure<S, ?>> distanceFunction;
    private final MfrSetMonitor<S, T, V> argMonitor;
    private final Function<List<V>, R> aggregator;

    MfrMonitorReduce(MfrSetMonitor<S, T, V> argMonitor,
                     Function<List<V>, R> aggregator,
                     Function<SpatialModel<S>,
                             DistanceStructure<S, ?>> distanceFunction) {
        this.distanceFunction = distanceFunction;
        this.argMonitor = argMonitor;
        this.aggregator = aggregator;
    }

    @Override
    public SpatialTemporalSignal<R> monitor(
            LocationService<Double, S> locationService,
            SpatialTemporalSignal<T> signal) {
        SpatialTemporalSignal<V> arg = argMonitor.monitor(locationService,
                signal);
        MfrOp<S, R, V> sc = new MfrOp<>(locationService,
                distanceFunction,
                (a, b) -> reduce(signal.size()).apply(a, b));
        return sc.computeUnary(arg);
    }

    private BiFunction<IntFunction<V>, DistanceStructure<S, ?>, IntFunction<R>>
    reduce(int size) {
        MfrAlgorithm<V> sp = new MfrAlgorithm<>(false);
        return (arg, ds) ->
                sp.reduceAlgorithm(aggregator, arg, size, ds);
    }

//    @Override
//    public SpatialTemporalSignal<R> monitor(
//            LocationService<Double, S> locationService,
//            SpatialTemporalSignal<T> signal) {
//        SpatialOp<S, R> sc = new SpatialOp<>(locationService,
//                distanceFunction,
//                reduce(argMonitor.monitor(locationService, null, //TODO
//                        signal)));
//        return sc.computeUnary(null); // TODO: not implemented yet
//
//    }


}
