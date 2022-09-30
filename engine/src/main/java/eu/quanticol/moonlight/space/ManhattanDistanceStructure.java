package eu.quanticol.moonlight.space;

import eu.quanticol.moonlight.core.space.DistanceDomain;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.SpatialModel;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ManhattanDistanceStructure<E, M>
        implements DistanceStructure<E, M> {
    private final Function<E, M> distanceFunction;
    private final DistanceDomain<M> distanceDomain;
    private final M lowerBound;
    private final M upperBound;
    private final RegularGridModel<E> model;

    public ManhattanDistanceStructure(
            @NotNull Function<E, M> distanceFunction,
            @NotNull DistanceDomain<M> distanceDomain,
            @NotNull M lowerBound,
            @NotNull M upperBound,
            @NotNull RegularGridModel<E> model) {
        this.distanceFunction = distanceFunction;
        this.distanceDomain = distanceDomain;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.model = model;
    }

    @Override
    public boolean areWithinBounds(int from, int to) {
        return isWithinBounds(getDistance(from, to));
    }

    @Override
    public M getDistance(int from, int to) {
        int[] fPair = model.toCoordinates(from);
        int[] tPair = model.toCoordinates(to);
        return computeManhattanDistance(fPair, tPair);
    }

    private M computeManhattanDistance(int[] from,
                                       int[] to) {
        int distX = Math.abs(from[0] - to[0]);
        int distY = Math.abs(from[1] - to[1]);
        int dist = distX + distY;
        return distanceToMetric(dist);
    }

    private M distanceToMetric(int distance) {
        M weight = distanceFunction.apply(model.getWeight());
        return distanceDomain.multiply(weight, distance);
    }

    @Override
    public boolean isWithinBounds(M d) {
        return distanceDomain.lessOrEqual(lowerBound, d) &&
                distanceDomain.lessOrEqual(d, upperBound);
    }

    @Override
    public SpatialModel<E> getModel() {
        return model;
    }

    @Override
    public Function<E, M> getDistanceFunction() {
        return distanceFunction;
    }

    @Override
    public DistanceDomain<M> getDistanceDomain() {
        return distanceDomain;
    }
}
