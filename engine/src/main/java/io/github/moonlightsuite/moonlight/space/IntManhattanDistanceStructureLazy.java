package io.github.moonlightsuite.moonlight.space;

import io.github.moonlightsuite.moonlight.core.space.DistanceDomain;
import io.github.moonlightsuite.moonlight.core.space.DistanceStructure;
import io.github.moonlightsuite.moonlight.core.space.SpatialModel;
import io.github.moonlightsuite.moonlight.domain.IntegerDomain;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class IntManhattanDistanceStructureLazy
        implements DistanceStructure<Integer, Integer> {
    private final int lowerBound;
    private final int upperBound;
    private final RegularGridModel<Integer> model;

    public IntManhattanDistanceStructureLazy(
            int lowerBound,
            int upperBound,
            @NotNull RegularGridModel<Integer> model) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.model = model;
    }

    @Override
    public boolean areWithinBounds(int from, int to) {
        return isWithinBounds(getIntDistance(from, to));
    }

    public int getIntDistance(int from, int to) {
        int[] fPair = model.unsafeToCoordinates(from);
        int[] tPair = model.unsafeToCoordinates(to);
        return computeManhattanDistance(fPair, tPair);
    }

    private int computeManhattanDistance(int[] from,
                                         int[] to) {
        int distX = Math.abs(from[0] - to[0]);
        int distY = Math.abs(from[1] - to[1]);
        return distX + distY;
    }

    public boolean isWithinBounds(int d) {
        return lowerBound <= d && d <= upperBound;
    }

    @Override
    public Integer getDistance(int from, int to) {
        return getIntDistance(from, to);
    }

    @Override
    public boolean isWithinBounds(Integer d) {
        return isWithinBounds(d.intValue());
    }

    @Override
    public SpatialModel<Integer> getModel() {
        return model;
    }

    @Override
    public Function<Integer, Integer> getDistanceFunction() {
        return x -> x;
    }

    @Override
    public DistanceDomain<Integer> getDistanceDomain() {
        return new IntegerDomain();
    }
}
