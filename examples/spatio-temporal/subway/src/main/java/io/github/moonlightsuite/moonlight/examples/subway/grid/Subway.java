package io.github.moonlightsuite.moonlight.examples.subway.grid;

import io.github.moonlightsuite.moonlight.core.base.Triple;
import io.github.moonlightsuite.moonlight.core.formula.Interval;
import io.github.moonlightsuite.moonlight.core.space.DefaultDistanceStructure;
import io.github.moonlightsuite.moonlight.core.space.DistanceStructure;
import io.github.moonlightsuite.moonlight.core.space.LocationService;
import io.github.moonlightsuite.moonlight.core.space.SpatialModel;
import io.github.moonlightsuite.moonlight.domain.BooleanDomain;
import io.github.moonlightsuite.moonlight.domain.DoubleDomain;
import io.github.moonlightsuite.moonlight.offline.monitoring.spatialtemporal.SpatialTemporalMonitor;
import io.github.moonlightsuite.moonlight.offline.signal.Signal;
import io.github.moonlightsuite.moonlight.offline.signal.SpatialTemporalSignal;
import io.github.moonlightsuite.moonlight.util.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * General idea of this scenario:
 * <p>
 * Consider we are in a subway  network, and we want to verify, at each station that:
 * - whenever a big number of people reach the station
 * - within a time bound, a train reaches the station and,
 * - within a small time after that, the number of people decreases.
 * We call this property "Peak Management".
 * <p>
 * TODO: externalize signals
 * <p>
 * In symbols, let P be the threshold for people, M be the time bound,
 * let D represent the distance threshold to express the spatial reachability,
 * and let O represent the departure offset, and T the time horizon of interest
 * We can say that, in a given Station i...
 * (G_[0,T] ((SW_D (People &gt;= P)) &and; (Station = i)))
 * U_[0,M]
 * ( (SW_D(Train &gt; 1)) &and; (Station = i)) &and; (F_[0,O] (People &lt; P) )
 *
 * @see #peakManagement for the implementation of the formula
 */
public class Subway {

    /**
     * We initialize the numeric constants of the problem
     */
    private static final int P = 100;       // max n. of people
    private static final double T = 100;    // time horizon
    private static final double TH = 10;    // time horizon
    private static final double M = 40;     // waiting time bound
    private static final double D = 40;     // distance threshold
    private static final double O = 40;     // departure offset

    /**
     * We initialize the domains and the spatial network
     *
     * @see Grid for a description of the spatial model.
     */
    private static final DoubleDomain ROBUSTNESS = new DoubleDomain();
    private static final BooleanDomain SATISFACTION = new BooleanDomain();
    /**
     * Signal Dimensions (i.e. signal domain)
     */
    private static final List<Integer> trainsAvailable = Arrays.asList(1, 0, 1, 2, 0, 0, 0, 0, 1);
    private static final List<Boolean> isStation = Arrays.asList(false, false, true, false, false,
            true, false, false, false);
    private static final List<Integer> peopleAtStations = Arrays.asList(3, 145, 67, 243, 22, 103, 6, 24, 54);
    private static final SpatialModel<Double> network = Grid.simulateModel();

    public static void main(String[] argv) {

        //// We initialize our 3-dimensional signal ////
        SpatialTemporalSignal<Triple<Integer, Boolean, Integer>> signal =
                createSTSignal(network.size(), 0, 1, T, Subway::sValues);


        //// We are considering a static Location Service ///
        LocationService<Double, Double> locService = Utils.createLocServiceStatic(0, 1, T, network);


        // Now we can monitor the system for the satisfaction of our Peak Management property
        SpatialTemporalMonitor<Double, Triple<Integer, Boolean, Integer>, Boolean> m = peakManagement();
        SpatialTemporalSignal<Boolean> output = m.monitor(locService, signal);
        List<Signal<Boolean>> signals = output.getSignals();

        System.out.print("The monitoring result is: ");
        System.out.println(signals.get(0).getValueAt(0.0));
    }

    // --------- FORMULAE --------- //

    /**
     * The usage peak is managed if, supposing it occurs within time T,
     * the service adapts in at most M time
     * <p>
     * In symbols: G_[0,T] (crowdedStation U_[0,M] properService)
     *
     * @return a GloballyMonitor for the property
     */
    private static SpatialTemporalMonitor<Double, Triple<Integer, Boolean, Integer>, Boolean> peakManagement() {
        return SpatialTemporalMonitor.globallyMonitor(   // Eventually...
                SpatialTemporalMonitor.untilMonitor(
                        crowdedStation(), new Interval(0, M), properService(), // a Until b...
                        SATISFACTION)
                , SATISFACTION, new Interval(0, TH));
    }

    /**
     * A station is crowded if too many people reach it in a give time frame.
     * <p>
     * In symbols: SW_D(tooManyPeople) ∧ rightStation
     *
     * @return an AndMonitor for the property
     */
    private static SpatialTemporalMonitor<Double, Triple<Integer, Boolean, Integer>, Boolean> crowdedStation() {
        return SpatialTemporalMonitor.andMonitor(
                SpatialTemporalMonitor.somewhereMonitor(tooManyPeople(), distance(0, D), SATISFACTION),
                SATISFACTION,
                rightStation());
    }

    /**
     * We can say that a peak of requests is properly serviced if a train is arriving and,
     * within O time, the number of people decreases.
     * <p>
     * In symbols: trainArrives ∧ peopleLeave
     *
     * @return an AndMonitor for the property
     */
    private static SpatialTemporalMonitor<Double, Triple<Integer, Boolean, Integer>, Boolean> properService() {
        return SpatialTemporalMonitor.andMonitor(trainArrives(), SATISFACTION, peopleLeave());
    }

    /**
     * We can say that a train is arriving if a train is somewhere nearby the Station, within distance D
     * <p>
     * In symbols: (SW_D(Train > 1)) ∧ (Station = i)
     *
     * @return an AndMonitor for the property
     */
    private static SpatialTemporalMonitor<Double, Triple<Integer, Boolean, Integer>, Boolean> trainArrives() {
        return SpatialTemporalMonitor.andMonitor(
                SpatialTemporalMonitor.somewhereMonitor(atLeastATrain(), distance(0, D), SATISFACTION),
                SATISFACTION, rightStation()
        );


    }

    /**
     * We can say that people are leaving a station is, within a given time frame,
     * the number of people goes under the threshold
     * <p>
     * In symbols: F_[0,O] !tooManyPeople
     *
     * @return an EventuallyMonitor for the property
     */
    private static SpatialTemporalMonitor<Double, Triple<Integer, Boolean, Integer>, Boolean> peopleLeave() {
        return SpatialTemporalMonitor.eventuallyMonitor(SpatialTemporalMonitor.notMonitor(tooManyPeople(), SATISFACTION) // not tooManyPeople...
                , SATISFACTION, new Interval(0, M + O));
    }

    // --------- ATOMIC PREDICATES --------- //

    /**
     * Atomic predicate describing the "crowdedness" of stations
     * <p>
     * In symbols: People >= P
     *
     * @return an AtomicMonitor for the property
     */
    private static SpatialTemporalMonitor<Double, Triple<Integer, Boolean, Integer>, Boolean> tooManyPeople() {
        return SpatialTemporalMonitor.atomicMonitor((x -> x.getThird().doubleValue() >= P));
    }

    /**
     * We can say that there is at least a train at a given position
     * if the number of trains is bigger than 0
     * <p>
     * In symbols: Train > 0
     *
     * @return an AtomicMonitor for the property
     */
    private static SpatialTemporalMonitor<Double, Triple<Integer, Boolean, Integer>, Boolean> atLeastATrain() {
        return SpatialTemporalMonitor.atomicMonitor((x -> x.getFirst() > 0));
    }


    /**
     * Atomic predicate describing the stations
     * TODO: distinguish between stations
     * <p>
     * In symbols: Station = i
     *
     * @return an AtomicMonitor for the property
     */
    private static SpatialTemporalMonitor<Double, Triple<Integer, Boolean, Integer>, Boolean> rightStation() {
        return SpatialTemporalMonitor.atomicMonitor(Triple::getSecond);
    }


    // --------- HELPERS --------- //

    /**
     * It calculates the proper distance, given a spatial model
     *
     * @param from double representing the starting position
     * @param to   double representing the ending position
     * @return a DoubleDistance object, meaningful in the given Spatial Model
     */
    private static Function<SpatialModel<Double>, DistanceStructure<Double, ?>> distance(double from, double to) {
        return g -> new DefaultDistanceStructure<>(x -> x, new DoubleDomain(), from, to, g);
    }

    /**
     * It returns the n-dim value of the ST signal, given a time instant and a location
     * WARNING: it currently returns the same value for each time instant, i.e. time-invariant signal
     *
     * @param t the time instant of interest
     * @param l the location of interest
     * @return a triplet corresponding to an element of the co-domain of the signal
     */
    private static Triple<Integer, Boolean, Integer> sValues(double t, int l) {
        return new Triple<>(trainsAvailable.get(l), isStation.get(l), peopleAtStations.get(l));
    }

    private static <T> SpatialTemporalSignal<T>
    createSTSignal(int size, double start, double dt, double end, BiFunction<Double, Integer, T> f) {
        SpatialTemporalSignal<T> s = new SpatialTemporalSignal<>(size);

        for (double t = start; t < end; t += dt) {
            double finalTime = t;
            s.add(t, i -> f.apply(finalTime, i));
        }

        s.add(end, i -> f.apply(end, i));
        return s;
    }


}



