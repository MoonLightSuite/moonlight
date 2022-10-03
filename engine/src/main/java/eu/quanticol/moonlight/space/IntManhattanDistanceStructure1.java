package eu.quanticol.moonlight.space;

import eu.quanticol.moonlight.core.space.DistanceDomain;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.domain.IntegerDomain;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class IntManhattanDistanceStructure1
        implements DistanceStructure<Integer, Integer> {
    private final int lowerBound;
    private final int upperBound;
    private final RegularGridModel<Integer> model;

    private boolean[][] distanceMatrix;

    public IntManhattanDistanceStructure1(
            int lowerBound,
            int upperBound,
            @NotNull RegularGridModel<Integer> model) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.model = model;
        computeDistanceMatrix();
    }

    private void computeDistanceMatrix() {
        distanceMatrix = new boolean[model.size()][model.size()];
        for (int i = 0; i < model.size(); i++) {
//            int[] jBounds = computeBoundingBox(i);
//            int minJ = jBounds[0];
//            int maxJ = jBounds[1];
            // System.out.println("i: " + i + " minJ: " + minJ + " maxJ: " + 
            // maxJ);
            for (int j = 0; j < model.size(); j++) {
                distanceMatrix[i][j] = isWithinBounds(getIntDistance(i, j));
            }
        }
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

    @Override
    public boolean areWithinBounds(int from, int to) {
        return distanceMatrix[from][to];
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
