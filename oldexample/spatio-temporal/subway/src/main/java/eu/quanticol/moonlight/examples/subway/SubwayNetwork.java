package eu.quanticol.moonlight.examples.subway;

import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.TestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * We are assuming the Subway network is part of an N x N grid,
 * where all the edges represent the same distance.
 */
public class SubwayNetwork {
    private static final int SIZE = 3;

    public static SpatialModel<Double>  getModel() {
        HashMap<Pair<Integer, Integer>, Double> cityMap = new HashMap<>();

        for(int i = 0; i < SIZE; i++)
            for(int j = 0; j < SIZE; j++) {
                    List<Integer> ns = getNeighbours(i, j);
                    for(int n : ns)
                        cityMap.put(new Pair<>(toArray(i,j) , n), 1.0);
                }

        return TestUtils.createSpatialModel(SIZE * SIZE, cityMap);
    }

    /**
     * Surroundings of the current node
     * @param x first coordinate of the current node
     * @param y second coordinate of the current node
     * @return a List of the serialized coordinates of the nodes.
     *
     * @see #toArray for details on the serialization technique
     */
    private static List<Integer> getNeighbours(int x, int y) {
        List<Integer> neighbours = new ArrayList<>();

        // left boundary
        if(x > 0)
            neighbours.add(toArray(x - 1, y));

        // top boundary
        if(y > 0)
            neighbours.add(toArray(x, y - 1));

        // right boundary
        if(x < SIZE - 1)
            neighbours.add(toArray(x + 1, y));

        // bottom boundary
        if(y < SIZE - 1)
            neighbours.add(toArray(x, y + 1));

        // top-left corner
        if(x > 0 && y > 0)
            neighbours.add(toArray(x - 1, y - 1));

        // bottom-right corner
        if(x < SIZE - 1 && y < SIZE - 1)
            neighbours.add(toArray(x + 1, y + 1));

        // top-right corner
        if(x > 0 && y < SIZE - 1)
            neighbours.add(toArray(x - 1, y + 1));

        // bottom-left corner
        if(x < SIZE - 1 && y > 0)
            neighbours.add(toArray(x + 1, y - 1));

        return neighbours;
    }

    /**
     * Given a pair of coordinates of a node,
     * it returns their array-style version
     * @param x first coordinate of the node
     * @param y second coordinate of the node
     * @return an int corresponding  to the serialized coordinates.
     */
    private static int toArray(int x, int y) {
        return x + y * SIZE;
    }

    /**
     * Returns the total dimension of the matrix
     * @return n x n
     */
    public static int getSize() {
        return SIZE * SIZE;
    }
}
