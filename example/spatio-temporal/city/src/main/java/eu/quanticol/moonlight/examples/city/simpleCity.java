package eu.quanticol.moonlight.examples.city;

import eu.quanticol.moonlight.formula.BooleanDomain;
import eu.quanticol.moonlight.formula.DoubleDistance;
import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.monitoring.spatiotemporal.SpatioTemporalMonitor;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.TestUtils;
import eu.quanticol.moonlight.util.Triple;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;


public class simpleCity {

    private static SpatialModel<Double> city = buildingCity();
    private static final double range = 40;
    private static final int SIZE = 5;
    private static final DoubleDomain doubleDomain = new DoubleDomain();
    private static final BooleanDomain booleanDomain = new BooleanDomain();

    public static void main(String[] argv) {

        //// SpatioTemporalSignal
        List<Integer> typeNode = Arrays.asList( 1, 3, 3, 3, 3);
        SpatioTemporalSignal<Integer> signal = createSpatioTemporalSignal(SIZE, 0, 1, 20.0,
                (t, l) -> Integer.valueOf(typeNode.get(l)));


        //// Loc Service Static ///
        LocationService<Double> locService = TestUtils.createLocServiceStatic(0, 1, 20.0,city);


        ////  P1 = "isThereATaxi" ////
        SpatioTemporalMonitor<Double,Integer,Boolean> m = someType(0, 1);

        SpatioTemporalSignal<Boolean> sout = m.monitor(locService, signal);
        List<Signal<Boolean>> signals = sout.getSignals();

        System.out.println(signals.get(0).valueAt(0));




    }

	private static SpatioTemporalMonitor<Double, Integer, Boolean>  someType(double from, double to) {
//      Formula taxiReachStop = new ReachFormula(
//              new AtomicFormula("isThereATaxi"),"no", "dist3", stopReacMainsquare );
		return SpatioTemporalMonitor.somewhereMonitor(typeNode(), distance(from,to),booleanDomain);
	}

	private static Function<SpatialModel<Double>, DistanceStructure<Double, ?>>  distance(double from, double to) {
		return g -> new DistanceStructure<>(x -> x, new DoubleDistance(), from, to, g);
	}

    private static SpatioTemporalMonitor<Double, Integer, Boolean> typeNode() {
        return SpatioTemporalMonitor.atomicMonitor(p -> p==1);
    }


	private static SpatialModel<Double> buildingCity() { //metto alla fine tutti i metodi privati di servizio.
        HashMap<Pair<Integer, Integer>, Double> cityMap = new HashMap<>();
        cityMap.put(new Pair<>(1, 3), 1.0);
        cityMap.put(new Pair<>(1, 5), 1.0);
        cityMap.put(new Pair<>(2, 3), 1.0);
        cityMap.put(new Pair<>(2, 4), 1.0);
        cityMap.put(new Pair<>(2, 5), 1.0);
        cityMap.put(new Pair<>(3, 1), 1.0);
        cityMap.put(new Pair<>(3, 2), 1.0);
        cityMap.put(new Pair<>(3, 4), 1.0);
        cityMap.put(new Pair<>(3, 5), 1.0);
        cityMap.put(new Pair<>(4, 2), 1.0);
        cityMap.put(new Pair<>(4, 3), 1.0);
        cityMap.put(new Pair<>(5, 1), 1.0);
        cityMap.put(new Pair<>(5, 2), 1.0);
        cityMap.put(new Pair<>(5, 3), 1.0);
        return TestUtils.createSpatialModel(SIZE, cityMap);
    }

    private static <T> SpatioTemporalSignal<T> createSpatioTemporalSignal(int size, double start, double dt, double end, BiFunction<Double, Integer, T> f) {
        SpatioTemporalSignal<T> s = new SpatioTemporalSignal(size);

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



