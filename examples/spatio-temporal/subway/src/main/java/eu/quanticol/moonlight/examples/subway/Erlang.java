package eu.quanticol.moonlight.examples.subway;

import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.io.DataWriter;
import eu.quanticol.moonlight.io.parsing.PrintingStrategy;
import eu.quanticol.moonlight.io.parsing.RawTrajectoryExtractor;
import eu.quanticol.moonlight.space.LocationService;
import eu.quanticol.moonlight.space.SpatialModel;
import eu.quanticol.moonlight.statistics.SignalStatistics;
import eu.quanticol.moonlight.util.MultiValuedTrace;
import eu.quanticol.moonlight.examples.subway.grid.Grid;
import eu.quanticol.moonlight.io.DataReader;
import eu.quanticol.moonlight.io.FileType;
import eu.quanticol.moonlight.io.parsing.MultiRawTrajectoryExtractor;
import eu.quanticol.moonlight.statistics.StatisticalModelChecker;
import eu.quanticol.moonlight.domain.BooleanDomain;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.util.TestUtils;

import static eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor.*;
import static eu.quanticol.moonlight.examples.subway.ErlangSignal.*;

import java.util.ArrayList;
import java.util.Arrays;
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
    private static final String DATA_DIR = "CARar_3_steps_ahead/";
    private static final String NETWORK_FILE = "adjacent_matrix_milan_grid_21x21.txt";
    private static final String TRAJ_FILE_PART = "_trajectories_grid_21x21_T_144_h_";
    private static final String TRAJ_FILE_EXT = ".csv";
    private static final String RESULT = "_smc_grid_21x21_T_144.csv";
    private static final String REAL_DATA = "data_matrix_20131111.csv";
    private static final String NETWORK_SOURCE =
            Erlang.class.getResource(NETWORK_FILE).getPath();

    /**
     * Numeric constants of the problem
     */
    private static final double K = 500;     // crowdedness threshold
    private static final double TH = 3;   // properties time horizon

    /**
     * We initialize the domains and the spatial network
     * @see Grid for a description of the spatial model.
     */
    private static final SignalDomain<Double> ROBUSTNESS = new DoubleDomain();
    private static final SignalDomain<Boolean> SATISFACTION = new BooleanDomain();
    private static SpatialModel<Double> network = new Grid().getModel(NETWORK_SOURCE);

    /**
     * Signal Dimensions (i.e. signal domain)
     */
    //private static final RawTrajectoryExtractor singleTraj = new RawTrajectoryExtractor(network.size());
    private static final ErlangSignal processor = new ErlangSignal(4);
    private static final MultiRawTrajectoryExtractor multiTraj = new MultiRawTrajectoryExtractor(network.size(), processor);



    public static void main(String[] argv) {
        mainTrajectories();
        //realData();
    }

    public static void mainTrajectories() {
        LOG.info(() -> "The network size is: " + network.size());

        LocationService<Double, Double> locService =
                TestUtils.createLocServiceStatic(0, 1.0,
                                                 multiTraj.getTimePoints(),
                                                 network);

        Collection<MultiValuedTrace> trajectories = loadTrajectories();

//        smc(phi1(SATISFACTION), "s_p1", trajectories, locService);
//        smc(phi1(ROBUSTNESS), "r_p1", trajectories, locService);
//        smc(real_phi1(SATISFACTION), "real_s_p1", trajectories, locService);
//        smc(real_phi1(ROBUSTNESS), "real_r_p1", trajectories, locService);
        smc(phi2(SATISFACTION), "s_p2", trajectories, locService);
        smc(phi2(ROBUSTNESS), "r_p2", trajectories, locService);
//        smc(phi3(SATISFACTION), "s_p3", trajectories, locService);
//        smc(phi3(ROBUSTNESS), "r_p3", trajectories, locService);
        /*smc(phi4(SATISFACTION), "s_p4", trajectories, locService);
        smc(phi4(ROBUSTNESS), "r_p4", trajectories, locService);*/
    }

    private static <D> void smc(
            SpatialTemporalMonitor<Double, List<Comparable<?>>, D> p,
            String id,
            Collection<MultiValuedTrace> trajectories,
            LocationService<Double, Double> locService)
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

        LOG.info("SMC results: " + Arrays.toString(smc.getStats()));
    }

    private static Collection<MultiValuedTrace> loadTrajectories() {
        Collection<MultiValuedTrace> trajectories = new ArrayList<>();
        for(int i = 1; i <= 5; i++) {
            String t = "100";
            if(i < 10)
                t = "00".concat(String.valueOf(i));
            else if(i < 100)
                t = "0".concat(String.valueOf(i));
            trajectories.add(loadTrajectory(t));
            LOG.info("Trajectory " + i + " loaded successfully!");
        }
        return trajectories;
    }

    private static MultiValuedTrace loadTrajectory(String i) {
        ErlangSignal processor = new ErlangSignal(4);
        MultiRawTrajectoryExtractor extractor =
                    new MultiRawTrajectoryExtractor(network.size(), processor);

        new DataReader<>(path(REAL_DATA), FileType.CSV, extractor).read();

        for(int predictor = 1; predictor <= 3; predictor++) {
            String path = path(DATA_DIR + i + TRAJ_FILE_PART + predictor + TRAJ_FILE_EXT);
            new DataReader<>(path, FileType.CSV, extractor).read();
        }

        return processor.generateSignal();
    }

    private static String outputFile(String path, String ext1, String ext2) {
        String trace = DATA_DIR + ext1 + "_" + ext2 + "_K" + (int) K;
        String newpath = Erlang.class.getResource("/").getPath() + trace + path;
        newpath = newpath.replace("build/classes/java/main",
                "src/main/resources/eu/quanticol/moonlight/examples/subway");


        LOG.info("Saving output in :" + newpath);

        return newpath;
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


    private static String path(String p) {
        return Erlang.class.getResource(p).getPath();
    }


    // --------- FORMULAE --------- //

    /**
     * PoiReach -> P4 paper, hospital reachability
     */
    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> phi4(SignalDomain<D> d) {
        return reachMonitor(isNotCrowded(d)
                            , Grid.distance(0, 4),
                            isHospital(d)
                            , d)
               ;
    }

    /**
     * real-data P1 paper
     */
    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> real_phi1(SignalDomain<D> d) {
        return  impliesMonitor(
                notMonitor(isNotCrowded(d), d),
                d,
                eventuallyMonitor(
                        isNotCrowded(d),
                        new Interval(0, TH), d
                    )
        );
    }

    /**
     * P1 paper, crowdedness goes down at some point
     */
    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> phi1(SignalDomain<D> d) {
        return  impliesMonitor(
                    notMonitor(isNotCrowded(d), d),
                    d,
                    orMonitor(isNotCrowded(d), d, orMonitor(fstStep(d), d, orMonitor(sndStep(d), d, trdStep(d))))
                    //eventuallyMonitor(
                    //        isNotCrowded(d),
                    //        new Interval(0, TH), d
                    //    )
                    );
    }

    /**
     * P2 paper
     */
    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> phi2(SignalDomain<D> d) {
        return impliesMonitor(
                    notMonitor(isNotCrowded(d), d), /// Crowd > K
                    d,
                    somewhereMonitor(isNotCrowded(d), Grid.distance(0,1)
                            , d)
                    )
        ;
    }

    /**
     * P3 paper
     */
    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> phi3(SignalDomain<D> d) {
        return impliesMonitor(
                globallyMonitor(notMonitor(isNotCrowded(d), d), new Interval(0,3),d)
                , d,
                somewhereMonitor(andMonitor(isNotCrowded(d), d, andMonitor(fstStep(d), d, andMonitor(sndStep(d), d, trdStep(d))))
                        //globallyMonitor(isNotCrowded(d),
                        //         new Interval(0, 3), d)
                        ,
                                 Grid.distance(0,1), d)
                )
                ;
    }

    /**
     * For debugging
     */
    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D> phi0(SignalDomain<D> d) {
        return  notMonitor(isNotCrowded(d), d);
    }

    // --------- ATOMIC PREDICATES --------- //
    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D>
    isNotCrowded(SignalDomain<D> d)
    {
        if(d instanceof DoubleDomain) {
            return (SpatialTemporalMonitor<Double, List<Comparable<?>>, D>) lcDouble();
        } else if(d instanceof BooleanDomain) {
            return (SpatialTemporalMonitor<Double, List<Comparable<?>>, D>) lcBoolean();
        } else
            throw new UnsupportedOperationException(INVALID_DOMAIN);
    }

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D>
    fstStep(SignalDomain<D> d)
    {
        if(d instanceof DoubleDomain) {
            return (SpatialTemporalMonitor<Double, List<Comparable<?>>, D>) fstepcDouble();
        } else if(d instanceof BooleanDomain) {
            return (SpatialTemporalMonitor<Double, List<Comparable<?>>, D>) fstepcBoolean();
        } else
            throw new UnsupportedOperationException(INVALID_DOMAIN);
    }

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D>
    sndStep(SignalDomain<D> d)
    {
        if(d instanceof DoubleDomain) {
            return (SpatialTemporalMonitor<Double, List<Comparable<?>>, D>) sstepcDouble();
        } else if(d instanceof BooleanDomain) {
            return (SpatialTemporalMonitor<Double, List<Comparable<?>>, D>) sstepcBoolean();
        } else
            throw new UnsupportedOperationException(INVALID_DOMAIN);
    }

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D>
    trdStep(SignalDomain<D> d)
    {
        if(d instanceof DoubleDomain) {
            return (SpatialTemporalMonitor<Double, List<Comparable<?>>, D>) tstepcDouble();
        } else if(d instanceof BooleanDomain) {
            return (SpatialTemporalMonitor<Double, List<Comparable<?>>, D>) tstepcBoolean();
        } else
            throw new UnsupportedOperationException(INVALID_DOMAIN);
    }

    private static <D> SpatialTemporalMonitor<Double, List<Comparable<?>>, D>
    isHospital(SignalDomain<D> d)
    {
        if(d instanceof DoubleDomain) {
            return (SpatialTemporalMonitor<Double, List<Comparable<?>>, D>) isHDouble();
        } else if(d instanceof BooleanDomain) {
            return (SpatialTemporalMonitor<Double, List<Comparable<?>>, D>) isHBoolean();
        } else
            throw new UnsupportedOperationException(INVALID_DOMAIN);
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean>
    lcBoolean()
    {
        return atomicMonitor((s -> (Float) s.get(LOC_CROWDEDNESS) < K));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Double>
    lcDouble()
    {
        return atomicMonitor((s -> K - (Float) s.get(LOC_CROWDEDNESS)));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean>
    fstepcBoolean()
    {
        return atomicMonitor((s -> (Float) s.get(CROWDEDNESS_1_STEP) < K));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Double>
    fstepcDouble()
    {
        return atomicMonitor((s -> K - (Float) s.get(CROWDEDNESS_1_STEP)));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean>
    sstepcBoolean()
    {
        return atomicMonitor((s -> (Float) s.get(CROWDEDNESS_2_STEP) < K));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Double>
    sstepcDouble()
    {
        return atomicMonitor((s -> K - (Float) s.get(CROWDEDNESS_2_STEP)));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean>
    tstepcBoolean()
    {
        return atomicMonitor((s -> (Float) s.get(CROWDEDNESS_3_STEP) < K));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Double>
    tstepcDouble()
    {
        return atomicMonitor((s -> K - (Float) s.get(CROWDEDNESS_3_STEP)));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Boolean>
    isHBoolean()
    {
        return atomicMonitor((s -> s.get(IS_HOSPITAL) == Boolean.TRUE));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable<?>>, Double>
    isHDouble()
    {
        return atomicMonitor((s -> s.get(IS_HOSPITAL) == Boolean.TRUE ?
                        Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY));
    }


    // ------------- HELPERS ------------- //

    private static final String INVALID_DOMAIN = "Unsupported Signal Domain!";

}