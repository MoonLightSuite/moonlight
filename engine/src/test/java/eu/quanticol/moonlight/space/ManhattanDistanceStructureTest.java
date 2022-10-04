package eu.quanticol.moonlight.space;

import eu.quanticol.moonlight.core.space.DistanceDomain;
import eu.quanticol.moonlight.domain.IntegerDomain;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ManhattanDistanceStructureTest {
    DistanceDomain<Integer> INT_DOMAIN = new IntegerDomain();
    int FROM_HERE = 0;
    int TO_TEN = 10;
    int ROWS = 3;
    int COLUMNS = 3;
    int UNARY_WEIGHT = 1;
    final RegularGridModel<Integer> MODEL = new RegularGridModel<>(ROWS,
            COLUMNS,
            UNARY_WEIGHT);

    @Test
    void simpleDistance() {
        var dist = new ManhattanDistanceStructure<>(defaultDistance(),
                INT_DOMAIN, FROM_HERE, TO_TEN, MODEL);

        assertEquals(2, dist.getDistance(0, 2));
        assertEquals(1, dist.getDistance(0, 3));
    }

    private Function<Integer, Integer> defaultDistance() {
        return x -> 1;
    }

    @Test
    void simpleDistanceInt() {
        var dist = new IntManhattanDistanceStructure(FROM_HERE, TO_TEN, MODEL);

        assertEquals(2, dist.getDistance(0, 2));
        assertEquals(1, dist.getDistance(0, 3));
    }

    @Test
    void fullNeighbourhoodOneStepCheck() {
        var dist = new IntManhattanDistanceStructure(1, 1, MODEL);

        assertArrayEquals(new int[]{1, 3}, neighbourhood(0, dist));
        assertArrayEquals(new int[]{0, 2, 4}, neighbourhood(1, dist));
        assertArrayEquals(new int[]{1, 5}, neighbourhood(2, dist));
        assertArrayEquals(new int[]{0, 4, 6}, neighbourhood(3, dist));
        assertArrayEquals(new int[]{1, 3, 5, 7}, neighbourhood(4, dist));
        assertArrayEquals(new int[]{2, 4, 8}, neighbourhood(5, dist));
        assertArrayEquals(new int[]{3, 7}, neighbourhood(6, dist));
        assertArrayEquals(new int[]{4, 6, 8}, neighbourhood(7, dist));
        assertArrayEquals(new int[]{5, 7}, neighbourhood(8, dist));
    }

    private int[] neighbourhood(int location,
                                IntManhattanDistanceStructure dist) {
        var neighbourhood = dist.getNeighbourhood(location);
        Arrays.sort(neighbourhood);
        return neighbourhood;
    }

    @Test
    void fullNeighbourhoodBaseStepCheck() {
        var dist = new IntManhattanDistanceStructure(0, 0, MODEL);
        var test = neighbourhood(0, dist);

        assertArrayEquals(new int[]{0}, test);
    }

    @Test
    void fullNeighbourhoodOneWithBaseStepCheck() {
        var dist = new IntManhattanDistanceStructure(0, 1, MODEL);

        assertArrayEquals(new int[]{0, 1, 3}, neighbourhood(0, dist));
        assertArrayEquals(new int[]{0, 1, 2, 4}, neighbourhood(1, dist));
        assertArrayEquals(new int[]{1, 2, 5}, neighbourhood(2, dist));
        assertArrayEquals(new int[]{0, 3, 4, 6}, neighbourhood(3, dist));
        assertArrayEquals(new int[]{1, 3, 4, 5, 7}, neighbourhood(4, dist));
        assertArrayEquals(new int[]{2, 4, 5, 8}, neighbourhood(5, dist));
        assertArrayEquals(new int[]{3, 6, 7}, neighbourhood(6, dist));
        assertArrayEquals(new int[]{4, 6, 7, 8}, neighbourhood(7, dist));
        assertArrayEquals(new int[]{5, 7, 8}, neighbourhood(8, dist));
    }

    @Test
    void fullNeighbourhoodTwoStepsCheck() {
        var dist = new IntManhattanDistanceStructure(1, 2, MODEL);

        assertArrayEquals(new int[]{0, 1, 2, 3, 4, 6}, neighbourhood(0, dist));
        assertArrayEquals(new int[]{0, 1, 2, 3, 4, 5, 7}, neighbourhood(1,
                dist));
        assertArrayEquals(new int[]{0, 1, 2, 4, 5, 8}, neighbourhood(2, dist));
        assertArrayEquals(new int[]{0, 1, 3, 4, 5, 6, 7}, neighbourhood(3,
                dist));
        assertArrayEquals(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, neighbourhood(4
                , dist));
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 7, 8}, neighbourhood(5,
                dist));
        assertArrayEquals(new int[]{0, 3, 4, 6, 7, 8}, neighbourhood(6, dist));
        assertArrayEquals(new int[]{1, 3, 4, 5, 6, 7, 8}, neighbourhood(7,
                dist));
        assertArrayEquals(new int[]{2, 4, 5, 6, 7, 8}, neighbourhood(8, dist));
    }
}
