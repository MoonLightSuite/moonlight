package eu.quanticol.moonlight.tests;

import eu.quanticol.moonlight.algorithms.SpaceUtilities;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.SpatialTemporalMonitoring;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.domain.BooleanDomain;
import eu.quanticol.moonlight.domain.DoubleDistance;
import eu.quanticol.moonlight.core.space.DefaultDistanceStructure;
import eu.quanticol.moonlight.space.GraphModel;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.Utils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import static eu.quanticol.moonlight.algorithms.SpaceUtilities.reach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author loreti
 */
class TestSpatialProperties {

    @Test
    void testGraphBuild() {
        int size = 10;
        SpatialModel<Double> model = Utils.createSpatialModel(size, (x, y) -> (y == (((x + 1) % size)) ? 1.0 : null));

        assertNotNull(model);
    }

    @Test
    void testGraphBuildEdges() {
        int size = 10;
        SpatialModel<Double> model = Utils.createSpatialModel(size, (x, y) -> (y == (((x + 1) % size)) ? 1.0 : null));

        assertNotNull(model);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (j == ((i + 1) % size)) {
                    assertEquals(1.0, model.get(i, j), 0.0);
                } else {
                    assertNull(model.get(i, j));
                }
            }
        }
    }

    @Test
    void testGraphBuildWithMaps() {
        int size = 10;
        HashMap<Pair<Integer, Integer>, Double> map = new HashMap<>();
        map.put(new Pair<>(0, 2), 1.0);
        map.put(new Pair<>(0, 1), 1.0);
        map.put(new Pair<>(2, 3), 1.0);
        map.put(new Pair<>(1, 3), 5.0);

        SpatialModel<Double> model = Utils.createSpatialModel(size, map);
        DefaultDistanceStructure<Double, Double> ds = new DefaultDistanceStructure<>(x -> x, new DoubleDistance(), 0.0, 10.0, model);

        assertNotNull(model);
        assertEquals(1.0, ds.getDistance(0, 1), 1.0, "d(0,1)");
        assertEquals(1.0, ds.getDistance(0, 2), 1.0, "d(0,2)");
        assertEquals(2.0, ds.getDistance(0, 3), 2.0, "d(0,3)");

    }


    @Test
    void testDistanceStructure() {
        int size = 3;
        SpatialModel<Double> model = Utils.createSpatialModel(size, (x, y) -> (y == (((x + 1) % size)) ? 1.0 : null));
        DefaultDistanceStructure<Double, Double> ds = new DefaultDistanceStructure<>(x -> x, new DoubleDistance(), 0.0, 10.0, model);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                assertEquals((j >= i ? j - i : size - i + j), ds.getDistance(i, j), 0.0, "d(" + i + "," + j + "): ");
            }
        }

    }

    @Test
    void testDistanceStructure2() {
        int size = 3;
        SpatialModel<Double> model = Utils.createSpatialModel(size, (x, y) -> (((y == ((x + 1) % size)) || (x == ((y + 1) % size))) ? 1.0 : null));
        DefaultDistanceStructure<Double, Double> ds = new DefaultDistanceStructure<>(x -> x, new DoubleDistance(), 0.0, 10.0, model);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                assertEquals(Math.min(Math.abs(j - i), size - Math.abs(i - j)), ds.getDistance(i, j), 0.0, "d(" + i + "," + j + "): ");
            }
        }

    }

    @Test
    void testGridGenerationNodeIndexes() {
        int rows = 100;
        int columns = 1000;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                int idx = Utils.gridIndexOf(i, j, columns);
                assertEquals(i * columns + j, idx);
                Pair<Integer, Integer> loc = Utils.gridLocationOf(idx, rows, columns);
                assertEquals(i, loc.getFirst().intValue());
                assertEquals(j, loc.getSecond().intValue());
            }
        }
    }

    @Test
    void testDistanceOnGrid() {
        int rows = 40;
        int columns = 40;
        SpatialModel<Double> model = Utils.createGridModel(rows, columns, false, 1.0);
        DefaultDistanceStructure<Double, Double> ds = new DefaultDistanceStructure<>(x -> x, new DoubleDistance(), 0.0, 20.0, model);
        for (int i1 = 0; i1 < rows; i1++) {
            for (int j1 = 0; j1 < columns; j1++) {
                for (int i2 = 0; i2 < rows; i2++) {
                    for (int j2 = 0; j2 < columns; j2++) {
                        assertEquals(
                                Math.abs(i1 - i2) + Math.abs(j1 - j2),
                                ds.getDistance(Utils.gridIndexOf(i1, j1, columns), Utils.gridIndexOf(i2, j2, columns)), 0.0,
                                "d(<" + i1 + "," + j1 + ">,<" + i2 + "," + j2 + ">): ");
                    }
                }

            }
        }
    }

    @Test
    void testSomewhereOnGrid() {
        int rows = 9;
        int columns = 12;
        double range = 10.0;
        int relevantC = 5;
        int relevantR = 5;
        SpatialModel<Double> model = Utils.createGridModel(rows, columns, false, 1.0);
        DefaultDistanceStructure<Double, Double> ds = new DefaultDistanceStructure<>(x -> 1.0, new DoubleDistance(), 0.0, range, model);
        List<Boolean> result = SpaceUtilities.somewhere(
                new BooleanDomain(),
                (i) -> i == Utils.gridIndexOf(relevantR, relevantC, columns),
                ds
        );
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                assertEquals(Math.abs(i - 5) + Math.abs(j - 5) <= range, result.get(Utils.gridIndexOf(i, j, columns)), "<" + i + "," + j + ">:");
            }
        }
    }

    @Test
    void testEverywhereOnGrid() {
        int rows = 9;
        int columns = 12;
        double range = 10.0;
        int relevantC = 5;
        int relevantR = 5;
        SpatialModel<Double> model = Utils.createGridModel(rows, columns, false, 1.0);
        DefaultDistanceStructure<Double, Double> ds = new DefaultDistanceStructure<>(x -> x, new DoubleDistance(), 0.0, range, model);
        List<Boolean> result = SpaceUtilities.everywhere(
                new BooleanDomain(),
                (i) -> i != Utils.gridIndexOf(relevantR, relevantC, columns),
                ds
        );
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                assertEquals(Math.abs(i - 5) + Math.abs(j - 5) > range, result.get(Utils.gridIndexOf(i, j, columns)), "<" + i + "," + j + ">:");
            }
        }
    }

    @Test
    void testEscapeOnGrid() {
        int rows = 35;
        int columns = 35;
        double range = 2.1;
        int wallC = 2;
        int wallR = 2;
        SpatialModel<Double> model = Utils.createGridModel(rows, columns, false, 1.0);
        DefaultDistanceStructure<Double, Double> ds = new DefaultDistanceStructure<>(x -> x, new DoubleDistance(), range, Double.POSITIVE_INFINITY, model);
        List<Boolean> result = ds.escape(
                new BooleanDomain(),
                (i) -> {
                    Pair<Integer, Integer> p = Utils.gridLocationOf(i, rows, columns);
                    return !(((p.getFirst().equals(wallC)) && (p.getSecond() <= wallR))
                            || ((p.getFirst() <= wallC) && (p.getSecond().equals(wallR))));
                }
        );
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                assertEquals((i > wallR) || (j > wallC), result.get(Utils.gridIndexOf(i, j, columns)), "<" + i + "," + j + ">:");
            }
        }
    }


    @Test
    void testReachOnGrid() {
        int rows = 5;
        int columns = 5;
        double range = 10.0;
        SpatialModel<Double> model = Utils.createGridModel(rows, columns, false, 1.0);
        DefaultDistanceStructure<Double, Double> ds = new DefaultDistanceStructure<>(x -> x, new DoubleDistance(), 0.0, range, model);
        List<Boolean> result = reach(
                new BooleanDomain(),
                (i) -> {
                    Pair<Integer, Integer> p = Utils.gridLocationOf(i, rows, columns);
                    return (p.getFirst() % 2 == 0) || (p.getSecond() % 2 == 0);
                }, i -> {
                    Pair<Integer, Integer> p = Utils.gridLocationOf(i, rows, columns);
                    return (p.getFirst() == 4) && (p.getSecond() == 4);
                },
                ds
        );
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                assertEquals((i % 2 == 0) || (j % 2 == 0), result.get(Utils.gridIndexOf(i, j, columns)), "<" + i + "," + j + ">:");
            }
        }
    }

    @Test
    void testSpatial2Nodes() {
        int size = 2;
        GraphModel<Double> city = new GraphModel<>(size);
        city.add(0, 2.0, 1);
        city.add(1, 2.0, 0);
        ArrayList<Boolean> s1 = new ArrayList<>(Arrays.asList(false, false));
        ArrayList<Boolean> s2 = new ArrayList<>(Arrays.asList(true, false));
        double range = 10;
        DefaultDistanceStructure<Double, Double> minutes = new DefaultDistanceStructure<>(x -> x, new DoubleDistance(), 0.0, range, city);

        List<Boolean> results = reach(new BooleanDomain(), s1::get, s2::get, minutes);

        Boolean[] objects = results.toArray(new Boolean[0]);
        assertArrayEquals(objects, new Boolean[]{true, false});
    }

    @Test
    void testReachOnSpatial2NodesInsufficientDistance() {
        int size = 2;
        GraphModel<Double> city = new GraphModel<>(size);
        city.add(0, 17.0, 1);
        city.add(1, 17.0, 0);
        ArrayList<Boolean> s1 = new ArrayList<>(Arrays.asList(false, false));
        ArrayList<Boolean> s2 = new ArrayList<>(Arrays.asList(true, false));
        double range = 10;
        DefaultDistanceStructure<Double, Double> minutes = new DefaultDistanceStructure<>(x -> x, new DoubleDistance(), 0.0, range, city);

        List<Boolean> results = reach(new BooleanDomain(), s1::get, s2::get, minutes);

        Boolean[] objects = results.toArray(new Boolean[0]);
        assertArrayEquals(objects, new Boolean[]{true, false});
    }

    @Test
    void testReachOnSpatial2NodesInsufficientDistance2() {
        int size = 3;
        GraphModel<Double> city = new GraphModel<>(size);
        city.add(0, 5.0, 1);
        city.add(1, 5.0, 0);
        city.add(1, 5.0, 2);
        city.add(2, 5.0, 1);
        ArrayList<Boolean> s1 = new ArrayList<>(Arrays.asList(true, true, false));
        ArrayList<Boolean> s2 = new ArrayList<>(Arrays.asList(false, false, true));
        double range = 1;
        DefaultDistanceStructure<Double, Double> minutes = new DefaultDistanceStructure<>(x -> x, new DoubleDistance(), 0.0, range, city);

        List<Boolean> results = reach(new BooleanDomain(), s1::get, s2::get, minutes);

        Boolean[] objects = results.toArray(new Boolean[0]);
        assertArrayEquals(new Boolean[]{range>=10, range>=5,true}, objects);
    }

    @Test
    void testReachOnSpatial5NodesInsufficientDistance3() {
        int size = 4;
        GraphModel<Double> city = new GraphModel<>(size);
        city.add(0, 1.0, 1);
        city.add(1, 1.0, 2);
        city.add(2, 1.0, 3);
        ArrayList<Boolean> s1 = new ArrayList<>(Arrays.asList(true, true, true, false));
        ArrayList<Boolean> s2 = new ArrayList<>(Arrays.asList(false, false, false, true));
        double range = 1;
        DefaultDistanceStructure<Double, Double> minutes = new DefaultDistanceStructure<>(x -> x, new DoubleDistance(), 0.0, range, city);

        List<Boolean> results = reach(new BooleanDomain(), s1::get, s2::get, minutes);

        Boolean[] objects = results.toArray(new Boolean[0]);
        System.out.println(new Boolean[0]);
        assertArrayEquals(new Boolean[]{range>2,range>1, range>0,true}, objects);
    }


    @Test
    void testEscapeAndViolationOfLowerBound() {
        //T -10 -> T
        int size = 2;
        double distance = 10;
        GraphModel<Double> city = new GraphModel<>(size);
        city.add(0, distance, 1);
        ArrayList<Boolean> s = new ArrayList<>(Arrays.asList(true, true));
        DefaultDistanceStructure<Double, Double> distanceStructure = new DefaultDistanceStructure<>(x -> x, new DoubleDistance(), distance + 1, Double.MAX_VALUE, city);

        List<Boolean> results = distanceStructure.escape(new BooleanDomain(), s::get);

        assertFalse(results.get(0));
    }


    @Test
    void testEscapeAndViolationOfUpperBound() {
        //T -10 -> T   Distance:(0,9)
        int size = 2;
        double distance = 10;
        GraphModel<Double> city = new GraphModel<>(size);
        city.add(0, distance, 1);
        ArrayList<Boolean> s = new ArrayList<>(Arrays.asList(true, false));
        DefaultDistanceStructure<Double, Double> distanceStructure = new DefaultDistanceStructure<>(x -> x, new DoubleDistance(), 0., distance - 1, city);

        List<Boolean> results = distanceStructure.escape(new BooleanDomain(), s::get);

        assertTrue(results.get(0));
    }

    @Test
    void testEscapeAndViolationOfUpperBound2() {
        //T -10 -> F  Distance:(0,10)
        Integer size = 2;
        double distance = 10;
        GraphModel<Double> city = new GraphModel<>(3);
        city.add(0, distance, 1);
        city.add(1, distance, 2);

        ArrayList<Boolean> s = new ArrayList<>(Arrays.asList(true, false, false));
        DefaultDistanceStructure<Double, Double> distanceStructure = new DefaultDistanceStructure<>(x -> x, new DoubleDistance(), 0., 2 * distance + 1, city);

        List<Boolean> results = distanceStructure.escape(new BooleanDomain(), s::get);

        assertTrue(results.get(0));
    }

    @Test
    void testEscapeAndViolationOfUpperBound3() {
        //T -10 -> F
        int size = 2;
        double distance = 10;
        GraphModel<Double> city = new GraphModel<>(size);
        city.add(0, distance, 1);
        ArrayList<Boolean> s = new ArrayList<>(Arrays.asList(true, false));
        DefaultDistanceStructure<Double, Double> distanceStructure = new DefaultDistanceStructure<>(x -> x, new DoubleDistance(), 0., distance - 1, city);

        List<Boolean> results = distanceStructure.escape(new BooleanDomain(), s::get);

        assertTrue(results.get(0));
    }

    @Test
    void testPropGraphBuildWithMaps() {
        int size = 10;
        HashMap<Pair<Integer, Integer>, Double> map = new HashMap<>();
        map.put(new Pair<>(0, 2), 1.0);
        map.put(new Pair<>(0, 1), 1.0);
        map.put(new Pair<>(2, 3), 1.0);
        map.put(new Pair<>(1, 3), 5.0);

        SpatialModel<Double> model = Utils.createSpatialModel(size, map);
        DefaultDistanceStructure<Double, Double> ds = new DefaultDistanceStructure<>(x -> x, new DoubleDistance(), 0.0, 10.0, model);

        assertNotNull(model);
        assertEquals(1.0, ds.getDistance(0, 1), 0.0, "d(0,1)");
        assertEquals(1.0, ds.getDistance(0, 2), 0.0, "d(0,2)");
        assertEquals(2.0, ds.getDistance(0, 3), 0.0, "d(0,3)");

    }

    @Test
    void testPropGraphBuildWithMaps2() {
        int size = 5;
        HashMap<Pair<Integer, Integer>, Double> map = new HashMap<>();
        map.put(new Pair<>(0, 1), 1.0);
        map.put(new Pair<>(0, 3), 1.0);
        map.put(new Pair<>(0, 4), 1.0);
        map.put(new Pair<>(1, 0), 1.0);
        map.put(new Pair<>(1, 2), 1.0);
        map.put(new Pair<>(1, 4), 1.0);
        map.put(new Pair<>(2, 1), 1.0);
        map.put(new Pair<>(2, 3), 1.0);
        map.put(new Pair<>(2, 4), 1.0);
        map.put(new Pair<>(3, 0), 1.0);
        map.put(new Pair<>(3, 2), 1.0);
        map.put(new Pair<>(3, 4), 1.0);
        map.put(new Pair<>(4, 0), 1.0);
        map.put(new Pair<>(4, 1), 1.0);
        map.put(new Pair<>(4, 2), 1.0);
        map.put(new Pair<>(4, 3), 1.0);

        SpatialModel<Double> model = Utils.createSpatialModel(size, map);
        List<Integer> typeOfNode = Arrays.asList(1, 3, 3, 3, 3);

        SpatialTemporalSignal<Integer> signal = Utils.createSpatioTemporalSignal(size, 0, 1, 1.0,
                (t, l) -> new Integer(typeOfNode.get(l)));
        //// Loc Service Static ///
        LocationService<Double, Double> locService = Utils.createLocServiceStatic(0, 1, 1,model);

        System.out.println(signal.valuesatT(0));


        HashMap<String, Function<Parameters, Function<Integer, Boolean>>> atomicFormulas = new HashMap<>();
        atomicFormulas.put("type1", p -> (x -> x == 1));
        atomicFormulas.put("type2", p -> (x -> x == 2));
        atomicFormulas.put("type3", p -> (x -> x == 3));

        HashMap<String, Function<SpatialModel<Double>, DistanceStructure<Double, ?>>> distanceFunctions = new HashMap<>();
        distanceFunctions.put("dist", m -> new DefaultDistanceStructure<>(x -> x , new DoubleDistance(), 0.0, 1.0, m));

        Formula reach = new ReachFormula(new AtomicFormula("type3"),"dist", new AtomicFormula("type1"));

        //// MONITOR /////
        SpatialTemporalMonitoring<Double, Integer, Boolean> monitor =
                new SpatialTemporalMonitoring<>(
                        atomicFormulas,
                        distanceFunctions,
                        new BooleanDomain(),
                        true);

        SpatialTemporalMonitor<Double,Integer,Boolean> m = monitor.monitor(reach, null);
        SpatialTemporalSignal<Boolean> sout = m.monitor(locService, signal);
        List<Signal<Boolean>> signals = sout.getSignals();
        for (int i = 0; i < size; i++) {
            System.out.println(signals.get(i).valueAt(1));
        }
        assertEquals(false, signals.get(2).valueAt(1));

    }

    @Test
    void testLoadGraphFromAdiacenceList() {
        double[][] graph = new double[][] {
                new double[] {0, 1, 1, 297.683377582777},
                new double[] {0, 2, 1, 696.654592727676},
                new double[] {0, 3, 1, 362.022924952817},
                new double[] {0, 4, 1, 443.026473778508},
                new double[] {1, 0, 1, 297.683377582777},
                new double[] {1, 2, 1, 667.669013033577},
                new double[] {2, 0, 1, 696.654592727676},
                new double[] {2, 1, 1, 667.669013033577},
                new double[] {2, 4, 1, 759.655443722058},
                new double[] {3, 0, 1, 362.022924952817},
                new double[] {3, 4, 1, 135.113318099020},
                new double[] {4, 0, 1, 443.026473778508},
                new double[] {4, 2, 1, 759.655443722058},
                new double[] {4, 3, 1, 135.113318099020}
        };
        RecordHandler rh = new RecordHandler(DataHandler.INTEGER,DataHandler.REAL);
        SpatialModel.buildSpatialModelFromAdjacencyList(6,rh,graph);
        assertTrue(true);

    }


}


