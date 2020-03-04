package eu.quanticol.moonlight.examples.subway;

import eu.quanticol.moonlight.examples.subway.Parsing.AdjacencyExtractor;
import eu.quanticol.moonlight.examples.subway.Parsing.FileType;
import eu.quanticol.moonlight.examples.subway.Parsing.ParsingStrategy;
import eu.quanticol.moonlight.signal.GraphModel;
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

    public SpatialModel<Double> getModel(String file) {
        ParsingStrategy<GraphModel<Double>> s = new AdjacencyExtractor();
        DataReader<GraphModel<Double>> data = new DataReader<>(file, FileType.TEXT, s);

        return data.read();
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
    private static SpatialModel<Double> generateNGrid(int d) {
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
     * Surroundings of the current node
     * @param x first coordinate of the current node
     * @param y second coordinate of the current node
     * @return a List of the serialized coordinates of the nodes.
     *
     * @see #toArray for details on the serialization technique
     */
    private static List<Integer> getNeighbours(int x, int y, int size) {
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

}
