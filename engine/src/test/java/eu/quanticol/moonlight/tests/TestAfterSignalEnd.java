package eu.quanticol.moonlight.tests;

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import static eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor.*;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.domain.BooleanDomain;
import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.core.space.DefaultDistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.Utils;
import eu.quanticol.moonlight.util.Triple;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestAfterSignalEnd {


    /**
     * We initialize the numeric constants of the problem
     */
    private static final int P = 100;       // max n. of people
    private static final double T = 100;   // time horizon
    private static final double M = 40;     // waiting time bound
    private static final double O = 70;     // departure offset

    private static final BooleanDomain SATISFACTION = new BooleanDomain();
    private static final SpatialModel<Double> network = getModel();

    /**
     * Signal Dimensions (i.e. signal domain)
     */
    private static final List<Integer> trainsAvailable = Arrays.asList(1, 0, 1, 2, 0, 0, 0);
    private static final List<Boolean> isStation = Arrays.asList(false, false, true, false, false, true, false);
    private static final List<Integer> peopleAtStations = Arrays.asList(3, 145, 67, 243, 22, 103, 6);


    private static final int SIZE = 7;


    @Test
    void untilExceeding() {
                SpatialTemporalSignal<Boolean> result = init(until());
                List<Signal<Boolean>> signals = result.getSignals();
                Signal<Boolean> onesignal = signals.get(0);
                assertTrue(onesignal.isEmpty());
    }


    @Test
    void eventuallyExceeding() {
        SpatialTemporalSignal<Boolean> result = init(peopleLeave());
        List<Signal<Boolean>> signals = result.getSignals();
        Signal<Boolean> onesignal = signals.get(0);
        assertTrue(onesignal.isEmpty());

    }

    @Test
    void globallyExceeding() {
        SpatialTemporalSignal<Boolean> result = init(peopleLeave2());
        List<Signal<Boolean>> signals = result.getSignals();
        Signal<Boolean> onesignal = signals.get(0);
        assertTrue(onesignal.isEmpty());
    }


    // --------- FORMULAE --------- //

    /**
     *  In symbols: F_[0,T] (tooManyPeople U_[0,M] rightStation)
     */
    private static SpatialTemporalMonitor<Double, Triple<Integer, Boolean, Integer>, Boolean> peakManagement() {
        return SpatialTemporalMonitor.eventuallyMonitor(   // Eventually...
                until()
                , new Interval(0,T), SATISFACTION);
    }

    /**
     *  In symbols:(tooManyPeople U_[0,M] rightStation)
     */
    private static SpatialTemporalMonitor<Double, Triple<Integer, Boolean, Integer>, Boolean> until() {
        return untilMonitor(
                tooManyPeople(), new Interval(0,M+O), rightStation(), // a Until b...
                        SATISFACTION);
    }


    /**
     * In symbols: F_[0,M+O] !tooManyPeople
     */
    private static SpatialTemporalMonitor<Double, Triple<Integer, Boolean, Integer>, Boolean> peopleLeave() {
        return eventuallyMonitor(   // Eventually...
                notMonitor(tooManyPeople(), SATISFACTION) // not tooManyPeople...
                , new Interval(0, M + O), SATISFACTION);
    }

    /**
     * In symbols: G_[0,M+O] !tooManyPeople
     */
    private static SpatialTemporalMonitor<Double, Triple<Integer, Boolean, Integer>, Boolean> peopleLeave2() {
        return globallyMonitor(   // Eventually...
                notMonitor(tooManyPeople(), SATISFACTION) // not tooManyPeople...
                , new Interval(0, M + O), SATISFACTION);
    }

    // --------- ATOMIC PREDICATES --------- //

    /**
     * In symbols: People >= P
     *
     * @return an AtmoicMonitor for the property
     */
    private static SpatialTemporalMonitor<Double, Triple<Integer, Boolean, Integer>, Boolean> tooManyPeople() {
        return atomicMonitor((x -> x.getThird().doubleValue() >= P));
    }

    /**
     * In symbols: Train > 0
     *
     * @return an AtomicMonitor for the property
     */
    private static SpatialTemporalMonitor<Double, Triple<Integer, Boolean, Integer>, Boolean> atLeastATrain() {
        return atomicMonitor((x -> x.getFirst() > 0));
    }


    /**
     * In symbols: Station = i
     *
     * @return an AtmoicMonitor for the property
     */
    private static SpatialTemporalMonitor<Double, Triple<Integer, Boolean, Integer>, Boolean> rightStation() {
        return atomicMonitor(Triple::getSecond);
    }

    // --------- HELPERS --------- //

    private SpatialTemporalSignal<Boolean> init(SpatialTemporalMonitor<Double, Triple<Integer, Boolean, Integer>, Boolean> f) {

        //// We initialize our 3-dimensional signal ////
        SpatialTemporalSignal<Triple<Integer, Boolean, Integer>> signal =
                createSTSignal(SIZE, 0, 1, T, TestAfterSignalEnd::sCoords);


        //// We are considering a static Location Service ///
        LocationService<Double, Double> locService = Utils.createLocServiceStatic(0, 1, T, network);


        // Now we can monitor the system for the satisfaction of our Peak Management property
        return f.monitor(locService, signal);
    }

    /**
     * It calculates the proper distance, given a spatial model
     *
     * @param from double representing the starting position
     * @param to double representing the ending position
     * @return a DoubleDistance object, meaningful in the given Spatial Model
     */
    private static Function<SpatialModel<Double>, DistanceStructure<Double, ?>>  distance(double from, double to) {
        return g -> new DefaultDistanceStructure<>(x -> x, new DoubleDomain(), from, to, g);
    }

    /**
     * It returns the n-dim value of the ST signal, given a time instant and a location
     * WARNING: it currently returns the same value for each time instant, i.e. time-constant signal
     * @param t the time instant of interest
     * @param l the location of interest
     * @return a triplet corresponding to an element of the co-domain of the signal
     */
    private static Triple<Integer,Boolean,Integer> sCoords(double t, int l) {
        return new Triple<>(trainsAvailable.get(l), isStation.get(l), peopleAtStations.get(l));
    }

    private static <T> SpatialTemporalSignal<T>
    createSTSignal(int size, double start, double dt, double end, BiFunction<Double, Integer, T> f) {
        SpatialTemporalSignal<T> s = new SpatialTemporalSignal(size);

        for(double time = start; time < end; time += dt) {
            double finalTime = time;
            s.add(time, (i) -> f.apply(finalTime, i));
        }

        s.add(end, (i) ->  f.apply(end, i));
        return s;
    }

    public static SpatialModel<Double>  getModel() {
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

        return Utils.createSpatialModel(SIZE, cityMap);
    }
}
