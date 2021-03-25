package eu.quanticol.moonlight.examples.bikes;

import eu.quanticol.moonlight.examples.bikes.utilities.SStats;
import eu.quanticol.moonlight.examples.bikes.utilities.SimHyAWrapper;
import eu.quanticol.jsstl.core.io.SyntaxErrorExpection;
import eu.quanticol.jsstl.core.io.TraGraphModelReader;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.SpatialTemporalMonitoring;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.domain.BooleanDomain;
import eu.quanticol.moonlight.domain.DoubleDistance;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.space.DistanceStructure;
import eu.quanticol.moonlight.space.GraphModel;
import eu.quanticol.moonlight.space.LocationService;
import eu.quanticol.moonlight.space.SpatialModel;
import eu.quanticol.moonlight.util.Pair;

import static eu.quanticol.moonlight.util.TestUtils.*;
import static java.util.Arrays.copyOfRange;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class contains an example based on a Bike Sharing System (BSS).
 * The system is modeled as a graph of 733 stations, and the same
 * number of bikes.
 * The simulation of bike traces is done by using SimHyA,
 * while the property to be monitored is presented later on.
 */
public class Bikes {
    /**
     * The files containing the data to run the experiment
     */
    private static final String SIMHYA_MODEL_FILE = "733bike.txt";
    private static final String GRAPH_FILE = "733stationsGraph.tra";
    /* An alternative smaller model is available, decomment the following.
    *  Note that it also requires proper adjustment of the simulator, or,
    *  alternatively, a prototypical trace is available at file:
    *  "trajectory.tra"
    */
    //private static final String GRAPH_FILE = "bssSpatialModel.tra";

    /**
     * Experiment General Constants
     */
    private static final int SIMULATION_TIME = 50; // Timespan of the simulation
    private static final int SIMULATION_STEPS = 100;
    private static final int SPACE_LOCATIONS = 733;

    /**
     * Formula constants
     */
    private static final double T_end = 40; // Time horizon of the property
    private static final double d = 0.3;    // Max distance we want to consider

    public static void main(String[] args) {
        // ****************************** INPUT ***************************** //
        // We generate a trajectory by simulating a bike sharing model
        // Powered by SimHyA...
        double[][] simulation = simulatorSetup();

        // We parse the simulated data as a Moonlight trajectory
        SpatialTemporalSignal<Pair<Double, Double>> trajectory =
                parseTrajectory(simulation);

        // We load the spatial graph model
        SpatialModel<Double> spatialModel = loadSpatialGraph();

        // We initialize a static location service on the space graph
        // (i.e. the space graph never changes its topology during monitoring)
        double[] times = copyOfRange(simulation[0], 0, SIMULATION_STEPS);
        LocationService<Double, Double> locService =
                createLocServiceStaticFromTimeTraj(times, spatialModel);

        // We initialize the set of atomic properties of our interest
        HashMap<String,
                Function<Parameters, Function<Pair<Double, Double>, Boolean>>>
                atomicFormulas = new HashMap<>();

        // *********************** DISTANCE FUNCTIONS *********************** //

        // We define some spatial distance functions of interest
        HashMap<String,
                Function<SpatialModel<Double>, DistanceStructure<Double, ?>>>
                distanceFunctions = new HashMap<>();
        DistanceStructure<Double, Double> dist =
                new DistanceStructure<>(x -> x,
                        new DoubleDistance(),
                        0.0, d,
                        spatialModel);
        distanceFunctions.put("dist", x -> dist);

        DistanceStructure<Double, Double> dist_spot =
                new DistanceStructure<>(x -> x,
                        new DoubleDistance(),
                        0.0, 1.0,
                        spatialModel);
        distanceFunctions.put("dist_spot", x -> dist_spot);

        // ***************************** FORMULA **************************** //

        // We want to check the phi1 property, informally stated here:
        // if I don't find a bike (resp. free slot),
        // can I find another station close enough
        // (i.e. within distance threshold d)
        // where I can find a bike (resp. free slot)?
        Formula phi1 = getPhi1Property(atomicFormulas);

        // Or we can alternatively search for spots:
        //Formula phi1 = getSpotProperty(atomicFormulas);



        // *************************** MONITORING *************************** //

        SStats<SpatialTemporalSignal<Boolean>> stats = new SStats<>();
        SpatialTemporalSignal<Boolean> result = null;
        for(int i = 0; i < 100; i++) {
            result = stats.record(
                () -> {
                    // We setup the monitoring process
                    SpatialTemporalMonitoring<Double, Pair<Double, Double>,
                                              Boolean>
                            monitor = new SpatialTemporalMonitoring<>(
                                                            atomicFormulas,
                                                            distanceFunctions,
                                                            new BooleanDomain(),
                                                            true);

                    // We instantiate the monitor on the formula of our interest
                    SpatialTemporalMonitor<Double, Pair<Double, Double>,
                                           Boolean> m =
                            monitor.monitor(phi1, null);

                    // We perform the monitoring, and save the output result
                    return m.monitor(locService, trajectory);
                });
        }

        // We show the output
        List<Signal<Boolean>> signals = result.getSignals();
        System.out.print("\nThe monitoring result of the phi1 property is: ");
        System.out.println(signals.get(0).valueAt(0));
        System.out.println("Execution stats:" + stats.analyze());
    }

    /**
     * Instantiates and runs the SimHyA simulator.
     * @return the result of the simulation.
     */
    public static double[][] simulatorSetup() {
        SimHyAWrapper model = new SimHyAWrapper();
        String modelPath = Bikes.class.getResource(SIMHYA_MODEL_FILE).getPath();
        model.loadModel(modelPath);
        model.setGB();

        return model.simulate(SIMULATION_TIME, SIMULATION_STEPS);
    }

    /**
     * Loads the spatial graph from a file
     * @return a graph model
     */
    private static GraphModel<Double> loadSpatialGraph() {
        try {
            String graphPath = Bikes.class.getResource(GRAPH_FILE).getPath();
            eu.quanticol.jsstl.core.space.GraphModel graph =
                    new TraGraphModelReader().read(graphPath);
            graph.dMcomputation();
            GraphModel<Double> newGraphModel =
                    new GraphModel<>(graph.getNumberOfLocations());
            graph.getEdges().forEach(
                    s -> newGraphModel.add(s.lStart.getPosition(),
                            s.weight,
                            s.lEnd.getPosition()));
            return newGraphModel;
        } catch (IOException | SyntaxErrorExpection e) {
            System.out.println("Unable to load the Spatial Graph");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parses the output of a SimHyA simulation as a SpatialTemporalSignal.
     * It expects to receive a sequence of data where the first
     * @param input the results of a SimHya simulation
     * @return a trajectory containing the given data
     */
    private static SpatialTemporalSignal<Pair<Double, Double>> parseTrajectory(
            double[][] input)
    {
        double[][][] data = new double[SPACE_LOCATIONS][SIMULATION_STEPS][2];
        double[] times = new double[SIMULATION_STEPS];
        // We update the new arrays with the provided data.
        // Note that locSize is used as an offset to select S data
        // (i.e. B_i := input[i][t], and S_i := input[i + locSize][t]
        for (int t = 0; t < SIMULATION_STEPS; t++) {
            times[t] = input[0][t]; // row 0 contains the time instants
            for (int i = 1; i < SPACE_LOCATIONS; i++) {
                data[i][t][0] = input[i][t];
                data[i][t][1] = input[i + SPACE_LOCATIONS][t];
            }
        }
        return signalGenerator(times, data);
    }

    /**
     * Generates a two-valued traces given the provide data and time sequences
     * @param times sequence of time instants
     * @param data sequence of couples of data
     * @return a SpatioTemporalSignal on a Pair type.
     * @see Pair
     */
    private static SpatialTemporalSignal<Pair<Double, Double>> signalGenerator(
            double[] times,
            double[][][] data)
    {
        SpatialTemporalSignal<Pair<Double, Double>> signal =
                new SpatialTemporalSignal<>(SPACE_LOCATIONS);

        for (int i = 0; i < times.length; i++) {
            int index = i;
            double t = times[i];

            List<Pair<Double, Double>> values =
                    IntStream.range(0, SPACE_LOCATIONS)
                            .mapToObj(s ->
                                    new Pair<>(data[s][index][0],
                                               data[s][index][1]))
                            .collect(Collectors.toList());

            signal.add(t, values);
        }
        return signal;
    }

    private static Formula getPhi1Property(
            HashMap<String,
                    Function<Parameters, Function<Pair<Double, Double>,
                             Boolean>>> atoms)
    {
        // First atomic property:  B > 0
        atoms.put("B > 0", p -> (x -> x.getFirst() > 0));
        // Second atomic property: S > 0
        atoms.put("S > 0", p -> (x -> x.getSecond() > 0));

        Formula atomB = new AtomicFormula("B > 0");
        Formula atomS = new AtomicFormula("S > 0");
        Formula somewhereB = new SomewhereFormula("dist", atomB);
        Formula somewhereS = new SomewhereFormula("dist", atomS);
        Formula andBS = new AndFormula(somewhereB, somewhereS);

        return new GloballyFormula(andBS, new Interval(0, T_end));
    }

    private static Formula getSpotProperty(
            HashMap<String,
                    Function<Parameters, Function<Pair<Double, Double>,
                             Boolean>>> atoms)
    {
        // First atomic property:  B > 0
        atoms.put("B < 1", p -> (x -> x.getFirst() < 1));
        // Second atomic property: S > 0
        atoms.put("B > 0", p -> (x -> x.getFirst() > 0));

        Formula lowValues = new AtomicFormula("B < 1");
        Formula highValues = new AtomicFormula("B > 0");

        Formula mediumValues = new NegationFormula(
                new OrFormula(lowValues, highValues));

        Formula reachP = new ReachFormula(lowValues,
                                      "dist_spot", mediumValues);

        return new AndFormula(lowValues, new NegationFormula(reachP));
    }
}