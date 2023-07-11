package io.github.moonlightsuite.moonlight.online.algorithms;

import io.github.moonlightsuite.moonlight.core.algorithms.EscapeAlgorithm;
import io.github.moonlightsuite.moonlight.core.algorithms.SpatialAlgorithms;
import io.github.moonlightsuite.moonlight.core.signal.SignalDomain;
import io.github.moonlightsuite.moonlight.core.space.DefaultDistanceStructure;
import io.github.moonlightsuite.moonlight.core.space.DistanceStructure;
import io.github.moonlightsuite.moonlight.core.space.LocationService;
import io.github.moonlightsuite.moonlight.core.space.SpatialModel;
import io.github.moonlightsuite.moonlight.domain.DoubleDomain;
import io.github.moonlightsuite.moonlight.online.signal.TimeChain;
import io.github.moonlightsuite.moonlight.online.signal.TimeSegment;
import io.github.moonlightsuite.moonlight.online.signal.Update;
import io.github.moonlightsuite.moonlight.space.ImmutableGraphModel;
import io.github.moonlightsuite.moonlight.space.LocationServiceList;
import io.github.moonlightsuite.moonlight.space.StaticLocationService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

import static io.github.moonlightsuite.moonlight.TestUtils.listOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpatialOpTest {
    private static final SignalDomain<Double> DOUBLES = new DoubleDomain();
    private static final boolean PARALLEL = true;
    private static final boolean SEQUENTIAL = false;

    private static TimeChain<Double, List<Double>> basicUpdates() {
        TimeChain<Double, List<Double>> updates = new TimeChain<>(5.0);
        List<Double> v1 = listOf(1.0, 1.0, 1.0, 1.0, 1.0);
        List<Double> v2 = listOf(2.0, 2.0, 2.0, 2.0, 2.0);
        updates.add(new TimeSegment<>(0.0, v1));
        updates.add(new TimeSegment<>(1.0, v2));

        return updates;
    }

    private static SpatialOp<Double, Double, Double> somewhere(
            LocationService<Double, Double> locSvc, boolean parallel) {
        // Distance bounds of the spatial operators
        double min = 0;
        double max = 5;

        if (!parallel)
            return new SpatialOp<>(locSvc,
                    distance(min, max),
                    SpatialOpTest::somewhereOp);
        else
            return new SpatialOp<>(locSvc,
                    distance(min, max),
                    SpatialOpTest::somewhereOpParallel);
    }

    private static SpatialOp<Double, Double, Double> everywhere(
            LocationService<Double, Double> locSvc, boolean parallel) {
        // Distance bounds of the spatial operators
        double min = 0;
        double max = 5;

        if (!parallel)
            return new SpatialOp<>(locSvc,
                    distance(min, max),
                    SpatialOpTest::everywhereOp);
        else
            return new SpatialOp<>(locSvc,
                    distance(min, max),
                    SpatialOpTest::everywhereOpParallel);
    }

    private static SpatialOp<Double, Double, Double> escape(
            LocationService<Double, Double> locSvc) {
        // Distance bounds of the spatial operators
        double min = 0;
        double max = 5;

        return new SpatialOp<>(locSvc,
                distance(min, max),
                SpatialOpTest::escapeOp);
    }

    public static Function<SpatialModel<Double>, DistanceStructure<Double, ?>>
    distance(double lowerBound, double upperBound) {
        return g -> new DefaultDistanceStructure<>(x -> x,
                new DoubleDomain(),
                lowerBound,
                upperBound,
                g);
    }

    private static IntFunction<Double> somewhereOp(IntFunction<Double> s,
                                                   DistanceStructure<Double, ?> ds) {
        return new SpatialAlgorithms<>(ds, DOUBLES, false).somewhere(s);
    }

    private static IntFunction<Double> everywhereOp(IntFunction<Double> s,
                                                    DistanceStructure<Double, ?> ds) {
        return new SpatialAlgorithms<>(ds, DOUBLES, false).everywhere(s);
    }

    private static IntFunction<Double> somewhereOpParallel(IntFunction<Double> s,
                                                           DistanceStructure<Double, ?> ds) {
        return new SpatialAlgorithms<>(ds, DOUBLES, true).somewhere(s);
    }

    private static IntFunction<Double> everywhereOpParallel(IntFunction<Double> s,
                                                            DistanceStructure<Double, ?> ds) {
        return new SpatialAlgorithms<>(ds, DOUBLES, true).everywhere(s);
    }

    private static IntFunction<Double> escapeOp(IntFunction<Double> s,
                                                DistanceStructure<Double, ?> ds) {
        return new EscapeAlgorithm<>(ds, DOUBLES, s).compute();
    }

    @Test
    void testEmptyLocationService() {
        LocationService<Double, Double> locSvc = new LocationServiceList<>();

        assertThrows(UnsupportedOperationException.class,
                () -> somewhere(locSvc, SEQUENTIAL));
    }

    @Test
    void testSomewhereSingleToChainEquality() {
        SpatialModel<Double> m = basicGraph();
        LocationService<Double, Double> locSvc = new StaticLocationService<>(m);
        SpatialOp<Double, Double, Double> op = somewhere(locSvc,
                SEQUENTIAL);
        TimeChain<Double, List<Double>> ups = basicUpdates();

        TimeChain<Double, List<Double>> resultIO = op.computeUnaryChain(ups);
        TimeChain<Double, List<Double>> resultOO = processUpdates(ups, op);

        assertEquals(resultOO, resultIO);
    }

    @Test
    void testEverywhereSingleToChainEquality() {
        SpatialModel<Double> m = basicGraph();
        LocationService<Double, Double> locSvc = new StaticLocationService<>(m);
        SpatialOp<Double, Double, Double> op = everywhere(locSvc,
                SEQUENTIAL);
        TimeChain<Double, List<Double>> ups = basicUpdates();

        TimeChain<Double, List<Double>> resultIO = op.computeUnaryChain(ups);
        TimeChain<Double, List<Double>> resultOO = processUpdates(ups, op);

        assertEquals(resultOO, resultIO);
    }

    @Test
    void testSomewhereParallelSingleToChainEquality() {
        SpatialModel<Double> m = basicGraph();
        LocationService<Double, Double> locSvc = new StaticLocationService<>(m);
        SpatialOp<Double, Double, Double> op = somewhere(locSvc,
                PARALLEL);
        TimeChain<Double, List<Double>> ups = basicUpdates();

        TimeChain<Double, List<Double>> resultIO = op.computeUnaryChain(ups);
        TimeChain<Double, List<Double>> resultOO = processUpdates(ups, op);

        assertEquals(resultOO, resultIO);
    }

    @Test
    void testEverywhereParallelSingleToChainEquality() {
        SpatialModel<Double> m = basicGraph();
        LocationService<Double, Double> locSvc = new StaticLocationService<>(m);
        SpatialOp<Double, Double, Double> op = everywhere(locSvc,
                PARALLEL);
        TimeChain<Double, List<Double>> ups = basicUpdates();

        TimeChain<Double, List<Double>> resultIO = op.computeUnaryChain(ups);
        TimeChain<Double, List<Double>> resultOO = processUpdates(ups, op);

        assertEquals(resultOO, resultIO);
    }

    @Test
    void testEscapeSingleToChainEquality() {
        SpatialModel<Double> m = basicGraph();
        LocationService<Double, Double> locSvc = new StaticLocationService<>(m);
        SpatialOp<Double, Double, Double> op = escape(locSvc);
        TimeChain<Double, List<Double>> ups = basicUpdates();

        TimeChain<Double, List<Double>> resultIO = op.computeUnaryChain(ups);
        TimeChain<Double, List<Double>> resultOO = processUpdates(ups, op);

        assertEquals(resultOO, resultIO);
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
     * <p>
     * Each edge is bidirectional (i.e. an arc) and has the same value (i.e. 1)
     *
     * @return the depicted spatial model
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

    private TimeChain<Double, List<Double>> processUpdates(
            TimeChain<Double, List<Double>> updates,
            SpatialOp<Double, Double, Double> op) {
        List<Update<Double, List<Double>>> upsOO = updates.toUpdates();
        List<Update<Double, List<Double>>> result = new ArrayList<>();
        for (Update<Double, List<Double>> u : upsOO)
            result.addAll(op.computeUnary(u));

        return Update.asTimeChain(result);
    }

}