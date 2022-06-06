package eu.quanticol.moonlight.offline.monitoring.spatialtemporal;

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpaceIterator;
import eu.quanticol.moonlight.core.space.SpatialModel;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

public class SpatialBinaryOp<S, R> {
    private final BiFunction<IntFunction<R>, DistanceStructure<S, ?>, IntFunction<R>> op;
    private final SpaceIterator<Double, S> spaceItr;

    public SpatialBinaryOp(
            LocationService<Double, S> l,
            Function<SpatialModel<S>, DistanceStructure<S, ?>> distance,
            BiFunction<IntFunction<R>, DistanceStructure<S, ?>,
                    IntFunction<R>> operator) {
        op = operator;
        spaceItr = new SpaceIterator<>(l, distance);
    }
}
