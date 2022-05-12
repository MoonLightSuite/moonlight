package eu.quanticol.moonlight.offline.monitoring.mfr;

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.offline.algorithms.mfr.MfrAlgorithm;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;

import java.util.List;
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
        var ds = distanceFunction.apply(locationService.get(0.0));
        var arg = argMonitor.monitor(locationService, signal);
        var result = reduce(arg, signal.size()).apply(ds);
        //return signal.apply(l -> result.get(l));
        return null;
    }

    private Function<DistanceStructure<S, ?>, List<R>> reduce(IntFunction<V> arg, int size) {
        MfrAlgorithm<V, R> sp = new MfrAlgorithm<>(false);
        return ds ->
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
