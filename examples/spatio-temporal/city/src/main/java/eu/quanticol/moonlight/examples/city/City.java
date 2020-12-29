package eu.quanticol.moonlight.examples.city;

import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.TestUtils;
import eu.quanticol.moonlight.util.Triple;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;


public class City {

    private static SpatialModel<Double> city = buildingCity();
    private static final double range = 40;
    private static final int SIZE = 7;
    private static final DoubleDomain doubleDomain = new DoubleDomain();
    private static final BooleanDomain booleanDomain = new BooleanDomain();

    public static void main(String[] argv) {

        //// SpatioTemporalSignal
        List<String> places = Arrays.asList("BusStop", "Hospital", "MetroStop", "MainSquare", "BusStop", "Museum", "MetroStop");
        List<Boolean> taxiAvailability = Arrays.asList(false, false, true, false, false, true, false);
        List<Integer> peopleAtPlaces = Arrays.asList(3, 145, 67, 243, 22, 103, 6);
        SpatialTemporalSignal<Triple<String, Boolean, Integer>> signal = createSpatioTemporalSignal(SIZE, 0, 1, 20.0,
                (t, l) -> new Triple<>(places.get(l), taxiAvailability.get(l), peopleAtPlaces.get(l)));


        //// Loc Service Static ///
        LocationService<Double> locService = TestUtils.createLocServiceStatic(0, 1, 20.0,city);


        ////  P1 = "isThereATaxi" ////
        SpatialTemporalMonitor<Double,Triple<String, Boolean, Integer>,Boolean> m = isThereATaxi();

        SpatialTemporalSignal<Boolean> sout = m.monitor(locService, signal);
        List<Signal<Boolean>> signals = sout.getSignals();

        System.out.println(signals.get(0).valueAt(0));


        ////  P2 = evTaxi ////
        SpatialTemporalMonitor<Double,Triple<String, Boolean, Integer>,Boolean> m7 = eventuallyATaxi(0,20);
        SpatialTemporalSignal<Boolean> sout7 = m7.monitor(locService, signal);
        List<Signal<Boolean>> signals7 = sout7.getSignals();
        System.out.println(signals7.get(0).valueAt(0));
//
//        ////  6 ////
        SpatialTemporalMonitor<Double,Triple<String, Boolean, Integer>,Boolean> m6 = ifTaxiReachStop(0,10);
        SpatialTemporalSignal<Boolean> sout6 = m6.monitor(locService, signal);
        List<Signal<Boolean>> signals6 = sout6.getSignals();
        System.out.println(signals6.get(0).valueAt(0));


    }


    private static SpatialTemporalMonitor<Double, Triple<String, Boolean, Integer>, Boolean> ifTaxiReachStop(double from, double to) {
//      Formula iftaxiReachStop = new OrFormula(new NegationFormula(new AtomicFormula("isThereATaxi")), taxiReachStop);
		return SpatialTemporalMonitor.orMonitor(
				SpatialTemporalMonitor.notMonitor(isThereATaxi(), booleanDomain), booleanDomain,
				taxiReachStop(from,to)
				);
	}

	private static SpatialTemporalMonitor<Double, Triple<String, Boolean, Integer>, Boolean> taxiReachStop(double from, double to) {
//      Formula taxiReachStop = new ReachFormula(
//              new AtomicFormula("isThereATaxi"),"no", "dist3", stopReacMainsquare );
		return SpatialTemporalMonitor.reachMonitor(isThereATaxi(), distance(from,to), stopReachMainSquare(), booleanDomain);
	}

	private static Function<SpatialModel<Double>, DistanceStructure<Double, ?>>  distance(double from, double to) {
		return g -> new DistanceStructure<>(x -> x, new DoubleDistance(), from, to, g);
	}

	private static SpatialTemporalMonitor<Double, Triple<String, Boolean, Integer>, Boolean> stopReachMainSquare() {
//      Formula somewhereTaxi = new SomewhereFormula("distX", new AtomicFormula("isThereATaxi"));
//      Formula stopReacMainsquare = new ReachFormula(
//              new AtomicFormula("isThereAStop"),"no", "dist10", new AtomicFormula("isMainSquare") );
		return SpatialTemporalMonitor.reachMonitor(isThereAStop(),distance(0, 10),isMainSquare(),booleanDomain);
	}

	private static SpatialTemporalMonitor<Double, Triple<String, Boolean, Integer>, Boolean> eventuallyATaxi(double a,
                                                                                                             double b) {
//      Formula evTaxi = new EventuallyFormula( new AtomicFormula("isThereATaxi"), new Interval(0,20));
    	return SpatialTemporalMonitor.eventuallyMonitor(isThereATaxi(), new Interval(a,b),booleanDomain);
	}

    private static SpatialTemporalMonitor<Double, Triple<String, Boolean, Integer>, Boolean> isThereATaxi() {
        return SpatialTemporalMonitor.atomicMonitor(p -> p.getSecond());
    }

	private static SpatialTemporalMonitor<Double, Triple<String, Boolean, Integer>, Boolean> isThereAStop() {
		return SpatialTemporalMonitor.atomicMonitor((x -> "BusStop".equals(x.getFirst()) || "MetroStop".equals(x.getFirst())));
	}

	private static SpatialTemporalMonitor<Double, Triple<String, Boolean, Integer>, Boolean> isMainSquare() {
		return SpatialTemporalMonitor.atomicMonitor((x -> "MainSquare".equals(x.getFirst())));
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

    private static <T> SpatialTemporalSignal<T> createSpatioTemporalSignal(int size, double start, double dt, double end, BiFunction<Double, Integer, T> f) {
        SpatialTemporalSignal<T> s = new SpatialTemporalSignal(size);

        for(double time = start; time < end; time += dt) {
            double finalTime = time;
            s.add(time, (i) -> {
                return f.apply(finalTime, i);
            });
        }

        s.add(end, (i) -> {
            return f.apply(end, i);
        });
        return s;
    }


}



