package eu.quanticol.moonlight.offline.algorithms;

import eu.quanticol.moonlight.core.space.DefaultDistanceStructure;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.domain.IntegerDomain;
import eu.quanticol.moonlight.space.GraphModel;
import eu.quanticol.moonlight.space.StaticLocationService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static eu.quanticol.moonlight.offline.TestSignalUtils.basicSignal;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReduceOpTest {

    @Test
    void reduceWorksOnTheTrivialSystem() {
        var locSet = new int[]{0};
        var locSvc = new StaticLocationService<>(trivialModel());
        var r = new ReduceOp<>(1, locSvc, anyLocation(), this::sum);

        var result = r.computeUnary(locSet, l -> basicSignal(1, locSet));
        System.out.println(result);

        var firstValue = result.getSignalAtLocation(0).getValueAt(0.0);
        var secondValue = result.getSignalAtLocation(0).getValueAt(2.0);
        assertEquals(2, firstValue);
        assertEquals(3, secondValue);
    }

    private static Function<SpatialModel<Integer>,
            DistanceStructure<Integer, ?>> anyLocation() {
        return x -> trivialDistance(x, Integer.MAX_VALUE);
    }

    private static DistanceStructure<Integer, ?> trivialDistance(
            SpatialModel<Integer> model, int max) {
        return new DefaultDistanceStructure<>(x -> x,
                new IntegerDomain(), 0, max,
                model);
    }

    private Integer sum(List<Integer> values) {
        return values.stream().reduce(0, Integer::sum);
    }

    private SpatialModel<Integer> trivialModel() {
        return new GraphModel<>(1);
    }

    @Test
    void reduceWorksOnSimpleTotalSystem() {
        var locSet = new int[]{0, 1, 2};
        var locSvc = new StaticLocationService<>(trivialModel());
        var r = new ReduceOp<>(3, locSvc, anyLocation(), this::sum);

        var result = r.computeUnary(locSet, l -> basicSignal(3, locSet));
        System.out.println(result);

        var firstValue = result.getSignalAtLocation(0).getValueAt(0.0);
        var secondValue = result.getSignalAtLocation(0).getValueAt(2.0);
        assertEquals(6, firstValue);
        assertEquals(9, secondValue);
    }

    @Test
    void reduceWorkOnSimpleSystemWithSubsetOfLocations() {
        var locSet = new int[]{0, 2};
        var locSvc = new StaticLocationService<>(trivialModel());
        var r = new ReduceOp<>(3, locSvc, anyLocation(), this::sum);

        var result = r.computeUnary(locSet, l -> basicSignal(3, locSet));
        System.out.println(result);

        var firstValue = result.getSignalAtLocation(0).getValueAt(0.0);
        var secondValue = result.getSignalAtLocation(0).getValueAt(2.0);
        assertEquals(4, firstValue);
        assertEquals(6, secondValue);
    }

    @Disabled
    @Test
    void reduceWorkOnSimpleSystemWithSubsetOfLocations2() {
        var locSet = new int[]{0, 2};
        var locSvc = new StaticLocationService<>(trivialModel());
        var r = new ReduceOp<>(3, locSvc, anyLocation(), this::sum);

        var result = r.computeUnary(locSet, l -> basicSignal(3, locSet));
        System.out.println(result);

        var firstValue = result.getSignalAtLocation(0).getValueAt(0.0);
        var secondValue = result.getSignalAtLocation(0).getValueAt(2.0);
        assertEquals(4, firstValue);
        assertEquals(6, secondValue);
    }

}
