package eu.quanticol.moonlight.space;

import eu.quanticol.moonlight.core.space.DistanceDomain;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.domain.IntegerDomain;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class IntManhattanDistanceStructure
        implements DistanceStructure<Integer, Integer> {
    private final int lowerBound;

    private final boolean parallel;
    private final int upperBound;
    private final RegularGridModel<Integer> model;
    private final int cappedUpperBound;
    private int[][] distanceMatrix;
    private int[][] oneStepDistanceMatrix;

    /**
     * @param lowerBound lower bound of the distance
     * @param upperBound upper bound of the distance, inclusive
     * @param model      the spatial model
     */
    public IntManhattanDistanceStructure(int lowerBound, int upperBound,
                                         @NotNull RegularGridModel<Integer> model) {
        this(false, lowerBound, upperBound, model);
    }

    /**
     * @param parallel   if true, the distance matrix is computed in parallel
     * @param lowerBound lower bound of the distance
     * @param upperBound upper bound of the distance, inclusive
     * @param model      the spatial model
     */
    public IntManhattanDistanceStructure(
            boolean parallel,
            int lowerBound,
            int upperBound,
            @NotNull RegularGridModel<Integer> model) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.model = model;
        this.parallel = parallel;
        cappedUpperBound = Math.min(model.size(), upperBound + 1);
        computeDistanceMatrix();
    }

    private void computeDistanceMatrix() {
        distanceMatrix = new int[model.size()][0];
        prepareOneStepDistanceMatrix();
        computeReachableSets();
        //cleanDuplicatesInReachabilitySet();
    }

    private void prepareOneStepDistanceMatrix() {
        oneStepDistanceMatrix = new int[model.size()][4];
        IntStream.range(0, model.size())
                .forEach(i -> oneStepDistanceMatrix[i] =
                        model.getNeighboursArray(i));

    }

    private void computeReachableSets() {
        IntStream.range(0, model.size())
                .forEach(this::computeLocationReachableSet);
    }

    private void computeLocationReachableSet(int location) {
        IntStream.range(lowerBound, cappedUpperBound)
                .forEach(distance -> addAll(location, distance));
    }

    private void addAll(int location, int distance) {
        switch (distance) {
            case 0 -> addDistanceSet(new int[]{location}, location);
            case 1 -> addDistanceSet(oneStepDistanceMatrix[location], location);
            default -> addDistanceSet(computeDistanceSet(location), location);
        }
    }

    private int[] computeDistanceSet(int location) {
        int[] previous = distanceMatrix[location];
        IntFunction<IntStream> next = i ->
                Arrays.stream(oneStepDistanceMatrix[i]);

        return Arrays.stream(previous).flatMap(next).toArray();
    }

    private void addDistanceSet(int[] set, int target) {
        int[] oldNeighbours = distanceMatrix[target];
        int[] result = Arrays.copyOf(oldNeighbours,
                oldNeighbours.length + set.length);
        System.arraycopy(set, 0, result,
                oldNeighbours.length, set.length);
        result = Arrays.stream(result).distinct().toArray();
        distanceMatrix[target] = result;
    }

    private void cleanDuplicatesInReachabilitySet() {
        IntStream.range(0, model.size()).forEach(i ->
                distanceMatrix[i] =
                        Arrays.stream(distanceMatrix[i]).distinct().toArray());
    }

    public int[] getNeighbourhood(int location) {
        return distanceMatrix[location];
    }

//    private void extendDistanceMatrix(int location) {
//        int[] oldNeighbours = distanceMatrix[location];
//        int[] result = Arrays.copyOf(oldNeighbours,
//                oldNeighbours.length + neighbours.length);
//        distanceMatrix[location] = result;
//    }

    @Override
    public boolean areWithinBounds(int from, int to) {
        return Arrays.stream(distanceMatrix[from]).anyMatch(x -> x == to);
    }

    @Override
    public Integer getDistance(int from, int to) {
        return getIntDistance(from, to);
    }

    public int getIntDistance(int from, int to) {
        int[] fPair = model.unsafeToCoordinates(from);
        int[] tPair = model.unsafeToCoordinates(to);
        return computeManhattanDistance(fPair, tPair);
    }

//    @Override
//    public int[] getBoundingBox(int location) {
//        int[] pair = model.unsafeToCoordinates(location);
//        int x = pair[0];
//        int y = pair[1];
//        int xMin = Math.max(0, x - upperBound);
//        int xMax = Math.min(model.getColumns() - 1, x + upperBound);
//        int yMin = Math.max(0, y - upperBound);
//        int yMax = Math.min(model.getRows() - 1, y + upperBound);
//        int jMin = model.fromCoordinates(xMin, yMin);
//        int jMax = model.fromCoordinates(xMax, yMax);
//        return new int[]{jMin, jMax};
//    }

    private int computeManhattanDistance(int[] from,
                                         int[] to) {
        int distX = Math.abs(from[0] - to[0]);
        int distY = Math.abs(from[1] - to[1]);
        return distX + distY;
    }

    @Override
    public boolean isWithinBounds(Integer d) {
        return isWithinBounds(d.intValue());
    }

    public boolean isWithinBounds(int d) {
        return lowerBound <= d && d <= upperBound;
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
