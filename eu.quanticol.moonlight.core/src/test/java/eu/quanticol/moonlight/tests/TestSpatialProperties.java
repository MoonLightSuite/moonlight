/**
 *
 */
package eu.quanticol.moonlight.tests;

import eu.quanticol.moonlight.formula.BooleanDomain;
import eu.quanticol.moonlight.formula.DoubleDistance;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.GraphModel;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.util.Pair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author loreti
 *
 */
public class TestSpatialProperties {

    @Test
    public void testGraphBuild() {
        int size = 10;
        SpatialModel<Double> model = TestUtils.createSpatialModel(size, (x, y) -> (y == (((x + 1) % size)) ? 1.0 : null));

        assertNotNull(model);
    }

    @Test
    public void testGraphBuildEdges() {
        int size = 10;
        SpatialModel<Double> model = TestUtils.createSpatialModel(size, (x, y) -> (y == (((x + 1) % size)) ? 1.0 : null));

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
    public void testDistanceStructure() {
        int size = 3;
        SpatialModel<Double> model = TestUtils.createSpatialModel(size, (x, y) -> (y == (((x + 1) % size)) ? 1.0 : null));
        DistanceStructure<Double, Double> ds = new DistanceStructure<Double, Double>(x -> x, new DoubleDistance(), 0.0, 10.0, model);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                assertEquals("d(" + i + "," + j + "): ", (j >= i ? j - i : size - i + j), ds.getDistance(i, j), 0.0);
            }
        }

    }

    @Test
    public void testDistanceStructure2() {
        int size = 3;
        SpatialModel<Double> model = TestUtils.createSpatialModel(size, (x, y) -> (((y == ((x + 1) % size)) || (x == ((y + 1) % size))) ? 1.0 : null));
        DistanceStructure<Double, Double> ds = new DistanceStructure<Double, Double>(x -> x, new DoubleDistance(), 0.0, 10.0, model);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                assertEquals("d(" + i + "," + j + "): ", Math.min(Math.abs(j - i), size - Math.abs(i - j)), ds.getDistance(i, j), 0.0);
            }
        }

    }

    @Test
    public void testGridGenerationNodeIndexes() {
        int rows = 100;
        int columns = 1000;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                int idx = TestUtils.gridIndexOf(i, j, columns);
                assertEquals(i * columns + j, idx);
                Pair<Integer, Integer> loc = TestUtils.gridLocationOf(idx, rows, columns);
                assertEquals(i, loc.getFirst().intValue());
                assertEquals(j, loc.getSecond().intValue());
            }
        }
    }

    @Test
    public void testDistanceOnGrid() {
        int rows = 40;
        int columns = 40;
        SpatialModel<Double> model = TestUtils.createGridModel(rows, columns, false, 1.0);
        DistanceStructure<Double, Double> ds = new DistanceStructure<>(x -> x, new DoubleDistance(), 0.0, 20.0, model);
        for (int i1 = 0; i1 < rows; i1++) {
            for (int j1 = 0; j1 < columns; j1++) {
                for (int i2 = 0; i2 < rows; i2++) {
                    for (int j2 = 0; j2 < columns; j2++) {
                        assertEquals(
                                "d(<" + i1 + "," + j1 + ">,<" + i2 + "," + j2 + ">): ",
                                Math.abs(i1 - i2) + Math.abs(j1 - j2),
                                ds.getDistance(TestUtils.gridIndexOf(i1, j1, columns), TestUtils.gridIndexOf(i2, j2, columns)), 0.0);
                    }
                }

            }
        }
    }

    @Test
    public void testSomewhereOnGrid() {
        int rows = 9;
        int columns = 12;
        double range = 10.0;
        int relevantC = 5;
        int relevantR = 5;
        SpatialModel<Double> model = TestUtils.createGridModel(rows, columns, false, 1.0);
        DistanceStructure<Double, Double> ds = new DistanceStructure<>(x -> x, new DoubleDistance(), 0.0, range, model);
        ArrayList<Boolean> result = ds.somewhere(
                new BooleanDomain(),
                (i) -> i == TestUtils.gridIndexOf(relevantR, relevantC, columns)
        );
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                assertEquals("<" + i + "," + j + ">:", Math.abs(i - 5) + Math.abs(j - 5) <= range, result.get(TestUtils.gridIndexOf(i, j, columns)));
            }
        }
    }

    @Test
    public void testEverywhereOnGrid() {
        int rows = 9;
        int columns = 12;
        double range = 10.0;
        int relevantC = 5;
        int relevantR = 5;
        SpatialModel<Double> model = TestUtils.createGridModel(rows, columns, false, 1.0);
        DistanceStructure<Double, Double> ds = new DistanceStructure<>(x -> x, new DoubleDistance(), 0.0, range, model);
        ArrayList<Boolean> result = ds.everywhere(
                new BooleanDomain(),
                (i) -> i != TestUtils.gridIndexOf(relevantR, relevantC, columns)
        );
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                assertEquals("<" + i + "," + j + ">:", Math.abs(i - 5) + Math.abs(j - 5) > range, result.get(TestUtils.gridIndexOf(i, j, columns)));
            }
        }
    }

    @Test
    public void testEscapeOnGrid() {
        int rows = 5;
        int columns = 5;
        double range = 2.1;
        int wallC = 2;
        int wallR = 2;
        SpatialModel<Double> model = TestUtils.createGridModel(rows, columns, false, 1.0);
        DistanceStructure<Double, Double> ds = new DistanceStructure<>(x -> x, new DoubleDistance(), range, Double.POSITIVE_INFINITY, model);
        ArrayList<Boolean> result = ds.escape(
                new BooleanDomain(),
                (i) -> {
                    Pair<Integer, Integer> p = TestUtils.gridLocationOf(i, rows, columns);
                    return !(((p.getFirst().equals(wallC)) && (p.getSecond() <= wallR))
                            || ((p.getFirst() <= wallC) && (p.getSecond().equals(wallR))));
                }
        );
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                assertEquals("<" + i + "," + j + ">:", (i > wallR) || (j > wallC), result.get(TestUtils.gridIndexOf(i, j, columns)));
            }
        }
    }


    @Test
    public void testReachOnGrid() {
        int rows = 5;
        int columns = 5;
        double range = 10.0;
        SpatialModel<Double> model = TestUtils.createGridModel(rows, columns, false, 1.0);
        DistanceStructure<Double, Double> ds = new DistanceStructure<>(x -> x, new DoubleDistance(), 0.0, range, model);
        ArrayList<Boolean> result = ds.reach(
                new BooleanDomain(),
                (i) -> {
                    Pair<Integer, Integer> p = TestUtils.gridLocationOf(i, rows, columns);
                    return (p.getFirst() % 2 == 0) || (p.getSecond() % 2 == 0);
                }, i -> {
                    Pair<Integer, Integer> p = TestUtils.gridLocationOf(i, rows, columns);
                    return (p.getFirst() == 4) && (p.getSecond() == 4);
                }
        );
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                assertEquals("<" + i + "," + j + ">:", (i % 2 == 0) || (j % 2 == 0), result.get(TestUtils.gridIndexOf(i, j, columns)));
            }
        }
    }

    @Test
    public void testSpatial2Nodes() {
        Integer size = 2;
        GraphModel<Double> city = new GraphModel<>(size);
        city.add(0, 2.0, 1);
        city.add(1, 2.0, 0);
        ArrayList<Boolean> s1 = new ArrayList<>(Arrays.asList(false, false));
        ArrayList<Boolean> s2 = new ArrayList<>(Arrays.asList(true, false));
        double range = 10;
        DistanceStructure<Double, Double> minutes = new DistanceStructure<>(x -> x, new DoubleDistance(), 0.0, range, city);

        List<Boolean> results = minutes.reach(new BooleanDomain(), s1::get, s2::get);

        Boolean[] objects = results.toArray(new Boolean[0]);
        assertArrayEquals(objects, new Boolean[]{true, false});
    }

    @Test
    public void testReachOnSpatial2NodesInsufficientDistance() {
        Integer size = 2;
        GraphModel<Double> city = new GraphModel<>(size);
        city.add(0, 17.0, 1);
        city.add(1, 17.0, 0);
        ArrayList<Boolean> s1 = new ArrayList<>(Arrays.asList(false, false));
        ArrayList<Boolean> s2 = new ArrayList<>(Arrays.asList(true, false));
        double range = 10;
        DistanceStructure<Double, Double> minutes = new DistanceStructure<>(x -> x, new DoubleDistance(), 0.0, range, city);

        List<Boolean> results = minutes.reach(new BooleanDomain(), s1::get, s2::get);

        Boolean[] objects = results.toArray(new Boolean[0]);
        assertArrayEquals(objects, new Boolean[]{true, false});
    }

    @Test
    public void testEscapeAndViolationOfLowerBound() {
        //T -10 -> T
        Integer size = 2;
        double distance = 10;
        GraphModel<Double> city = new GraphModel<>(size);
        city.add(0, distance, 1);
        ArrayList<Boolean> s = new ArrayList<>(Arrays.asList(true, true));
        DistanceStructure<Double, Double> distanceStructure = new DistanceStructure<>(x -> x, new DoubleDistance(), distance + 1, Double.MAX_VALUE, city);

        List<Boolean> results = distanceStructure.escape(new BooleanDomain(), s::get);

        assertFalse(results.get(0));
    }


    @Test
    public void testEscapeAndViolationOfUpperBound() {
        //T -10 -> T   Distance:(0,9)
        Integer size = 2;
        double distance = 10;
        GraphModel<Double> city = new GraphModel<>(size);
        city.add(0, distance, 1);
        ArrayList<Boolean> s = new ArrayList<>(Arrays.asList(true, false));
        DistanceStructure<Double, Double> distanceStructure = new DistanceStructure<>(x -> x, new DoubleDistance(), 0., distance - 1, city);

        List<Boolean> results = distanceStructure.escape(new BooleanDomain(), s::get);

        assertTrue(results.get(0));
    }

    @Test
    public void testEscapeAndViolationOfUpperBound2() {
        //T -10 -> F  Distance:(0,10)
        Integer size = 2;
        double distance = 10;
        GraphModel<Double> city = new GraphModel<>(3);
        city.add(0, distance, 1);
        city.add(1, distance, 2);

        ArrayList<Boolean> s = new ArrayList<>(Arrays.asList(true, false, false));
        DistanceStructure<Double, Double> distanceStructure = new DistanceStructure<>(x -> x, new DoubleDistance(), 0., 2 * distance + 1, city);

        List<Boolean> results = distanceStructure.escape(new BooleanDomain(), s::get);

        assertTrue(results.get(0));
    }

    @Test
    public void testEscapeAndViolationOfUpperBound3() {
        //T -10 -> F
        Integer size = 2;
        double distance = 10;
        GraphModel<Double> city = new GraphModel<>(size);
        city.add(0, distance, 1);
        ArrayList<Boolean> s = new ArrayList<>(Arrays.asList(true, false));
        DistanceStructure<Double, Double> distanceStructure = new DistanceStructure<>(x -> x, new DoubleDistance(), 0., distance - 1, city);

        List<Boolean> results = distanceStructure.escape(new BooleanDomain(), s::get);

        assertTrue(results.get(0));
    }
}
