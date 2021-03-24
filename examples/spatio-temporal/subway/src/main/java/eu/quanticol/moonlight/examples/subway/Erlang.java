package eu.quanticol.moonlight.examples.subway;

import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.examples.subway.io.DataWriter;
import eu.quanticol.moonlight.examples.subway.parsing.PrintingStrategy;
import eu.quanticol.moonlight.examples.subway.parsing.RawTrajectoryExtractor;
import eu.quanticol.moonlight.signal.space.ImmutableGraphModel;
import eu.quanticol.moonlight.signal.space.LocationService;
import eu.quanticol.moonlight.signal.space.LocationServiceList;
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
import eu.quanticol.moonlight.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

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
 */
public class Erlang {
    private static final Logger LOG = Logger.getLogger(Erlang.class.getName());

    /**
     * Source files location
     */
    private static final String DATA_DIR = "CARar_TODO/";
    private static final String NETWORK_FILE = "adjacent_matrix_milan_grid_21x21.txt";
    private static final String TRAJECTORY_FILE_PART = "_trajectories_grid_21x21_T_144.csv";
    private static final String RESULT = "_smc_grid_21x21_T_144.csv";
    private static final String NETWORK_SOURCE =
            Erlang.class.getResource(DATA_DIR + NETWORK_FILE).getPath();

    /**
     * Numeric constants of the problem
     */
    private static final int LH = 1;       // location horizon (neighbourhood)
    private static final double K = 500;     // crowdedness threshold
    private static final double TH = 3;   // properties time horizon
    private static final double T2 = 7;    // properties time horizon
    private static final double T3 = 1;    // output signal reaction time

    /**
     * We initialize the domains and the spatial network
     * @see Grid for a description of the spatial model.
     */
    private static final SignalDomain<Double> ROBUSTNESS = new DoubleDomain();
    private static final SignalDomain<Boolean> SATISFACTION = new BooleanDomain();
    private static ImmutableGraphModel<Double> network =
            (ImmutableGraphModel<Double>) new Grid().getModel(NETWORK_SOURCE);

    /**
     * Signal Dimensions (i.e. signal domain)
     */
    //private static final RawTrajectoryExtractor singleTraj = new RawTrajectoryExtractor(network.size());
    private static final ErlangSignal processor = new ErlangSignal();
    private static final MultiRawTrajectoryExtractor multiTraj = new MultiRawTrajectoryExtractor(network.size(), processor);
   private static final Collection<MultiValuedTrace> data =
            new DataReader<>(Erlang.class
                    .getResource(DATA_DIR + "001" + TRAJECTORY_FILE_PART)
                    .getPath(), FileType.CSV, multiTraj).read();


    public static void main(String[] argv) {
        LOG.info("The network size is: " + network.size());

        Pair<List<Integer>, List<GridDirection>> device = processor.getSampleDevice();

        //// We are considering a dynamic Location Service ///
        LocationService<Double> locService = createOrientedLocSvc(device.getFirst(), device.getSecond());

        Collection<MultiValuedTrace> trajectories = loadTrajectories();
        smc(phi1(SATISFACTION), "s_p1", trajectories, locService);
        /*smc(phi1(ROBUSTNESS), "r_p1", trajectories, locService);
        smc(phi11(SATISFACTION), "s_p11", trajectories, locService);
        smc(phi11(ROBUSTNESS), "r_p11", trajectories, locService);
        smc(phi2(SATISFACTION), "s_p2", trajectories, locService);
        smc(phi2(ROBUSTNESS), "r_p2", trajectories, locService);
        smc(phi3(SATISFACTION), "p3", trajectories, locService);
        smc(phi3(ROBUSTNESS), "p3", trajectories, locService);
        //smc(poiReach(SATISFACTION), "s_poi", trajectories, locService);
        //smc(poiReach(ROBUSTNESS), "r_poi", trajectories, locService);
        smc(phi33(SATISFACTION), "s_p33", trajectories, locService);
        smc(phi33(ROBUSTNESS), "r_p33", trajectories, locService);*/
        //smc(isHospital(SATISFACTION), "h", trajectories, locService);

        LOG.info("Saving output in :" + RESULT);

        //LOG.info("SMC Average Satisfiability: " + smc.getStats().toString());
    }

    private static <D> void smc(
            SpatialTemporalMonitor<Double, List<Comparable<?>>, D> p,
            String id,
            Collection<MultiValuedTrace> trajectories,
            LocationService<Double> locService)
    {
        //Statistical Model Checking
        StatisticalModelChecker<Double, List<Comparable<?>>, D> smc =
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
            LOG.info("Trajectory " + i + " loaded successfully!");
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
        LOG.info("The network size is: " + network.size());

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

        LOG.info("Saving output in :" + RESULT);

        //LOG.info("SMC Average Satisfiability: " + smc.getStats().toString());
    }*/

    private static String outputFile(String path, String ext1, String ext2) {
        String trace = DATA_DIR + ext1 + "_" + ext2 + "_K" + (int) K;
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

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> poiReach(SignalDomain<D> d) {
        return //impliesMonitor(notMonitor(isNotCrowded(d), d), d,
                    reachMonitor(
                                //orMonitor(
                                        isNotCrowded(d)
                                        //, d,
                                        //isHospital(d))
                                , Grid.distance(0, 6),
                                isHospital(d)
                                //somewhereMonitor(isHospital(d),
                                //                 Grid.distance(0, 3),
                                //                 d)
                                , d)
                //)
        ;
    }
    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> phi0(SignalDomain<D> d) {
        return  notMonitor(isNotCrowded(d), d);
    }

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> phi11(SignalDomain<D> d) {
        return globallyMonitor(phi1(d), d);
    }

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> phi1(SignalDomain<D> d) {
        return  impliesMonitor(
                    notMonitor(isNotCrowded(d), d),
                    d,
                    eventuallyMonitor(
                            isNotCrowded(d),
                            new Interval(0, TH), d
                        )
                    );
    }

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> phi2(SignalDomain<D> d) {
        return escapeMonitor(isNotCrowded(d),
                             Grid.distance(0, LH),
                        d);
    }

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> phi33(SignalDomain<D> d) {
        return impliesMonitor(       // Crowd > K => Everywhere[1,2] Crowd < k)
                    notMonitor(isNotCrowded(d), d), /// Crowd > K
                    d,
                    //surroundPhi(notMonitor(isNotCrowded(d), d),
                    //            isNotCrowded(d), d)
                    //escapeMonitor(locationCrowdedness(d), Grid.distance(1,2), d)
                    somewhereMonitor(isNotCrowded(d), Grid.distance(0,1)
                            , d)
                    )
        ;
    }

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> phi3(SignalDomain<D> d) {
        return orMonitor(
                globallyMonitor(isNotCrowded(d), new Interval(0,3),d)
                , d,
        //return impliesMonitor(
        //        notMonitor(locationCrowdedness(d), d),
                //d,
                //surroundPhi(notMonitor(locationCrowdedness(d), d),
                //            locationCrowdedness(d), d)
                //escapeMonitor(locationCrowdedness(d), Grid.distance(1,2), d)
                somewhereMonitor(globallyMonitor(isNotCrowded(d), new Interval(0,3),d), Grid.distance(1,2), d)
                )
                ;
    }

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D>
        surroundPhi(SpatialTemporalMonitor<Double, List<Comparable<?>>, D> p1,
                    SpatialTemporalMonitor<Double, List<Comparable<?>>, D> p2,
                    SignalDomain<D> d)
    {
        return andMonitor(p1, d,
                        andMonitor(
                            notMonitor(
                                reachMonitor(p1, Grid.distance(0, 2),
                                    notMonitor(
                                        orMonitor(p1, d, p2),
                                    d),
                                d),
                            d),
                                d,
                                notMonitor(
                                        escapeMonitor(p2, Grid.distance(2, Double.POSITIVE_INFINITY),
                                                                d),
                                        d))
                        );
    }

    /**
     * The minimum requirement for the feature to be right, is that
     * each request receives a response from a feasible location.
     *
     * In symbols: G_TH (request -> eligibleLoc)
     *
     * @return a GloballyMonitor for the property
     */
    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> neighbourSafety(SignalDomain<D> d) {
        return globallyMonitor(   // Globally in TH...
                    impliesMonitor(request(d), d, eligibleLoc(d))
                , new Interval(0, TH), d);
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
    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> communicationLiveness(SignalDomain<D> d) {
        return eventuallyMonitor(   // Eventually in T2...
                andMonitor(request(d), d,
                        eventuallyMonitor(response(d), new Interval(0, T3), d))
                , new Interval(0, T2), d);
    }

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> request(SignalDomain<D> d) {
        return devConnected(d);
    }

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> eligibleLoc(SignalDomain<D> d) {
        return somewhereMonitor(
                andMonitor(isNotCrowded(d), d, locationRouter(d))
                , Grid.distance(0, LH), d);
    }

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> response(SignalDomain<D> d) {
        return outputRouter(d); //some quality metrics may be added here...
    }


    // --------- ATOMIC PREDICATES --------- //

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> devConnected(SignalDomain<D> d) {
        if(d instanceof DoubleDomain || d instanceof BooleanDomain) {
            return atomicMonitor((s -> (D) s.get(DEV_CONNECTED)));
        } else
            throw new UnsupportedOperationException("Unsupported Signal Domain!");
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean> devMoving() {
        return atomicMonitor((s -> s.get(DEV_DIRECTION) != GridDirection.HH));
    }

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> locationRouter(SignalDomain<D> d) {
        if(d instanceof DoubleDomain) {
            return (SpatialTemporalMonitor<Double, List<Comparable<?>>, D>) lrDouble();
        } else if(d instanceof BooleanDomain) {
            return (SpatialTemporalMonitor<Double, List<Comparable<?>>, D>) lrBoolean();
        } else
            throw new UnsupportedOperationException("Unsupported Signal Domain!");
    }

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> isNotCrowded(SignalDomain<D> d) {
        if(d instanceof DoubleDomain) {
            return (SpatialTemporalMonitor<Double, List<Comparable<?>>, D>) lcDouble();
        } else if(d instanceof BooleanDomain) {
            return (SpatialTemporalMonitor<Double, List<Comparable<?>>, D>) lcBoolean();
        } else
            throw new UnsupportedOperationException("Unsupported Signal Domain!");
    }

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> outputRouter(SignalDomain<D> d) {
        if(d instanceof DoubleDomain) {
            return (SpatialTemporalMonitor<Double, List<Comparable<?>>, D>) orDouble();
        } else if(d instanceof BooleanDomain) {
            return (SpatialTemporalMonitor<Double, List<Comparable<?>>, D>) orBoolean();
        } else
            throw new UnsupportedOperationException("Unsupported Signal Domain!");
    }

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> isHospital(SignalDomain<D> d) {
        if(d instanceof DoubleDomain) {
            return (SpatialTemporalMonitor<Double, List<Comparable<?>>, D>) isHDouble();
        } else if(d instanceof BooleanDomain) {
            return (SpatialTemporalMonitor<Double, List<Comparable<?>>, D>) isHBoolean();
        } else
            throw new UnsupportedOperationException("Unsupported Signal Domain!");
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean> lrBoolean() {
        return atomicMonitor((s -> (Integer) s.get(LOC_ROUTER) >= 0));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Double> lrDouble() {
        return atomicMonitor((s -> (Double) s.get(LOC_ROUTER)));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean> lcBoolean() {
        return atomicMonitor((s -> (Float) s.get(LOC_CROWDEDNESS) < K));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Double> lcDouble() {
        return atomicMonitor((s -> K - (Float) s.get(LOC_CROWDEDNESS)));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean> orBoolean() {
        return atomicMonitor((s -> (Integer) s.get(OUT_ROUTER) >= -1));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Double> orDouble() {
        return atomicMonitor((s -> (Double) s.get(OUT_ROUTER) - 1));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean> isHBoolean()
    {
        return atomicMonitor((s -> s.get(IS_HOSPITAL) == Boolean.TRUE));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Double> isHDouble()
    {
        return atomicMonitor((s -> s.get(IS_HOSPITAL) == Boolean.TRUE ?
                        Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY));
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