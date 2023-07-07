package io.github.moonlightsuite.moonlight.offline.monitoring.mfr;

import io.github.moonlightsuite.moonlight.core.space.DistanceStructure;
import io.github.moonlightsuite.moonlight.core.space.LocationService;
import io.github.moonlightsuite.moonlight.core.space.SpatialModel;
import io.github.moonlightsuite.moonlight.offline.algorithms.ReduceOp;
import io.github.moonlightsuite.moonlight.offline.signal.SpatialTemporalSignal;
import io.github.moonlightsuite.moonlight.offline.signal.mfr.MfrSignal;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static io.github.moonlightsuite.moonlight.offline.algorithms.mfr.MfrAlgorithm.getAllWithinDistance;

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
        return doMonitor(signal, allLocations(signal.getNumberOfLocations()));
    }

    private int[] allLocations(int size) {
        return IntStream.range(0, size).toArray();
    }

    private SpatialTemporalSignal<R> doMonitor(SpatialTemporalSignal<T> signal,
                                               int[] locations) {
        var distance = staticGetDistance();
        IntFunction<int[]> locs = i -> getAllWithinDistance(i,
                signal.getNumberOfLocations(),
                distance);
        IntFunction<MfrSignal<V>> arg = argMonitor.monitor(signal, locs);
        ReduceOp<S, R, V> reduce = new ReduceOp<>(signal.getNumberOfLocations(),
                locationService,
                distanceFunction, aggregator);
        var result = reduce.computeUnary(locations, arg);
        if (result.getNumberOfLocations() == signal.getNumberOfLocations()) {
            return new SpatialTemporalSignal<>(signal.getNumberOfLocations(),
                    result::getSignalAtLocation);
        }
        throw new IllegalStateException("Reduce algorithm failed");
    }

    private DistanceStructure<S, ?> staticGetDistance() {
        return distanceFunction.apply(locationService.get(0.0));
    }

    @Override
    public IntFunction<MfrSignal<R>> monitor(SpatialTemporalSignal<T> signal,
                                             IntFunction<int[]> locations) {
        return i -> doMonitor(signal, locations.apply(i));
    }

}
