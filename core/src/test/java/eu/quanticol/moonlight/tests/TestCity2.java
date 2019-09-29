package eu.quanticol.moonlight.tests;

import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.SpatioTemporalMonitoring;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;
import eu.quanticol.moonlight.util.Pair;
import org.junit.jupiter.api.BeforeAll;
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

class TestCity2 {

    private static SpatialModel<Double> city;
    private static final double range = 40;
    private static final int SIZE = 7;


    @BeforeAll
    static void setUp() { //questo metodo viene eseguito una volta quando viene caricata la classe, visto che la città è la stessa per tutti i test è il posto giusto dove definirla
        city = buildingCity();
    }

    @Test
    void testDistanceInCity() {
        DistanceStructure<Double, Double> ds = new DistanceStructure<>(x -> x, new DoubleDistance(), 0.0, range, city);

        assertNotNull(city);
        assertEquals(2.0, ds.getDistance(0, 1), 0.0, "d(0,1)");
        assertEquals(11.0, ds.getDistance(0, 2), 0.0, "d(0,2)");

    }


    @Test
    void testAtomicPropCity() {
        List<String> places = Arrays.asList("BusStop", "Hospital", "MetroStop", "MainSquare", "BusStop", "Museum", "MetroStop");
        List<Boolean> taxiAvailability = Arrays.asList(false, false, true, false, false, true, false);
//        List<Integer> peopleAtPlaces =Arrays.asList(3, 145, 67, 243, 22, 103, 6);
        SpatioTemporalSignal<Pair<String, Boolean>> signal = TestUtils.createSpatioTemporalSignal(
                SIZE, 0, 0.1, 10, (t, l) -> new Pair<>(places.get(l), taxiAvailability.get(l)));

        HashMap<String, Function<Parameters, Function<Pair<String, Boolean>, Boolean>>> atomicFormulas = new HashMap<>();
        atomicFormulas.put("isThereATaxi", p -> (Pair::getSecond));
        atomicFormulas.put("isThereAStop", p -> (x -> "BusStop".equals(x.getFirst()) || "MetroStop".equals(x.getFirst())));
        atomicFormulas.put("isHospital", p -> (x -> "Hospital".equals(x.getFirst())));

        HashMap<String, Function<SpatialModel<Double>, DistanceStructure<Double, ?>>> distanceFunctions = new HashMap<>();
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
        ArrayList<Signal<Boolean>> signals = sout.getSignals();
        for (int i = 0; i < SIZE; i++) {
            assertEquals(taxiAvailability.get(i), signals.get(i).valueAt(9));
        }


        BiFunction<DoubleFunction<SpatialModel<Double>>, SpatioTemporalSignal<Pair<String, Boolean>>, SpatioTemporalSignal<Boolean>> m2 = monitor.monitor(
                new AtomicFormula("isThereAStop"), null);
        SpatioTemporalSignal<Boolean> sout2 = m2.apply(t -> city, signal);
        ArrayList<Signal<Boolean>> signals2 = sout2.getSignals();
        ArrayList<Boolean> soluz = new ArrayList<>(Arrays.asList(true, false, true, false, true, false, true));
        for (int i = 0; i < SIZE; i++) {
            assertEquals(soluz.get(i), signals2.get(i).valueAt(1));
        }


        Formula somewhereTaxi = new SomewhereFormula("dist10", new AtomicFormula("isThereATaxi"));
        BiFunction<DoubleFunction<SpatialModel<Double>>, SpatioTemporalSignal<Pair<String, Boolean>>, SpatioTemporalSignal<Boolean>> m3 = monitor.monitor(
                somewhereTaxi, null);
        SpatioTemporalSignal<Boolean> sout3 = m3.apply(t -> city, signal);
        ArrayList<Signal<Boolean>> signals3 = sout3.getSignals();
        for (int i = 0; i < SIZE; i++) {
            assertEquals(true, signals3.get(i).valueAt(0));
        }
    }


    private static SpatialModel<Double> buildingCity() { //metto alla fine tutti i metodi privati di servizio.
        HashMap<Pair<Integer, Integer>, Double> cityMap = new HashMap<>();
        cityMap.put(new Pair<>(0, 1), 2.0);
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
        return TestUtils.createSpatialModel(SIZE, cityMap);
    }

}
