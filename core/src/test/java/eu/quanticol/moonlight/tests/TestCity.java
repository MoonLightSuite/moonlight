/**
 *
 */
package eu.quanticol.moonlight.tests;

import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.SpatioTemporalMonitoring;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;
import eu.quanticol.moonlight.util.Pair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestCity {
    private final int size = 7;
    private final double range = 40;


    SpatialModel<Double> buildingCity() {

        HashMap<Pair<Integer, Integer>, Double> map = new HashMap<>();

        map.put(new Pair<>(0, 1), 2.0);
        map.put(new Pair<>(0, 1), 2.0);
        map.put(new Pair<>(1, 0), 2.0);
        map.put(new Pair<>(0, 5), 2.0);
        map.put(new Pair<>(5, 0), 2.0);
        map.put(new Pair<>(1, 2), 9.0);
        map.put(new Pair<>(2, 1), 9.0);
        map.put(new Pair<>(2, 3), 3.0);
        map.put(new Pair<>(3, 2), 3.0);
        map.put(new Pair<>(3, 4), 6.0);
        map.put(new Pair<>(4, 3), 6.0);
        map.put(new Pair<>(4, 5), 7.0);
        map.put(new Pair<>(5, 4), 7.0);
        map.put(new Pair<>(6, 1), 4.0);
        map.put(new Pair<>(1, 6), 4.0);
        map.put(new Pair<>(6, 3), 15.0);
        map.put(new Pair<>(3, 6), 15.0);

        return TestUtils.createSpatialModel(this.size, map);
    }

    @Test
    void testDistanceInCity() {

        SpatialModel<Double> city = buildingCity();
        DistanceStructure<Double, Double> ds = new DistanceStructure<>(x -> x, new DoubleDistance(), 0.0, this.range, city);

        assertNotNull(city);
        assertEquals(2.0, ds.getDistance(0, 1), 0.0, "d(0,1)");
        assertEquals(11.0, ds.getDistance(0, 2), 0.0, "d(0,2)");

    }


    @Test
    void testAtomicPropCity() {

        SpatialModel<Double> city = buildingCity();
        double range = 40;
        DistanceStructure<Double, Double> ds = new DistanceStructure<>(x -> x, new DoubleDistance(), 0.0, range, city);

        ArrayList<String> place = new ArrayList<>(Arrays.asList("BusStop", "Hospital", "MetroStop", "MainSquare", "BusStop", "Museum", "MetroStop"));
        ArrayList<Boolean> taxi = new ArrayList<>(Arrays.asList(false, false, true, false, false, true, false));
        ArrayList<Integer> people = new ArrayList<>(Arrays.asList(3, 145, 67, 243, 22, 103, 6));
        SpatioTemporalSignal<Pair<String, Boolean>> signal = TestUtils.createSpatioTemporalSignal(
                this.size, 0, 0.1, 10, (t, l) -> new Pair<>(place.get(l), taxi.get(l)));

        assertEquals("Hospital", signal.valuesatT(0).get(1).getFirst());
        assertEquals(false, signal.valuesatT(0).get(0).getSecond());

        HashMap<String, Function<Parameters, Function<Pair<String, Boolean>, Boolean>>> atomic = new HashMap<>();
        atomic.put("isThereATaxi", p -> (Pair::getSecond));
        atomic.put("isThereAStop", p -> (x -> x.getFirst().equals("BusStop") || x.getFirst().equals("MetroStop")));
        atomic.put("isHopsital", p -> (x -> x.getFirst().equals("Hospital")));

        HashMap<String, Function<SpatialModel<Double>, DistanceStructure<Double, ? extends Object>>> distanceFunctions = new HashMap<>();
        DistanceStructure<Double, Double> predist = new DistanceStructure<>(x -> x + 10, new DoubleDistance(), 0.0, range, city);
        distanceFunctions.put("dist10", x -> predist);

        Formula somewhereTaxi = new SomewhereFormula("dist10", new AtomicFormula("isThereATaxi"));

        SpatioTemporalMonitoring<Double, Pair<String, Boolean>, Boolean> monitor = new SpatioTemporalMonitoring<>(
                atomic,
                new HashMap<>(),
                new BooleanDomain(),
                true);

        BiFunction<Function<Double, SpatialModel<Double>>, SpatioTemporalSignal<Pair<String, Boolean>>, SpatioTemporalSignal<Boolean>> m = monitor.monitor(
                new AtomicFormula("isThereATaxi"), null);
        SpatioTemporalSignal<Boolean> sout = m.apply(t -> city, signal);
        ArrayList<Signal<Boolean>> signals = sout.getSignals();
        for (int i = 0; i < this.size; i++) {
            assertEquals(taxi.get(i), signals.get(i).valueAt(9));
        }


        BiFunction<Function<Double, SpatialModel<Double>>, SpatioTemporalSignal<Pair<String, Boolean>>, SpatioTemporalSignal<Boolean>> m2 = monitor.monitor(
                new AtomicFormula("isThereAStop"), null);
        SpatioTemporalSignal<Boolean> sout2 = m2.apply(t -> city, signal);
        ArrayList<Signal<Boolean>> signals2 = sout2.getSignals();
        ArrayList<Boolean> soluz = new ArrayList<>(Arrays.asList(true, false, true, false, true, false, true));
        for (int i = 0; i < this.size; i++) {
            assertEquals(soluz.get(i), signals2.get(i).valueAt(1));
        }

        BiFunction<Function<Double, SpatialModel<Double>>, SpatioTemporalSignal<Pair<String, Boolean>>, SpatioTemporalSignal<Boolean>> m3 = monitor.monitor(
                somewhereTaxi, null);
        SpatioTemporalSignal<Boolean> sout3 = m.apply(t -> city, signal);
        ArrayList<Signal<Boolean>> signals3 = sout.getSignals();
        for (int i = 0; i < this.size; i++) {
            assertEquals(true, signals.get(i).valueAt(0));
        }
    }
}

