package eu.quanticol.moonlight.algorithms.online;

import eu.quanticol.moonlight.algorithms.SpaceUtilities;
import eu.quanticol.moonlight.domain.DoubleDistance;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.signal.online.TimeChain;
import eu.quanticol.moonlight.signal.online.TimeSegment;
import eu.quanticol.moonlight.signal.online.Update;
import eu.quanticol.moonlight.space.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

import static eu.quanticol.moonlight.util.TestUtils.listOf;
import static org.junit.jupiter.api.Assertions.*;

class SpatialComputationTest {

    private static final SignalDomain<Double> DOUBLES = new DoubleDomain();


    @Test
    void testEmptyLocationService() {
        LocationService<Double, Double> locSvc = new LocationServiceList<>();

        assertThrows(UnsupportedOperationException.class,
                     () -> somewhere(locSvc));
    }


    @Test
    void test1() {
        SpatialModel<Double> m = basicGraph();
        LocationService<Double, Double> locSvc = new StaticLocationService<>(m);



        SpatialComputation<Double, Double, Double> op = somewhere(locSvc);
        TimeChain<Double, List<Double>> ups = basicUpdates();
        TimeChain<Double, List<Double>> result = op.computeUnaryChain(ups);
        System.out.println(result);
    }

    private static TimeChain<Double, List<Double>> basicUpdates() {
        TimeChain<Double, List<Double>> updates = new TimeChain<>(5.0);
        List<Double> v1 = listOf(1.0, 1.0, 1.0, 1.0, 1.0);
        List<Double> v2 = listOf(2.0, 2.0, 2.0, 2.0, 2.0);
        updates.add(new TimeSegment<>(0.0, v1));
        updates.add(new TimeSegment<>(1.0, v2));

        return updates;
    }

    private static SpatialComputation<Double, Double, Double> somewhere(
            LocationService<Double, Double> locSvc)
    {
        // Distance bounds of the spatial operators
        double min = 0;
        double max = 5;

        return new SpatialComputation<>(locSvc,
                                        distance(min, max),
                                        SpatialComputationTest::somewhereOp);
    }

    /**
     * This method generates the following graph:
     * <pre>
     *   O - - 1
     *    \   /
     *      2
     *    /   \
     *   3 - - 4
     * </pre>
     *
     * Each edge is bidirectional (i.e. an arc) and has the same value (i.e. 1)
     * @return  the depicted spatial model
     */
    private SpatialModel<Double> basicGraph() {
        ImmutableGraphModel<Double> model = new ImmutableGraphModel<>(5);
        double d = 1;
        // 0 <-> 1
        model.add(0, d, 1);
        model.add(1, d, 0);
        // 0 <-> 2
        model.add(0, d, 2);
        model.add(2, d, 0);
        // 1 <-> 2
        model.add(1, d, 2);
        model.add(2, d, 1);
        // 2 <-> 3
        model.add(3, d, 2);
        model.add(2, d, 3);
        // 2 <-> 4
        model.add(4, d, 2);
        model.add(2, d, 4);
        // 3 <-> 4
        model.add(3, d, 4);
        model.add(4, d, 3);
        return model;
    }


    public static Function<SpatialModel<Double>, DistanceStructure<Double, ?>>
    distance(double lowerBound, double upperBound)
    {
        return g -> new DistanceStructure<>(x -> x,
                                            new DoubleDistance(),
                                            lowerBound,
                                            upperBound,
                                            g);
    }

    private static List<Double> somewhereOp(IntFunction<Double> s,
                                            DistanceStructure<Double, ?> ds)
    {
        return SpaceUtilities.somewhere(DOUBLES, s, ds);
    }

}