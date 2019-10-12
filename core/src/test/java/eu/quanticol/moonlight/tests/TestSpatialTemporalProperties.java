package eu.quanticol.moonlight.tests;

import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.SpatioTemporalMonitoring;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author loreti
 *
 */
class TestSpatialTemporalProperties {

    @Test
    void testSPTGridBuild(){
        int size = 32;
        SpatialModel<Double> grid = TestUtils.createGridModel(size, size,false,1.0);

        HashMap<String, Function<SpatialModel<Double>, DistanceStructure<Double, ?>>> distanceFunctions = new HashMap<>();
        DistanceStructure<Double, Double> predist = new DistanceStructure<>(x -> x , new DoubleDistance(), 6.0, 32.0*32.0, grid);
        distanceFunctions.put("dist6", x -> predist);

        HashMap<String, Function<Parameters, Function<Double, Double>>> atomic = new HashMap<>();
        atomic.put("simpleAtomic", p -> (x -> (x - 2.0)));
        atomic.put("simpleAtomicl", p -> (x -> (0.5 -x)));
        atomic.put("simpleAtomich", p -> (x -> (x - 0.5)));

        Formula somewhere = new SomewhereFormula( "dist6", new AtomicFormula("simpleAtomicl"));
        Formula reach = new ReachFormula( new AtomicFormula("simpleAtomicl"),"ciccia", "dist6", new AtomicFormula("simpleAtomich"));
        Formula escape = new EscapeFormula("ciccia", "dist6", new AtomicFormula("simpleAtomicl"));

        SpatioTemporalSignal<Double> signal = TestUtils.createSpatioTemporalSignal(size*size, 0, 1, 5, (t, l) -> t * l);
        SpatioTemporalMonitoring<Double, Double, Double> monitor = new SpatioTemporalMonitoring<>(
                atomic,
                distanceFunctions,
                new DoubleDomain(),
                true);

        BiFunction<DoubleFunction<SpatialModel<Double>>, SpatioTemporalSignal<Double>, SpatioTemporalSignal<Double>> m = monitor.monitor(
                escape, null);
        SpatioTemporalSignal<Double> sout = m.apply(t -> grid, signal);
        ArrayList<Signal<Double>> signals = sout.getSignals();
        assertEquals(0.5, signals.get(0).valueAt(0.0), 0.0001);


        assertNotNull(grid);
    }

    @Test
    void testSPTsignalGraphBuild() {
        int size = 5;
        SpatialModel<Double> model = TestUtils.createSpatialModel(size, (x, y) -> (y == (((x + 1) % size)) ? 1.0 : null));

        HashMap<String, Function<SpatialModel<Double>, DistanceStructure<Double, ?>>> distanceFunctions = new HashMap<>();
        DistanceStructure<Double, Double> predist = new DistanceStructure<>(x -> x , new DoubleDistance(), 0.5, 3.0, model);
        distanceFunctions.put("dist6", x -> predist);

        HashMap<String, Function<Parameters, Function<Double, Double>>> atomic = new HashMap<>();
        atomic.put("simpleAtomic", p -> (x -> (x - 2.0)));
        atomic.put("simpleAtomicl", p -> (x -> (0.5 -x)));
        atomic.put("simpleAtomich", p -> (x -> (x - 0.5)));

        Formula somewhere = new SomewhereFormula( "dist6", new AtomicFormula("simpleAtomicl"));
        Formula reach = new ReachFormula( new AtomicFormula("simpleAtomicl"),"ciccia", "dist6", new AtomicFormula("simpleAtomich"));
        Formula escape = new EscapeFormula("ciccia", "dist6", new AtomicFormula("simpleAtomicl"));

        SpatioTemporalSignal<Double> signal = TestUtils.createSpatioTemporalSignal(size, 0, 1, 10, (t, l) -> t * l);
        SpatioTemporalMonitoring<Double, Double, Double> monitor = new SpatioTemporalMonitoring<>(
                atomic,
                distanceFunctions,
                new DoubleDomain(),
                true);

        BiFunction<DoubleFunction<SpatialModel<Double>>, SpatioTemporalSignal<Double>, SpatioTemporalSignal<Double>> m = monitor.monitor(
                new AtomicFormula("simpleAtomic"), null);
        SpatioTemporalSignal<Double> sout = m.apply(t -> model, signal);
        ArrayList<Signal<Double>> signals = sout.getSignals();
        for (int i = 0; i < size; i++) {
            assertEquals(i * 5.0 - 2, signals.get(i).valueAt(5.0), 0.0001);
        }

        BiFunction<DoubleFunction<SpatialModel<Double>>, SpatioTemporalSignal<Double>, SpatioTemporalSignal<Double>> m2 = monitor.monitor(
                somewhere, null);
        SpatioTemporalSignal<Double> sout2 = m2.apply(t -> model, signal);
        ArrayList<Signal<Double>> signals2 = sout2.getSignals();
        assertEquals(0.5, signals2.get(0).valueAt(5.0), 0.0001);

        assertNotNull(model);
    }

    @Test
    void testSPTsignalGraphBuild2() {
        int size = 10;
        HashMap<String, Function<Parameters, Function<Pair<Double, Double>, Double>>> atomic = new HashMap<>();
        atomic.put("simpleAtomic", p -> (x -> (x.getFirst() + x.getSecond() - 2)));
        SpatialModel<Double> model = TestUtils.createSpatialModel(size, (x, y) -> (y == (((x + 1) % size)) ? 1.0 : null));
        SpatioTemporalSignal<Pair<Double, Double>> signal = TestUtils.createSpatioTemporalSignal(size, 0, 0.1, 10, (t, l) -> new Pair<>(t * l / 2, t * l / 2));
        SpatioTemporalMonitoring<Double, Pair<Double, Double>, Double> monitor = new SpatioTemporalMonitoring<>(
                atomic,
                new HashMap<>(),
                new DoubleDomain(),
                true);

        BiFunction<DoubleFunction<SpatialModel<Double>>, SpatioTemporalSignal<Pair<Double, Double>>, SpatioTemporalSignal<Double>> m = monitor.monitor(new AtomicFormula("simpleAtomic"), null);
        SpatioTemporalSignal<Double> sout = m.apply(t -> model, signal);
        ArrayList<Signal<Double>> signals = sout.getSignals();
        for (int i = 0; i < 10; i++) {
            assertEquals(i * 5.0 - 2, signals.get(i).valueAt(5.0), 0.0001);
        }
        assertNotNull(model);
    }

    @Test
    void testGraphSPTsignalBuildWithMaps() {
        int size = 10;
        HashMap<Pair<Integer, Integer>, Double> map = new HashMap<>();
        map.put(new Pair<>(0, 2), 1.0);
        map.put(new Pair<>(0, 1), 1.0);
        map.put(new Pair<>(2, 3), 1.0);
        map.put(new Pair<>(1, 3), 5.0);
    }

}
