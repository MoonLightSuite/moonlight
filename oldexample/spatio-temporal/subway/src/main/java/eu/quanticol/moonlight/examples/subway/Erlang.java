package eu.quanticol.moonlight.examples.subway;

import eu.quanticol.moonlight.examples.subway.io.DataWriter;
import eu.quanticol.moonlight.examples.subway.parsing.PrintingStrategy;
import eu.quanticol.moonlight.examples.subway.parsing.RawTrajectoryExtractor;
import eu.quanticol.moonlight.statistics.SignalStatistics;
import eu.quanticol.moonlight.util.MultiValuedTrace;
import eu.quanticol.moonlight.examples.subway.grid.Grid;
import eu.quanticol.moonlight.examples.subway.grid.GridDirection;
import eu.quanticol.moonlight.examples.subway.io.DataReader;
import eu.quanticol.moonlight.examples.subway.io.FileType;
import eu.quanticol.moonlight.examples.subway.parsing.MultiRawTrajectoryExtractor;
import eu.quanticol.moonlight.statistics.StatisticalModelChecker;
import eu.quanticol.moonlight.domain.BooleanDomain;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.Interval;
import static eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor.*;
import static eu.quanticol.moonlight.examples.subway.ErlangSignal.*;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * General idea of this scenario:
 *
 * Consider we are in a city and there is a grid-like network of phone cells.
 * We want to make sure that the Assisted Routing feature of these cells
 * satisfies the specification.
 * More precisely:
 *  - at some points in time a device makes a request at a specific cell,
 *      indicating the direction towards which is moving.
 *  - the cells provide one (or more?) identifier of the next cell where the
 *      device should connect.
 *  - the next cell should not be too crowded and should be located in a
 *      direction consistent with the one of the device.
 *
 * We want to enforce:
 *  - a safety condition that says that requests are handled only by suitable
 *      cells.
 *
 *  - a liveness condition that says that eventually a request will happen and
 *      that, if this is the case, within a time boundary a reply is received.
 *
 * For the symbolic formulation, look at {@link #neighbourSafety()} and
 * {@link #communicationLiveness()}.
 */
public class Erlang {

    /**
     * Source files location
     */
    private static final String DATA_DIR = "trajectories/";
    private static final String NETWORK_FILE = "adjacent_matrix_milan_grid_35x35.txt";
    private static final String TRAJECTORY_FILE_PART = "_trajectory_grid_35x35_T_144.csv";
    private static final String RESULT = "_k100_smc_milan_grid_35x35_T_133.csv";
    //private static final String TRAJECTORY_SOURCE = Erlang.class.getResource("trajectories_100.csv").getPath();
    //private static final String NETWORK_SOURCE = Erlang.class.getResource("adj_matrix.txt").getPath();
    //private static final String TRAJECTORY_SOURCE = Erlang.class.getResource("100_trajectory_grid_25x25_T_336.csv").getPath();
    private static final String TRAJECTORY_SOURCE = Erlang.class.getResource("001_trajectory_grid_35x35_T_144.csv").getPath();
    //private static final String NETWORK_SOURCE = Erlang.class.getResource("adjacent_matrix_milan_grid_25x25.txt").getPath();
    private static final String NETWORK_SOURCE = Erlang.class.getResource("adjacent_matrix_milan_grid_35x35.txt").getPath();

    /**
     * Numeric constants of the problem
     */
    private static final int LH = 1;       // location horizon (neighbourhood)
    private static final double K = 100;     // crowdedness threshold
    private static final double TH = 10;   // properties time horizon
    private static final double T2 = 7;    // properties time horizon
    private static final double T3 = 1;    // output signal reaction time

    /**
     * We initialize the domains and the spatial network
     * @see Grid for a description of the spatial model.
     */
    private static final DoubleDomain ROBUSTNESS = new DoubleDomain();
    private static final BooleanDomain SATISFACTION = new BooleanDomain();
    private static ImmutableGraphModel<Double> network = (ImmutableGraphModel<Double>) new Grid().getModel(NETWORK_SOURCE);

    /**
     * Signal Dimensions (i.e. signal domain)
     */
    //private static final RawTrajectoryExtractor singleTraj = new RawTrajectoryExtractor(network.size());
    private static final ErlangSignal processor = new ErlangSignal();
    private static final MultiRawTrajectoryExtractor multiTraj = new MultiRawTrajectoryExtractor(network.size(), processor);
    private static final Collection<MultiValuedTrace> data =
            new DataReader<>(TRAJECTORY_SOURCE, FileType.CSV, multiTraj).read();


    public static void main(String[] argv) {
        System.out.println("The network size is: " + network.size());

        MultiValuedTrace signal = data.iterator().next();

        Pair<List<Integer>, List<GridDirection>> device = processor.getSampleDevice();

        //// We are considering a dynamic Location Service ///
        LocationService<Double> locService = createOrientedLocSvc(device.getFirst(), device.getSecond());

        Collection<MultiValuedTrace> trajectories = loadTrajectories();
        smc(phi1(), "p1", trajectories, locService);
        smc(phi2(), "p2", trajectories, locService);
        smc(phi3(), "p3", trajectories, locService);

        System.out.println("Saving output in :" + RESULT);

        //System.out.println("SMC Average Satisfiability: " + smc.getStats().toString());
    }

    private static void smc(
            SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean> p,
            String id,
            Collection<MultiValuedTrace> trajectories,
            LocationService<Double> locService)
    {
        //Statistical Model Checking
        StatisticalModelChecker<Double, List<Comparable<?>>, Boolean> smc =
                new StatisticalModelChecker<>(p, trajectories, locService);
        smc.compute();
        double[][] avg = filterAverage(smc.getStats());
        double[][] var = filterVariance(smc.getStats());

        PrintingStrategy<double[][]> str = new RawTrajectoryExtractor(network.size());
        new DataWriter<>(outputFile(RESULT, id, "avg"), FileType.CSV, str).write(avg);
        new DataWriter<>(outputFile(RESULT, id, "var"), FileType.CSV, str).write(var);
    }

    private static Collection<MultiValuedTrace> loadTrajectories() {
        Collection<MultiValuedTrace> trajectories = new ArrayList<>();
        for(int i = 1; i <= 100; i++) {
            String t = "100";
            if(i < 10)
                t = "00".concat(String.valueOf(i));
            else if(i < 100)
                t = "0".concat(String.valueOf(i));
            trajectories.addAll(loadTrajectory(t));
            System.out.println("Trajectory " + i + " loaded successfully!");
        }
        return trajectories;
    }

    private static Collection<MultiValuedTrace> loadTrajectory(String i) {
        MultiRawTrajectoryExtractor trajectory =
                    new MultiRawTrajectoryExtractor(network.size(), processor);
        String path = Erlang.class
                     .getResource(DATA_DIR + i + TRAJECTORY_FILE_PART)
                     .getPath();
        return new DataReader<>(path, FileType.CSV, trajectory).read();
    }

    /*
    public static void oldmain(String[] argv) {
        System.out.println("The network size is: " + network.size());

        MultiValuedTrace signal = data.iterator().next();

        Pair<List<Integer>, List<GridDirection>> device = processor.getSampleDevice();

        //// We are considering a dynamic Location Service ///
        LocationService<Double> locService = createOrientedLocSvc(device.getFirst(), device.getSecond());

        // Now we can monitor the system for the satisfaction of our properties
        SpatialTemporalMonitor<Double, List<Comparable<?>>, Double> safety = phi1();
        SpatialTemporalSignal<Double> result = safety.monitor(locService, signal);

        Signal<Double> output = result.getSignals().get(0);

        SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean> liveness = phi2();
        result = liveness.monitor(locService, signal);
        output = result.getSignals().get(0);

        //Statistical Model Checking
        StatisticalModelChecker<Double, List<Comparable<?>>, Double> smc =
                new StatisticalModelChecker<>(safety, data, locService);
        smc.compute();
        double[][] sat = filterAverage(smc.getStats());

        PrintingStrategy<double[][]> str = new RawTrajectoryExtractor(network.size());
        new DataWriter<>(RESULT, FileType.CSV, str).write(sat);

        System.out.println("Saving output in :" + RESULT);

        //System.out.println("SMC Average Satisfiability: " + smc.getStats().toString());
    }*/

    private static String outputFile(String path, String ext1, String ext2) {
        String trace = ext1 + "_" + ext2;
        String newpath = Erlang.class.getResource("/").getPath() + trace + path;
        return newpath.replace("build/classes/java/main",
                "src/main/resources/eu/quanticol/moonlight/examples/subway");
    }

    private static double[][] filterAverage(SignalStatistics.Statistics[][] stats) {
        double[][] output = new double[stats.length][stats[0].length];
        for(int l = 0; l < stats.length; l++) {
            for(int t = 0; t < stats[0].length; t++) {
                output[l][t] = stats[l][t].average;
            }
        }
        return output;
    }

    private static double[][] filterVariance(SignalStatistics.Statistics[][] stats) {
        double[][] output = new double[stats.length][stats[0].length];
        for(int l = 0; l < stats.length; l++) {
            for(int t = 0; t < stats[0].length; t++) {
                output[l][t] = stats[l][t].variance;
            }
        }
        return output;
    }



    // --------- FORMULAE --------- //


    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean> phi1() {
        return globallyMonitor(
                locationCrowdedness()
                , new Interval(0, TH), SATISFACTION);
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean> phi2() {
        return globallyMonitor(
                reachMonitor(locationCrowdedness(),
                             Grid.distance(1, LH),
                             locationCrowdedness(),
                        SATISFACTION)
                , new Interval(0, TH), SATISFACTION);
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean> phi3() {
        return somewhereMonitor(
                impliesMonitor(
                        notMonitor(locationCrowdedness(), SATISFACTION),
                        SATISFACTION,
                        surroundPhi(notMonitor(locationCrowdedness(), SATISFACTION)
                                   ,locationCrowdedness()))
                , Grid.distance(0, LH), SATISFACTION);
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean>
        surroundPhi(SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean> p1,
                    SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean> p2)
    {
        return andMonitor(p1, SATISFACTION,
                notMonitor(
                        andMonitor(
                                reachMonitor(p1, Grid.distance(1, 1),
                                    notMonitor(
                                            orMonitor(p1, SATISFACTION, p2),
                                            SATISFACTION),
                                SATISFACTION),
                                SATISFACTION,
                                notMonitor(
                                        escapeMonitor(p2, Grid.distance(1, 1),
                                                                SATISFACTION),
                                        SATISFACTION))
                        , SATISFACTION));
    }

    /**
     * The minimum requirement for the feature to be right, is that
     * each request receives a response from a feasible location.
     *
     * In symbols: G_TH (request -> eligibleLoc)
     *
     * @return a GloballyMonitor for the property
     */
    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Double> neighbourSafety() {
        return globallyMonitor(   // Globally in TH...
                    impliesMonitor(request(), ROBUSTNESS, eligibleLoc())
                , new Interval(0, TH), ROBUSTNESS);
    }

    /**
     * The minimum requirement for the feature to be meaningful, is that
     * at some point a request is received, and within a maximum time limit,
     * a response to the device is issued.
     *
     * In symbols: F_T2 (request ∧ F_T3 response)
     *
     * @return an EventuallyMonitor for the property
     */
    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Double> communicationLiveness() {
        return eventuallyMonitor(   // Eventually in T2...
                andMonitor(request(), ROBUSTNESS,
                        eventuallyMonitor(response(), new Interval(0, T3), ROBUSTNESS))
                , new Interval(0, T2), ROBUSTNESS);
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Double> request() {
        return devConnected();
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Double> eligibleLoc() {
        return somewhereMonitor(
                andMonitor(locationCrowdednessDouble(), ROBUSTNESS, locationRouter())
                , Grid.distance(0, LH), ROBUSTNESS);
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Double> response() {
        return outputRouter(); //some quality metrics may be added here...
    }


    // --------- ATOMIC PREDICATES --------- //

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Double> devConnected() {
        return atomicMonitor((s -> (Double) s.get(DEV_CONNECTED)));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean> devMoving() {
        return atomicMonitor((s -> s.get(DEV_DIRECTION) != GridDirection.HH));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Double> locationRouter() {
        //return atomicMonitor((s -> (Integer) s.get(LOC_ROUTER) >= 0));
        return atomicMonitor((s -> (Double) s.get(LOC_ROUTER)));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean> locationCrowdedness() {
        return atomicMonitor((s -> (Float) s.get(LOC_CROWDEDNESS) < K));
        //return atomicMonitor((s -> (Float) s.get(LOC_CROWDEDNESS) - C));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Double> locationCrowdednessDouble() {
        //return atomicMonitor((s -> (Float) s.get(LOC_CROWDEDNESS) < C));
        return atomicMonitor((s -> (Float) s.get(LOC_CROWDEDNESS) - K));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Double> outputRouter() {
        //return atomicMonitor((s -> (Integer) s.get(OUT_ROUTER) >= -1));
        return atomicMonitor((s -> (Double) s.get(OUT_ROUTER) - 1));
    }


    // ------------- HELPERS ------------- //

    /**
     * Given a list of (devPos, devDir), one for every time instant,
     * it updates the graph by adding orientation-specific edges.
     *
     * @param devPos list of positions of the device, at each time instant
     * @param devDir list of directions of the device, at each time instant
     * @return a dynamic location service for the grid spatial model
     */
    private static LocationService<Double>
    createOrientedLocSvc(List<Integer> devPos, List<GridDirection> devDir) {
        LocationServiceList<Double> locService = new LocationServiceList<>();

        // initial configuration
        List<Pair<Double, Integer>> edges = getEdges(devPos.get(0), devDir.get(0));
        for(Pair<Double, Integer> e: edges)
            network = network.add(devPos.get(0), e.getFirst(), e.getSecond());

        locService.add(devPos.get(0), network);

        //for each position at every time instant...
        for (int i = 1; i < devPos.size(); i++) {
            // remove previous edges
            for(Pair<Double, Integer> e: edges)
                network = network.remove(devPos.get(i - 1), e.getSecond());

            // fetch new neighbours
            edges = getEdges(devPos.get(i), devDir.get(i));

            // add new edges
            for(Pair<Double, Integer> e: edges)
                network = network.add(devPos.get(i), e.getFirst(), e.getSecond());

            locService.add(i, network);
        }

        return locService;
    }

    /**
     * Given a node, returns a set of edges in the given direction
     * @param node node identifier
     * @param dir direction of interest
     * @return a list of (weight, destination) relevant to the given direction
     */
    private static List<Pair<Double, Integer>> getEdges(Integer node, GridDirection dir) {
        List<Integer> ns = Grid.getNeighboursByDirection(node, dir, network.size());

        List<Pair<Double, Integer>> edges = new ArrayList<>();
        for(Integer n: ns) {
            edges.add(new Pair<>(1.0, n));
        }

        return edges;
    }




}