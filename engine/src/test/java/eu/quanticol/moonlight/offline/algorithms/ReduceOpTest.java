package eu.quanticol.moonlight.offline.algorithms;

import eu.quanticol.moonlight.core.space.DefaultDistanceStructure;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.domain.IntegerDomain;
import eu.quanticol.moonlight.space.StaticLocationService;
import eu.quanticol.moonlight.util.Utils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import static eu.quanticol.moonlight.offline.TestSignalUtils.basicSetSignal;
import static eu.quanticol.moonlight.offline.TestSignalUtils.basicSignal;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReduceOpTest {

    @Test
    void reduceWorksOnTheTrivialSystem() {
        int size = 1;
        var locSet = allLocations(size);
        var r = new ReduceOp<>(size, basicLocSvc(size), anywhere(), this::sum);

        var result = r.computeUnary(locSet, l -> basicSetSignal(size, locSet));

        assertEquals(2, result.getValueAt(0, 0.0));
        assertEquals(3, result.getValueAt(0, 2.0));
    }

    private static Function<SpatialModel<Integer>,
            DistanceStructure<Integer, ?>> anywhere() {
        return x -> trivialDistance(x, Integer.MAX_VALUE);
    }

    private static DistanceStructure<Integer, ?> trivialDistance(
            SpatialModel<Integer> model, int max) {
        return new DefaultDistanceStructure<>(x -> x,
                new IntegerDomain(), 0, max,
                model);
    }

    private static int[] allLocations(int amount) {
        return IntStream.range(0, amount).toArray();
    }

    private static LocationService<Double, Integer> basicLocSvc(int locations) {
        return new StaticLocationService<>(model(locations));
    }

    private static SpatialModel<Integer> model(int locations) {
        return Utils.createGridModel(locations / 2, locations / 2, false, 1);
    }

    private Integer sum(List<Integer> values) {
        return values.stream().reduce(0, Integer::sum);
    }

    @Test
    void reduceWorksOnSimpleTotalSystem() {
        int size = 3;
        var all = allLocations(size);
        var r = new ReduceOp<>(size, basicLocSvc(size), anywhere(), this::sum);

        var result = r.computeUnary(all, l -> basicSetSignal(size, all));

        assertEquals(6, result.getValueAt(0, 0.0));
        assertEquals(9, result.getValueAt(0, 2.0));
    }

    @Test
    void reduceWorkOnSimpleSystemWithSubsetOfLocations() {
        int size = 3;
        var all = allLocations(size);
        var locSet = onlyLocations(0, 2);
        var r = new ReduceOp<>(size, basicLocSvc(size), anywhere(), this::sum);

        var result = r.computeUnary(all, l -> basicSetSignal(size, locSet));

        assertEquals(4, result.getValueAt(0, 0.0));
        assertEquals(6, result.getValueAt(0, 2.0));
    }

    private int[] onlyLocations(int... locations) {
        return locations;
    }

    @Test
    void nestedReduceWorksAsExpected() {
        int size = 4;
        var all = allLocations(size);
        var locSet = onlyLocations(0, 2);
        var locSvc = basicLocSvc(size);
        var r1 = new ReduceOp<>(size, locSvc, anywhere(), this::sum);
        var r2 = new ReduceOp<>(size, locSvc, nowhere(), this::sum);

        var innerResult = r2.computeUnary(locSet, l -> basicSignal(size));
        var result = r1.computeUnary(all, l -> innerResult);

        assertEquals(12, result.getValueAt(0, 0.0));
        assertEquals(18, result.getValueAt(0, 2.0));
    }

    private static Function<SpatialModel<Integer>,
            DistanceStructure<Integer, ?>> nowhere() {
        return x -> trivialDistance(x, 0);
    }

}
