package eu.quanticol.moonlight.offline.monitoring.mfr;

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.offline.algorithms.mfr.MfrAlgorithm;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.offline.signal.mfr.MfrSignal;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static eu.quanticol.moonlight.offline.algorithms.mfr.MfrAlgorithm.getAllWithinDistance;

public class MfrMonitorReduce<S, T, R, V> implements MfrMonitor<S, T, R> {

    private final Function<SpatialModel<S>, DistanceStructure<S, ?>> distanceFunction;
    private final MfrSetMonitor<S, T, V> argMonitor;
    private final Function<List<V>, R> aggregator;
    private final LocationService<Double, S> locationService;

    MfrMonitorReduce(MfrSetMonitor<S, T, V> argMonitor,
                     Function<List<V>, R> aggregator,
                     Function<SpatialModel<S>,
                             DistanceStructure<S, ?>> distanceFunction,
                     LocationService<Double, S> locationService) {
        this.distanceFunction = distanceFunction;
        this.argMonitor = argMonitor;
        this.aggregator = aggregator;
        this.locationService = locationService;
    }

    @Override
    public SpatialTemporalSignal<R> monitor(SpatialTemporalSignal<T> signal) {
        var distance = staticGetDistance();
        IntFunction<int[]> locations = i -> getAllWithinDistance(i,
                signal.size(),
                distance);
        var arg = argMonitor.monitor(signal, locations);
        return null;
    }

    private DistanceStructure<S, ?> staticGetDistance() {
        return distanceFunction.apply(locationService.get(0.0));
    }

    private SpatialTemporalSignal<R> doReduce(int size,
                                              IntFunction<MfrSignal<V>> argument) {
        // per ogni locazione
        // mi scorro i segnali in parallelo
        // aggrego secondo l'aggregator e salvo in output
        // quindi devo avere: SxT->V -> TxR per ogni locazione
        int[] locations = IntStream.range(0, size).toArray();
        IntStream.range(0, size).mapToObj(i -> argument);
        //new SpatialTemporalSignal<>();
        return null;
    }


    @Override
    public IntFunction<MfrSignal<R>> monitor(
            SpatialTemporalSignal<T> signal,
            IntFunction<int[]> locations) {
        //var alg = new MfrAlgorithm<>(false);
        //alg.getCloseLocations(signal.size(), distanceFunction);

        //spatial signal: locationSet per ogni locazione
//        Function<int[], SpatialTemporalSignal<V>> arg =
//                locs -> argMonitor.monitor(locationService, signal, locs);
//        MfrOp<S, R, V> sc = new MfrOp<>(locationService,
//                distanceFunction,
//                (a, b) -> reduce(locations, signal.size()).apply(a, b));
//        return sc.computeUnary(locations.length, arg);
//        var alg = new MfrAlgorithm<>(false);
//        int[] locs = alg.getCloseLocations(signal.size(), distanceFunction);
//
//        IntFunction<MfrSignal<V>> arg =
//                l -> argMonitor.monitor(locationService, signal, locs);

        return null;
    }

    private BiFunction<IntFunction<V>, DistanceStructure<S, ?>, IntFunction<R>>
    reduce(int[] locationsSet, int size) {
        MfrAlgorithm<V> sp = new MfrAlgorithm<>(false);
//        return (arg, ds) ->
//                sp.reduceAlgorithm(aggregator, arg, size, ds).apply(locationsSet);
        return null;
    }

//        MfrAlgorithm<V> sp = new MfrAlgorithm<>(false);
//        List<int[]> locationSets = sp.streamAllWithinDistance(locations,
//                signal.size(),
//                distanceFunction.apply(locationService.get(0.0))).collect(Collectors.toList());

}
