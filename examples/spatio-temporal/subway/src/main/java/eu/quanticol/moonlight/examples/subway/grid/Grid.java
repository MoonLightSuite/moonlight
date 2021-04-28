package eu.quanticol.moonlight.examples.subway.grid;

import eu.quanticol.moonlight.io.DataReader;
import eu.quanticol.moonlight.io.parsing.AdjacencyExtractor;
import eu.quanticol.moonlight.io.FileType;
import eu.quanticol.moonlight.io.parsing.ParsingStrategy;
import eu.quanticol.moonlight.domain.DoubleDistance;
import eu.quanticol.moonlight.space.DistanceStructure;
import eu.quanticol.moonlight.space.SpatialModel;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.TestUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * We are assuming the Subway network is part of an N x N grid,
 * where all the edges represent the same distance.
 */
public class Grid {

    public SpatialModel<Double> getModel(InputStream file) {
        ParsingStrategy<SpatialModel<Double>> s = new AdjacencyExtractor();
        DataReader<SpatialModel<Double>> data = new DataReader<>(file, FileType.TEXT, s);

        return data.read();
    }

    /**
     * Predicate that, given two locations, converts them in grid coordinates
     * and checks whether the direction is consistent with the new location.
     * @param nl identifier of the new location
     * @param ol identifier of the old location
     * @param dir direction to consider
     * @param size dimension of the (squared) grid
     * @return the result of the comparison between the direction and the locations.
     */
    public static Boolean checkDirection(int nl, int ol, GridDirection dir, int size) {
        int nx = fromArray(nl, size).getFirst();
        int ny = fromArray(nl, size).getSecond();
        int ox = fromArray(ol, size).getFirst();
        int oy = fromArray(ol, size).getSecond();

        switch(dir) {
            case NE:
                return (ny == oy + 1) && (nx == ox + 1);
            case NW:
                return (ny == oy + 1) && (nx == ox - 1);
            case SE:
                return (ny == oy - 1) && (nx == ox + 1);
            case SW:
                return (ny == oy - 1) && (nx == ox - 1);
            case NN:
                return ny == oy + 1;
            case SS:
                return ny == oy - 1;
            case WW:
                return nx == ox - 1;
            case EE:
                return nx == ox + 1;
            case HH:
                return nx == ox && ny == oy;
            default:
                throw new UnsupportedOperationException("Invalid direction provided.");
        }

    }

    /**
     * Locally generated grid
     * @return a spatial grid of fixed size
     */
    public static SpatialModel<Double> simulateModel() {
        return generateNGrid(3);
    }

    /**
     * Generates an N x N grid and returns a SpatialModel derived from it
     * @param d the N dimension of the N x N Grid
     * @return an N-Grid spatial model
     */
    public static SpatialModel<Double> generateNGrid(int d) {
        HashMap<Pair<Integer, Integer>, Double> cityMap = new HashMap<>();

        for(int i = 0; i < d; i++)
            for(int j = 0; j < d; j++) {
                List<Integer> ns = getNeighbours(i, j, d);
                for(int n : ns)
                    cityMap.put(new Pair<>(toArray(i,j, d) , n), 1.0);
            }

        return TestUtils.createSpatialModel(d * d, cityMap);
    }

    /**
     * Surroundings of the current node, filtered by a direction
     * @param loc the current node
     * @param dir the direction of interest
     * @param size dimension of the (quadratic) grid
     * @return a List of the serialized coordinates of the nodes.
     *
     * @see #toArray for details on the serialization technique
     */
    public static List<Integer> getNeighboursByDirection(int loc, GridDirection dir, int size) {
        int x = Grid.fromArray(loc, size).getFirst();
        int y = Grid.fromArray(loc, size).getSecond();
        List<Integer> neighbours = getNeighbours(x, y, size);

        // remove neighbours not in the right direction
        neighbours.removeIf(n -> !checkDirection(n, loc, dir, size));

        return neighbours;
    }

    /**
     * Surroundings of the current node
     * @param x first coordinate of the current node
     * @param y second coordinate of the current node
     * @param size dimension of the (quadratic) grid
     * @return a List of the serialized coordinates of the nodes.
     *
     * @see #toArray for details on the serialization technique
     */
    public static List<Integer> getNeighbours(int x, int y, int size) {
        List<Integer> neighbours = new ArrayList<>();

        // left boundary
        if(x > 0)
            neighbours.add(toArray(x - 1, y, size));

        // top boundary
        if(y > 0)
            neighbours.add(toArray(x, y - 1, size));

        // right boundary
        if(x < size - 1)
            neighbours.add(toArray(x + 1, y, size));

        // bottom boundary
        if(y < size - 1)
            neighbours.add(toArray(x, y + 1, size));

        // top-left corner
        if(x > 0 && y > 0)
            neighbours.add(toArray(x - 1, y - 1, size));

        // bottom-right corner
        if(x < size - 1 && y < size - 1)
            neighbours.add(toArray(x + 1, y + 1, size));

        // top-right corner
        if(x > 0 && y < size - 1)
            neighbours.add(toArray(x - 1, y + 1, size));

        // bottom-left corner
        if(x < size - 1 && y > 0)
            neighbours.add(toArray(x + 1, y - 1, size));

        return neighbours;
    }

    /**
     * Given a pair of coordinates of a node,
     * it returns their array-style version
     * @param x first coordinate of the node
     * @param y second coordinate of the node
     * @param size the dimension of the squared matrix
     * @return an int corresponding  to the serialized coordinates.
     */
    private static int toArray(int x, int y, int size) {
        return x + y * size;
    }

    /**
     * Given the dimension n of the square matrix, it converts a position in the
     * array to a position in the n x n matrix.
     * @param a position in the array
     * @param size dimension of the matrix
     * @return the pair (x,y) of coordinates in the matrix.
     */
    private static Pair<Integer, Integer> fromArray(int a, int size) {
        int x = a % size;
        int y = a / size;
        return new Pair<>(x, y);
    }

    /**
     * It calculates the proper distance, given a spatial model
     *
     * @param lowerBound double representing the starting position
     * @param upperBound double representing the ending position
     * @return a DoubleDistance object, meaningful in the given Spatial Model
     */
    public static Function<SpatialModel<Double>, DistanceStructure<Double, ?>> distance(double lowerBound, double upperBound) {
        return g -> new DistanceStructure<>(x -> x, new DoubleDistance(), lowerBound, upperBound, g);
    }

}
