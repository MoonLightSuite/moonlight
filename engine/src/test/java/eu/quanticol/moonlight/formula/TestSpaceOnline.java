package eu.quanticol.moonlight.formula;

import eu.quanticol.moonlight.core.base.Box;
import eu.quanticol.moonlight.core.formula.Formula;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.formula.spatial.SomewhereFormula;
import eu.quanticol.moonlight.online.monitoring.OnlineSpatialTemporalMonitor;
import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.core.signal.TimeSignal;
import eu.quanticol.moonlight.online.signal.Update;
import eu.quanticol.moonlight.core.space.DefaultDistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.core.base.Pair;
import eu.quanticol.moonlight.util.Utils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
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
                Utils.createLocServiceStatic(0, 1, T, grid);


        HashMap<String, Function<Double, Box<Double>>>
                atoms = new HashMap<>();

        atoms.put("positiveX", v -> new Box<>(v, v));

        HashMap<String,
                Function<SpatialModel<Double>,
                         DistanceStructure<Double, ?>>> dist = new HashMap<>();
        dist.put("standard",
                    g -> distance(0.0, 1.0).apply(g));

        Formula f = new SomewhereFormula("standard",
                                        new AtomicFormula("positiveX"));

        OnlineSpatialTemporalMonitor<Double, Double, Double> m =
                new OnlineSpatialTemporalMonitor<>(f, N * N, new DoubleDomain(),
                                             locSvc, atoms, dist, true);

//        ForkJoinPool customThreadPool = new ForkJoinPool(12);
//
//        TimeSignal<Double, List<Box<Double>>> r =
//                m.monitor((TimeChain<Double, List<Double>>) null);
//
//        TimeChain<Double, List<Box<Double>>> ss = r.getSegments();
//
//        assertEquals(1, ss.size());

        Update<Double, List<Double>> u = basicUpdate(N * N);
        var r = m.monitor(u);

        var ss = r.getSegments();

        assertEquals(2, ss.size());

        List<Box<Double>> fs = ss.get(0).getValue();

        assertEquals(4, fs.get(0).getStart());
        assertEquals(5, fs.get(1).getStart());
        assertEquals(5, fs.get(2).getStart());
        assertEquals(7, fs.get(3).getStart());
        assertEquals(8, fs.get(4).getStart());
        assertEquals(8, fs.get(5).getStart());
        assertEquals(7, fs.get(6).getStart());
        assertEquals(8, fs.get(7).getStart());
        assertEquals(8, fs.get(8).getStart());

        System.out.println("Grid:");
        IntStream.range(0, N * N)
                 .forEach(x -> System.out.println(fromArray(x, N)));

        System.out.println("Update:");
        IntStream.range(0, N * N)
                 .forEach(x -> System.out.println("Cell " + fromArray(x, N) +
                                                  " -> " + u.getValue().get(x)
                                                            .intValue()));

        System.out.println("Robustness:");
        IntStream.range(0, N * N)
                 .forEach(x -> System.out.println("Cell " + fromArray(x, N) +
                                                  " -> " + fs.get(x)
                                                             .getStart()
                                                             .intValue()));
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

        return Utils.createSpatialModel(d * d, cityMap);
    }

    private static Update<Double, List<Double>> basicUpdate(int n) {
        List<Double> uData =
                IntStream.range(0, n)
                        .boxed()
                        .map(Integer::doubleValue)
                        .collect(Collectors.toList());

        return new Update<>(0.0, 5.0, uData);
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
        return g -> new DefaultDistanceStructure<>(x -> x, new DoubleDomain(), lowerBound, upperBound, g);
    }
}
