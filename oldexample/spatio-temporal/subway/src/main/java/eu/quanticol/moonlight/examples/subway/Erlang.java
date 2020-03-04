package eu.quanticol.moonlight.examples.subway;

import eu.quanticol.moonlight.examples.subway.Parsing.FileType;
import eu.quanticol.moonlight.examples.subway.Parsing.TrajectoryExtractor;
import eu.quanticol.moonlight.formula.BooleanDomain;
import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.monitoring.spatiotemporal.SpatioTemporalMonitor;
import eu.quanticol.moonlight.signal.*;
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
    private static final double P = 5;       // max n. of conections
    private static final double TH = 10;    // properties time horizon

    /**
     * We initialize the domains and the spatial network
     * @see SubwayNetwork for a description of the spatial model.
     */
    private static final DoubleDomain ROBUSTNESS = new DoubleDomain();
    private static final BooleanDomain SATISFACTION = new BooleanDomain();
    private static SpatialModel<Double> network = new SubwayNetwork().getModel(networkSource);

    /**
     * Signal and related Dimensions (i.e. signal domain)
     */
    private static final TrajectoryExtractor s = new TrajectoryExtractor(network.size());
    private static final double[][] data = new DataReader<>(trajectorySource, FileType.CSV, s).read();
    private static final int timeSamples = data[0].length;
    private static final SpatioTemporalSignal<Double> signal = createSTSignal(network.size(), timeSamples, Erlang::getSValue);


    public static void main(String[] argv) {

        //// We are considering a static Location Service ///
        LocationService<Double> locService = TestUtils.createLocServiceStatic(0, 1, timeSamples, network);


        // Now we can monitor the system for the satisfaction of our Peak Management property
        SpatioTemporalMonitor<Double, Double, Boolean> m = neverAboveThreshold();
        SpatioTemporalSignal<Boolean> output = m.monitor(locService, signal);
        List<Signal<Boolean>> signals = output.getSignals();

        System.out.print("The monitoring result is: ");
        System.out.println(signals.get(0).valueAt(0));
    }

    // --------- FORMULAE --------- //

    /**
     *  The usage peak is managed if, supposing it occurs within time T,
     *  the service adapts in at most M time
     *
     *  In symbols: G_[0,T] (crowdedStation U_[0,M] properService)
     *
     * @return an GloballyMonitor for the property
     */
    private static SpatioTemporalMonitor<Double, Double, Boolean> neverAboveThreshold() {
        return SpatioTemporalMonitor.globallyMonitor(   // Globally...
                tooManyPeople(), new Interval(0,TH), SATISFACTION);
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
        return SpatioTemporalMonitor.atomicMonitor((x -> x >= P));
    }

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

}