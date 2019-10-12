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
import eu.quanticol.moonlight.util.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestCity {
    private final int size = 7;
    private final double range = 40;


    SpatialModel<Double> buildingCity() {

        HashMap<Pair<Integer, Integer>, Double> cityMap = new HashMap<>();

        cityMap.put(new Pair<>(0, 1), 2.0);
//        cityMap.put(new Pair<>(0, 1), 2.0);
        cityMap.put(new Pair<>(1, 0), 2.0);
        cityMap.put(new Pair<>(0, 5), 2.0);
        cityMap.put(new Pair<>(5, 0), 2.0);
        cityMap.put(new Pair<>(1, 2), 9.0);
        cityMap.put(new Pair<>(2, 1), 9.0);
        cityMap.put(new Pair<>(2, 3), 3.0);
        cityMap.put(new Pair<>(3, 2), 3.0);
        cityMap.put(new Pair<>(3, 4), 6.0);
        cityMap.put(new Pair<>(4, 3), 6.0);
        cityMap.put(new Pair<>(4, 5), 7.0);
        cityMap.put(new Pair<>(5, 4), 7.0);
        cityMap.put(new Pair<>(6, 1), 4.0);
        cityMap.put(new Pair<>(1, 6), 4.0);
        cityMap.put(new Pair<>(6, 3), 15.0);
        cityMap.put(new Pair<>(3, 6), 15.0);

        return TestUtils.createSpatialModel(size, cityMap);
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
        double range = 40;
        SpatialModel<Double> city = buildingCity();
        List<String> places = Arrays.asList("BusStop", "Hospital", "MetroStop", "MainSquare", "BusStop", "Museum", "MetroStop");
        List<Boolean> taxiAvailability = Arrays.asList(false, false, true, false, false, true, false);
        List<Integer> peopleAtPlaces = Arrays.asList(3, 145, 67, 243, 22, 103, 6);
        DistanceStructure<Double, Double> ds = new DistanceStructure<>(x -> x, new DoubleDistance(), 0.0, range, city);

        SpatioTemporalSignal<Pair<String, Boolean>> signal = TestUtils.createSpatioTemporalSignal(
                size, 0, 0.1, 10, (t, l) -> new Pair<>(places.get(l), taxiAvailability.get(l)));

//        assertEquals("Hospital", signal.valuesatT(0).get(1).getFirst());
//        assertEquals(false, signal.valuesatT(0).get(0).getSecond());

        HashMap<String, Function<Parameters, Function<Pair<String, Boolean>, Boolean>>> atomicFormulas = new HashMap<>();
        atomicFormulas.put("isThereATaxi", p -> (Pair::getSecond));
        atomicFormulas.put("isThereAStop", p -> (x -> "BusStop".equals(x.getFirst()) || "MetroStop".equals(x.getFirst())));
        atomicFormulas.put("isHospital", p -> (x -> "Hospital".equals(x.getFirst())));

        HashMap<String, Function<SpatialModel<Double>, DistanceStructure<Double, ? >>> distanceFunctions = new HashMap<>();
        DistanceStructure<Double, Double> predist = new DistanceStructure<>(x -> x + 10, new DoubleDistance(), 0.0, range, city);
        distanceFunctions.put("dist10", x -> predist);


        SpatioTemporalMonitoring<Double, Pair<String, Boolean>, Boolean> monitor = new SpatioTemporalMonitoring<>(
                atomicFormulas,
                distanceFunctions,
                new BooleanDomain(),
                true);

        BiFunction<DoubleFunction<SpatialModel<Double>>, SpatioTemporalSignal<Pair<String, Boolean>>, SpatioTemporalSignal<Boolean>> m = monitor.monitor(
                new AtomicFormula("isThereATaxi"), null);
        SpatioTemporalSignal<Boolean> sout = m.apply(t -> city, signal);
        List<Signal<Boolean>> signals = sout.getSignals();
        for (int i = 0; i < this.size; i++) {
            assertEquals(taxiAvailability.get(i), signals.get(i).valueAt(9));
        }


        BiFunction<DoubleFunction<SpatialModel<Double>>, SpatioTemporalSignal<Pair<String, Boolean>>, SpatioTemporalSignal<Boolean>> m2 = monitor.monitor(
                new AtomicFormula("isThereAStop"), null);
        SpatioTemporalSignal<Boolean> sout2 = m2.apply(t -> city, signal);
        List<Signal<Boolean>> signals2 = sout2.getSignals();
        ArrayList<Boolean> soluz = new ArrayList<>(Arrays.asList(true, false, true, false, true, false, true));
        for (int i = 0; i < this.size; i++) {
            assertEquals(soluz.get(i), signals2.get(i).valueAt(1));
        }


        Formula somewhereTaxi = new SomewhereFormula("dist10", new AtomicFormula("isThereATaxi"));
        BiFunction<DoubleFunction<SpatialModel<Double>>, SpatioTemporalSignal<Pair<String, Boolean>>, SpatioTemporalSignal<Boolean>> m3 = monitor.monitor(
                somewhereTaxi, null);
        SpatioTemporalSignal<Boolean> sout3 = m3.apply(t -> city, signal);
        List<Signal<Boolean>> signals3 = sout3.getSignals();
        for (int i = 0; i < this.size; i++) {
            assertEquals(true, signals3.get(i).valueAt(0));
        }
    }
}

