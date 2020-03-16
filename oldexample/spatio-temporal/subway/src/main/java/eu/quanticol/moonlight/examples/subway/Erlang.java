package eu.quanticol.moonlight.examples.subway;

import eu.quanticol.moonlight.examples.subway.Parsing.FileType;
import eu.quanticol.moonlight.examples.subway.Parsing.TrajectoryExtractor;
import eu.quanticol.moonlight.formula.BooleanDomain;
import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.formula.Interval;
import static eu.quanticol.moonlight.monitoring.spatiotemporal.SpatioTemporalMonitor.*;
import eu.quanticol.moonlight.monitoring.spatiotemporal.SpatioTemporalMonitor;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.TestUtils;

import java.util.List;
import java.util.function.BiFunction;

public class Erlang {

    /**
     * Source files location
     */
    private static final String trajectorySource = Erlang.class.getResource("trajectory.csv").getPath();
    private static final String networkSource = Erlang.class.getResource("adj_matrix.txt").getPath();

    /**
     * Numeric constants of the problem
     */
    private static final int LH = 1;        // location horizon (neighbourhood)
    private static final double C = 5;      // crowdness threshold
    private static final double TH = 10;    // properties time horizon
    private static final double RT = 10;    // output signal reaction time

    /**
     * We initialize the domains and the spatial network
     * @see SubwayNetwork for a description of the spatial model.
     */
    private static final DoubleDomain ROBUSTNESS = new DoubleDomain();
    private static final BooleanDomain SATISFACTION = new BooleanDomain();
    private static SpatialModel<Double> network = new SubwayNetwork().getModel(networkSource);

    /**
     * Signal Dimensions (i.e. signal domain)
     */
    private static final TrajectoryExtractor s = new TrajectoryExtractor(network.size());
    private static final double[][] data = new DataReader<>(trajectorySource, FileType.CSV, s).read();
    private static final int timeSamples = data[0].length;

    /**
     * State Signals
     */
    private static final SpatioTemporalSignal<Double> crowdedness = createSTSignal(network.size(), timeSamples, Erlang::getSValue);
    private static final SpatioTemporalSignal<Pair<Double, Double>> routerPosition = createSTSignal(network.size(), timeSamples, Erlang::getRouterPosition);

    /**
     * Output Signals
     */
    private static final SpatioTemporalSignal<Double> outputCrowdedness = createSTSignal();
    private static final SpatioTemporalSignal<Pair<Double, Double>> outputRouter = createSTSignal();

    /**
     * Input Signals
     */
    private static final SpatioTemporalSignal<Double> deviceDirection = createSTSignal();
    private static final SpatioTemporalSignal<Pair<Double, Double>> devicePosition = createSTSignal();



    public static void main(String[] argv) {

        //// We are considering a static Location Service ///
        var locService = TestUtils.createLocServiceStatic(0, 1, timeSamples, network);


        // Now we can monitor the system for the satisfaction of our Peak Management property
        var m = neighbourSafety();
        var output = m.monitor(locService, crowdedness);
        var signals = output.getSignals();

        System.out.print("The monitoring result is: ");
        System.out.println(signals.get(0).valueAt(0));
    }

    // --------- FORMULAE --------- //

    /**
     *  Safety property (spatio-temporal)
     *
     *  In symbols: G_[0,TH] E_[0, LH] correctResponse
     *
     * @return a GloballyMonitor for the property
     */
    private static SpatioTemporalMonitor<Double, Double, Boolean> neighbourSafety() {
        return globallyMonitor(   // Globally in TH...
                everywhereMonitor( // Everywhere within LH...
                    correctResponse()
                    , distance(0, LH), SATISFACTION)
                , new Interval(0,TH), SATISFACTION);
    }

    private static SpatioTemporalMonitor<Double, Double, Boolean> correctResponse() {
        return sinceMonitor(outputRouter(),
                new Interval(0, RT),
                eligibleRouter(),
                SATISFACTION);
    }

    private static SpatioTemporalMonitor<Double, Double, Boolean> outputRouter() {
        return andMonitor(currentRouter(), crowdness(), SATISFACTION);
    }

    private static SpatioTemporalMonitor<Double, Double, Boolean> eligibleRouter() {
        return andMonitor(lowCrowdness(), closeToDevice(), SATISFACTION);
    }


    // --------- ATOMIC PREDICATES --------- //

    /**
     * Atomic predicate describing the "crowdedness" of stations
     *
     * In symbols: People >= P
     *
     * @return an AtomicMonitor for the property
     */
    private static SpatioTemporalMonitor<Double, Double, Boolean> tooManyPeople() {
        return atomicMonitor((x -> x >= P));
    }

    // ------------- HELPERS ------------- //

    private static <T> SpatioTemporalSignal<T>
    createSTSignal(int size, int end, BiFunction<Integer, Integer, T> f) {
        SpatioTemporalSignal<T> s = new SpatioTemporalSignal(size);

        for(int t = 0; t < end; t ++) {
            int time = t;
            s.add(t, (i) -> f.apply(time, i));
        }

        return s;
    }

    private static Double getSValue(int t, int l) {
        return data[l][t];
    }

    private static Pair<Double, Double> getRouterPosition(int t, int l) {
        double x = l % network.size();
        double y = l / network.size();
        return new Pair<>(x, y);
    }

}