package eu.quanticol.moonlight.formula;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.DoubleDistance;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.monitoring.online.OnlineSpaceTimeMonitor;
import eu.quanticol.moonlight.signal.online.*;
import eu.quanticol.moonlight.space.DistanceStructure;
import eu.quanticol.moonlight.space.LocationService;
import eu.quanticol.moonlight.space.SpatialModel;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestSpaceOnline {
    static final double T = 10;
    static final int N = 3;

    @Test
    void testSomewhere() {
        SpatialModel<Double> grid = generateNGrid(N);
        LocationService<Double, Double> locSvc =
                TestUtils.createLocServiceStatic(0, 1, T, grid);


        HashMap<String, Function<Double, AbstractInterval<Double>>>
                atoms = new HashMap<>();

        atoms.put("positiveX", v -> new AbstractInterval<>(v, v));

        HashMap<String, Function<SpatialModel<Double>, DistanceStructure<Double, ?>>> dist = new HashMap<>();
        dist.put("standard",
                    g -> distance(0.0, 1.0).apply(g));

        OnlineSpaceTimeMonitor<Double, Double, Double> m =
                new OnlineSpaceTimeMonitor<>(formula(), N * N, new DoubleDomain(), locSvc, atoms, dist);


        //define update

        List<Double> uData =
            IntStream.range(0, N * N)
                     .boxed()
                     .map(Integer::doubleValue)
                     .collect(Collectors.toList());

        Update<Double, List<Double>> u = new Update<>(0.0, 5.0, uData);

        SignalInterface<Double, List<AbstractInterval<Double>>> r =
                m.monitor(null);

        SegmentChain<Double, List<AbstractInterval<Double>>> ss = r.getSegments();

        List<AbstractInterval<Double>> fs = ss.get(0).getValue();

        for(int i = 0; i < fs.size(); i++)
            System.out.println("Robustness at Location " + i + ": " + fs.get(i));

        assertEquals(ss.size(), 1);

        r = m.monitor(u);

        ss = r.getSegments();

        fs = ss.get(0).getValue();

        for(int i = 0; i < fs.size(); i++)
            System.out.println("Robustness at Location " + i + ": " + fs.get(i));

        System.out.println("Grid:");
        IntStream.range(0, N * N).forEach(x -> System.out.println(fromArray(x, N)));

        System.out.println("Update:");
        IntStream.range(0, N * N).forEach(x -> System.out.println("Cell " + fromArray(x, N) + " -> " + uData.get(x).intValue()));

        System.out.println("Robustness:");
        List<AbstractInterval<Double>> vs = fs;
        IntStream.range(0, N * N).forEach(x -> System.out.println("Cell " + fromArray(x, N) + " -> " + vs.get(x).getStart().intValue()));

    }

    private static Formula formula() {
        return new SomewhereFormula("standard",
                                    new AtomicFormula("positiveX"));
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
