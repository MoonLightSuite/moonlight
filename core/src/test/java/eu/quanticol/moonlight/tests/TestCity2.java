package eu.quanticol.moonlight.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import eu.quanticol.moonlight.formula.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.quanticol.moonlight.monitoring.SpatialTemporalMonitoring;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.monitoring.SpatialTemporalMonitoring;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import static eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor.*;
import static org.junit.jupiter.api.Assertions.*;

import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.TestUtils;
import eu.quanticol.moonlight.util.Triple;

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
    void testPropCity() {


        ///// Signals  /////
        List<String> places = Arrays.asList("BusStop", "Hospital", "MetroStop", "MainSquare", "BusStop", "Museum", "MetroStop");
        List<Boolean> taxiAvailability = Arrays.asList(false, false, true, false, false, true, false);
        List<Integer> peopleAtPlaces = Arrays.asList(3, 145, 67, 243, 22, 103, 6);
        SpatialTemporalSignal<Triple<String, Boolean, Integer>> signal = TestUtils.createSpatioTemporalSignal(SIZE, 0, 1, 20.0,
                (t, l) -> new Triple<>(places.get(l), taxiAvailability.get(l), peopleAtPlaces.get(l)));


        //// Loc Service Static ///
        LocationService<Double> locService = TestUtils.createLocServiceStatic(0, 1, 20.0,city);

        ///// Properties  //////
        HashMap<String, Function<Parameters, Function<Triple<String, Boolean, Integer>, Boolean>>> atomicFormulas = new HashMap<>();
        atomicFormulas.put("isThereATaxi", p -> (Triple::getSecond));
        atomicFormulas.put("isThereAStop", p -> (x -> "BusStop".equals(x.getFirst()) || "MetroStop".equals(x.getFirst())));
        atomicFormulas.put("isHospital", p -> (x -> "Hospital".equals(x.getFirst())));
        atomicFormulas.put("isMainSquare", p -> (x -> "MainSquare".equals(x.getFirst())));

        HashMap<String, Function<Parameters, Function<Triple<String, Boolean, Integer>, Double>>> atomicFormulasQuant = new HashMap<>();
        atomicFormulasQuant.put("FewPeople", p -> (x -> 0.5 - x.getThird()));
        atomicFormulasQuant.put("ManyPeople", p -> (x -> x.getThird() - 0.5));


        HashMap<String, Function<SpatialModel<Double>, DistanceStructure<Double, ?>>> distanceFunctions = new HashMap<>();
        DistanceStructure<Double, Double> predist = new DistanceStructure<>(x -> x , new DoubleDistance(), 0.0, 1.0, city);
        DistanceStructure<Double, Double> predist3 = new DistanceStructure<>(x -> x , new DoubleDistance(), 0.0, 3.0, city);
        DistanceStructure<Double, Double> predist6 = new DistanceStructure<>(x -> x , new DoubleDistance(), 0.0, 6.0, city);
        DistanceStructure<Double, Double> predist10 = new DistanceStructure<>(x -> x , new DoubleDistance(), 0.0, 10.0, city);
        DistanceStructure<Double, Double> predistX = new DistanceStructure<>(x -> x , new DoubleDistance(), 6.0, 32.0*32.0, city);

        distanceFunctions.put("distX", x -> predist);
        distanceFunctions.put("dist3", x -> predist3);
        distanceFunctions.put("dist6", x -> predist6);
        distanceFunctions.put("dist10", x -> predist10);
        distanceFunctions.put("distH", x -> predistX);



        Formula somewhereTaxi = new SomewhereFormula("distX", new AtomicFormula("isThereATaxi"));
        Formula stopReacMainsquare = new ReachFormula(
                new AtomicFormula("isThereAStop"),"ciccia", "dist10", new AtomicFormula("isMainSquare") );
        Formula taxiReachStop = new ReachFormula(
                new AtomicFormula("isThereATaxi"),"ciccia", "dist3", stopReacMainsquare );
        Formula iftaxiReachStop = new OrFormula(new NegationFormula(new AtomicFormula("isThereATaxi")), taxiReachStop);
        Formula evTaxi = new EventuallyFormula( new AtomicFormula("isThereATaxi"), new Interval(0,20));
        Formula escpCroud = new EscapeFormula("ciccia", "dist10", new AtomicFormula("isMainSquare"));

        Formula reachQuant = new ReachFormula(
                new AtomicFormula("FewPeople"),"ciccia", "distH", new AtomicFormula("ManyPeople") );

        //// MONITOR /////
        SpatialTemporalMonitoring<Double, Triple<String, Boolean, Integer>, Boolean> monitor =
                new SpatialTemporalMonitoring<>(
                        atomicFormulas,
                        distanceFunctions,
                        new BooleanDomain(),
                        true);

        //// MONITOR QUANT/////
        SpatialTemporalMonitoring<Double, Triple<String, Boolean, Integer>, Double> monitorQuant =
                new SpatialTemporalMonitoring<>(
                        atomicFormulasQuant,
                        distanceFunctions,
                        new DoubleDomain(),
                        true);

        ////  1 ////
        SpatialTemporalMonitor<Double,Triple<String, Boolean, Integer>,Boolean> m =
                monitor.monitor(new AtomicFormula("isThereATaxi"), null);
        SpatialTemporalSignal<Boolean> sout = m.monitor(locService, signal);
        List<Signal<Boolean>> signals = sout.getSignals();
        for (int i = 0; i < SIZE; i++) {
            assertEquals(taxiAvailability.get(i), signals.get(i).valueAt(1));
        }


        ////  2 ////
        SpatialTemporalMonitor<Double,Triple<String, Boolean, Integer>,Boolean> m2 =
                monitor.monitor(new AtomicFormula("isThereAStop"), null);
        SpatialTemporalSignal<Boolean> sout2 = m2.monitor(locService, signal);
        List<Signal<Boolean>> signals2 = sout2.getSignals();
        ArrayList<Boolean> soluz = new ArrayList<>(Arrays.asList(true, false, true, false, true, false, true));
        for (int i = 0; i < SIZE; i++) {
            assertEquals(soluz.get(i), signals2.get(i).valueAt(1));
        }


        ////  3 ////
        SpatialTemporalMonitor<Double,Triple<String, Boolean, Integer>,Boolean> m3 =
                monitor.monitor(somewhereTaxi, null);
        SpatialTemporalSignal<Boolean> sout3 = m3.monitor(locService, signal);
        List<Signal<Boolean>> signals3 = sout3.getSignals();
        for (int i = 0; i < SIZE; i++) {
            assertEquals(taxiAvailability.get(i), signals3.get(i).valueAt(1));
        }


        ////  4 ///
        SpatialTemporalMonitor<Double,Triple<String, Boolean, Integer>,Boolean> m4 =
                monitor.monitor(stopReacMainsquare, null);
        SpatialTemporalSignal<Boolean> sout4 = m4.monitor(locService, signal);
        List<Signal<Boolean>> signals4 = sout4.getSignals();

        assertEquals(true, signals4.get(3).valueAt(1));
        assertEquals(false, signals4.get(6).valueAt(1));


        ////  5 ////
        SpatialTemporalMonitor<Double,Triple<String, Boolean, Integer>,Boolean> m5 =
                monitor.monitor(taxiReachStop, null);
        SpatialTemporalSignal<Boolean> sout5 = m5.monitor(locService, signal);
        List<Signal<Boolean>> signals5 = sout5.getSignals();

        assertEquals(true, signals5.get(3).valueAt(1));
        assertEquals(false, signals5.get(6).valueAt(1));


        ////  6 ////
        SpatialTemporalMonitor<Double,Triple<String, Boolean, Integer>,Boolean> m6 =
                monitor.monitor(iftaxiReachStop, null);
        SpatialTemporalSignal<Boolean> sout6 = m6.monitor(locService, signal);
        List<Signal<Boolean>> signals6 = sout6.getSignals();


        assertEquals(true, signals6.get(0).valueAt(1));
        assertEquals(true, signals6.get(1).valueAt(1));
        assertEquals(true, signals6.get(2).valueAt(1));
        assertEquals(true, signals6.get(3).valueAt(1));
        assertEquals(true, signals6.get(4).valueAt(1));
        assertEquals(false, signals6.get(5).valueAt(1));
        assertEquals(true, signals6.get(6).valueAt(1));



       ////  7 ////
        SpatialTemporalMonitor<Double,Triple<String, Boolean, Integer>,Boolean> m7 =
            monitor.monitor(evTaxi, null);
        SpatialTemporalSignal<Boolean> sout7 = m7.monitor(locService, signal);
        List<Signal<Boolean>> signals7 = sout7.getSignals();
        for (int i = 0; i < SIZE; i++) {
            assertEquals(taxiAvailability.get(i), signals7.get(i).valueAt(0));
        }

        ////  8 Quant ////
        SpatialTemporalMonitor<Double,Triple<String, Boolean, Integer>,Double> m8 =
                monitorQuant.monitor(reachQuant, null);
        SpatialTemporalSignal<Double> sout8 = m8.monitor(locService, signal);
        List<Signal<Double>> signals8 = sout8.getSignals();
        assertEquals(-102.5, signals8.get(0).valueAt(1));




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
