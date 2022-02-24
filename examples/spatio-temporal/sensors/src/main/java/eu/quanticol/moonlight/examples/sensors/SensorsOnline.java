package eu.quanticol.moonlight.examples.sensors;

import eu.quanticol.moonlight.online.signal.*;
import eu.quanticol.moonlight.domain.DoubleDistance;
import eu.quanticol.moonlight.util.Logger;
import eu.quanticol.moonlight.domain.*;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.SpatialTemporalMonitoring;
import eu.quanticol.moonlight.online.monitoring.OnlineSpaceTimeMonitor;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.core.space.DefaultDistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.Plotter;
import eu.quanticol.moonlight.util.Stopwatch;
import eu.quanticol.moonlight.utility.matlab.MatlabRunner;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static eu.quanticol.moonlight.util.Utils.*;

/**
 * Note: requires Matlab R2020a+ & Machine Learning toolbox plugin
 * in running environment
 */
public class SensorsOnline {
    // Experiment observation settings
    private static final int RND_SEED = 17;
    private static final Plotter plt = new Plotter(10.0);
    private static final int ITERATIONS = 1;
    private static final List<Stopwatch> stopwatches = new ArrayList<>();
    private static final Logger LOG = Logger.getLogger();
    private static final List<String> output = new ArrayList<>();

    // Matlab settings
    private static final String MATLAB_SCRIPT = "sensorNetworkExample";
    private static final String LOCAL_PATH = getLocalPath();

    // Experiment settings
    private static final double TIME_STEPS = 30.0;
    private static final int NODES = 5;

    private static Map<String,
                       Function<Parameters,
                                Function<Pair<Integer,Double>, Boolean>>>
                                                                 atomicFormulas;
    private static HashMap<String, Function<SpatialModel<Double>,
            DefaultDistanceStructure<Double, ?>>> distanceFunctions;
    private static LocationService<Double, Double> locSvc;

    // Device types
    private static final String COORDINATOR = "type1";
    private static final String ROUTER = "type2";
    private static final String END_DEVICE = "type3";

    // Humidity predicates
    private static final String HIGH_HUMIDITY = "highH";
    private static final String NORMAL_HUMIDITY = "normalH";

    // threshold constants
    private static final int HIGH_H = 60;
    private static final int OK_H = 30;
    private static final int T = 3;

    // Distance functions
    private static final String DISTANCE = "dist";
    private static final String DISTANCE2 = "dist2";

    private static double[] times;
    private static int[][] nodesType;
    private static double[][] temperature;

    public static void main(String[] args) {
        MatlabRunner matlab = new MatlabRunner(LOCAL_PATH);
        runSimulator(matlab);
        setAtomicFormulas();
        setDistanceFunctions();

        Formula f1 = formula1();
        repeatedRunner("F1 offline", () -> checkOffline(f1));
        repeatedRunner("F1 online IO",
                () -> checkOnline(f1, false, false));
        repeatedRunner("F1 online IO Parallel",
                () -> checkOnline(f1, false, true));

        Formula f2 = formula2();
        repeatedRunner("F2 offline", () -> checkOffline(f2));
        repeatedRunner("F2 online IO",
                        () -> checkOnline(f2, false, false));
        repeatedRunner("F2 online IO Parallel",
                        () -> checkOnline(f2, false, true));

//        Formula f3 = formula3();
//        repeatedRunner("F3 offline", () -> checkOffline(f3));
//        repeatedRunner("F3 online IO",
//                () -> checkOnline(f3, false, false));
//        repeatedRunner("F3 online IO Parallel",
//                () -> checkOnline(f3, false, true));


        output.forEach(LOG::info);
    }

    static void repeatedRunner(String title, Runnable task)
    {
        Optional<Long> total =
                IntStream.range(0, ITERATIONS)
                        .boxed()
                        .map(i -> {
                            task.run();
                            return stopwatches.get(i).getDuration();
                        }).reduce(Long::sum);

        double tot = total.orElse(0L);

        tot = (tot / ITERATIONS) / 1000.; // Converted to seconds

        output.add(title + " Execution time (avg over " + ITERATIONS + "):" +
                   tot);

        stopwatches.clear();
    }

    private static String getLocalPath() {
        URL url = Objects.requireNonNull(
                SensorsOnline.class.getResource(MATLAB_SCRIPT + ".m"));

        String localPath = null;
        try {
            localPath = Paths.get(url.toURI())
                    .getParent().toAbsolutePath().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return localPath;
    }

    private static Formula formula1() {
        Formula highHumidity = new AtomicFormula(HIGH_HUMIDITY);
        Formula normalHumidity = new AtomicFormula(NORMAL_HUMIDITY);
        return new OrFormula(new NegationFormula(highHumidity),
                             new EventuallyFormula(normalHumidity,
                                                   new Interval(0, T)));
    }

    private static Formula formula2() {
        Formula isType1 = new AtomicFormula(COORDINATOR);
        Formula someWhere = new SomewhereFormula(DISTANCE, isType1);
        return new EverywhereFormula(DISTANCE, someWhere);
    }

    private static Formula formula3() {
        Formula highHumidity = new AtomicFormula(HIGH_HUMIDITY);
        Formula normalHumidity = new AtomicFormula(NORMAL_HUMIDITY);
        Formula escapeHumidity = new EscapeFormula(DISTANCE2, normalHumidity);
        
//        return new OrFormula(new NegationFormula(highHumidity),
//                new EventuallyFormula(escapeHumidity,
//                        new Interval(0, T)));
        return escapeHumidity;
    }

    private static void setDistanceFunctions() {
        distanceFunctions = new HashMap<>();
        distanceFunctions.put(DISTANCE,
                m -> new DefaultDistanceStructure<>(x -> x , new DoubleDistance(),
                        0.0, 10.0, m));

        distanceFunctions.put(DISTANCE2,
                m -> new DefaultDistanceStructure<>(x -> x , new DoubleDistance(),
                        0.0, 2.0, m));
    }

    private static void setAtomicFormulas() {
        atomicFormulas = new HashMap<>();
        atomicFormulas.put(COORDINATOR, p -> (x -> x.getFirst() == 1));
        atomicFormulas.put(ROUTER, p -> (x -> x.getFirst() == 2));
        atomicFormulas.put(END_DEVICE, p -> (x -> x.getFirst() == 3));

        atomicFormulas.put(HIGH_HUMIDITY, p -> (x -> x.getSecond() >= HIGH_H));
        atomicFormulas.put(NORMAL_HUMIDITY, p -> (x -> x.getSecond() < OK_H));
    }

    private static void runSimulator(MatlabRunner matlab)
    {
        matlab.putVar("num_nodes", NODES);
        matlab.putVar("numSteps", TIME_STEPS);

        /// Trace generation
        matlab.eval(MATLAB_SCRIPT);

        // Reading results
        times = matlab.getVar("time");
        nodesType = new int[times.length][NODES];
        temperature = new double[times.length][NODES];
        loadLocationServiceData(matlab);
        loadSignalData(matlab);
    }

    private static void loadSignalData(MatlabRunner matlab) {
        for(int i = 0; i < times.length; i++) {
            int idx = i + 1;    // Matlab index offset
            matlab.eval("types = int32(cell2mat(spatialModelc{" + idx +
                        ", 1}.Nodes.nodeType))");
            matlab.eval("temperature = spatialModelc{" + idx +
                        ", 1}.Nodes.temperature");
            nodesType[i] = matlab.getVar("types");
            temperature[i] = matlab.getVar("temperature");
        }
    }



    private static void loadLocationServiceData(MatlabRunner matlab) {
        List<SpatialModel<Double>> models = new ArrayList<>();
        for(int i = 1; i <= times.length; i++) {
            matlab.eval("edges = int32(spatialModelc{" + i +
                        ", 1}.Edges.EndNodes);");
            matlab.eval("weights = spatialModelc{" + i +
                        ", 1}.Edges.Weights(:,1);");
            int[][] edges = matlab.getVar("edges");
            double[] weightsData = matlab.getVar("weights");
            Double[] weights = Arrays.stream(weightsData)
                    .boxed().toArray(Double[]::new);
            SpatialModel<Double> graph = createGraphFromMatlabData(NODES,
                                                                    edges,
                                                                    weights);
            models.add(graph);
        }
        SpatialModel<Double>[] graphs = models.toArray(new SpatialModel[0]);
        locSvc = createLocationServiceFromTimesAndModels(times, graphs);

    }

    private static void checkOffline(Formula f)
    {
        SpatialTemporalSignal<Pair<Integer, Double>> stSignal =
                generateSTSignal();

        SpatialTemporalMonitor<Double, Pair<Integer, Double>, Boolean>
                m = new SpatialTemporalMonitoring<>(atomicFormulas,
                                                    distanceFunctions,
                                                    new BooleanDomain(),
                                                    false)
                                .monitor(f, null);

        // Actual monitoring...
        Stopwatch rec = Stopwatch.start();
        SpatialTemporalSignal<Boolean> sOut = m.monitor(locSvc, stSignal);
        rec.stop();

        stopwatches.add(rec);
        //output.add("Offline execution time: " + rec.getDuration() + "ms");

        List<Signal<Boolean>> signals = sOut.getSignals();
    }

    private static SpatialTemporalSignal<Pair<Integer, Double>>
    generateSTSignal()
    {
        SpatialTemporalSignal<Pair<Integer, Double>> stSignal =
                new SpatialTemporalSignal<>(NODES);

        IntStream.range(0, times.length)
                .forEach(time -> stSignal
                        .add(time, (location ->
                                        new Pair<>(nodesType[time][location],
                                                   temperature[time][location])
                                   )
                            )
                );
        return stSignal;
    }

    private static List<TimeChain<Double, List<Pair<Integer, Double>>>>
    generateSTUpdates()
    {
        List<TimeChain<Double, List<Pair<Integer, Double>>>> result  =
                new ArrayList<>();
        TimeChain<Double, List<Pair<Integer, Double>>> chain =
                new TimeChain<>(times[times.length - 1]);
        result.add(chain);

        IntStream.range(0, times.length)
                .forEach(time ->
                        {
                            List<Pair<Integer, Double>> locations =
                                    spaceDataFromTime(time);
                            SegmentInterface<Double,
                                                                        List<Pair<Integer, Double>>>
                                    segment = new TimeSegment<>((double) time,
                                                                locations);
                            chain.add(segment);
                    }
                );

        return result;
    }

    private static List<Pair<Integer, Double>> spaceDataFromTime(int time) {
        return IntStream.range(0, NODES).boxed().map(location ->
                new Pair<>(nodesType[time][location], temperature[time][location])
        ).collect(Collectors.toList());
    }

    private static void checkOnline(Formula f, boolean shuffle, boolean parallel)
    {


        OnlineSpaceTimeMonitor<Double, Pair<Integer, Double>, Double> m =
                onlineMonitorInit(f, parallel);

        List<Update<Double, List<Pair<Integer, Double>>>> updates =
                generateSTUpdates().get(0).toUpdates();

        if(shuffle)
            Collections.shuffle(updates, new Random(RND_SEED));

        Stopwatch rec = Stopwatch.start();
        SpaceTimeSignal<Double, AbstractInterval<Double>> result = null;
        for(Update<Double, List<Pair<Integer, Double>>> u: updates) {
            result = m.monitor(u);
        }
        rec.stop();

        stopwatches.add(rec);
        //output.add("Online execution time: " + rec.getDuration() + "ms");

        //plt.plotAll(result.getSegments(), f.toString());
    }

    private static
    OnlineSpaceTimeMonitor<Double, Pair<Integer, Double>, Double>
    onlineMonitorInit(Formula f, boolean parallel)
    {
        Map<String, Function<Pair<Integer,Double>, AbstractInterval<Double>>>
            atoms = setOnlineAtoms();

        return new OnlineSpaceTimeMonitor<>(f, NODES, new DoubleDomain(),
                                            locSvc, atoms, distanceFunctions,
                                            parallel);
    }

    private static
    Map<String, Function<Pair<Integer, Double>, AbstractInterval<Boolean>>>
    setOnlineAtomsBoolean()
    {
        Map<String, Function<Pair<Integer, Double>, AbstractInterval<Boolean>>>
                atoms = new HashMap<>();
        atoms.put(COORDINATOR, x -> booleanInterval(x.getFirst() == 1));
        atoms.put(ROUTER, x -> booleanInterval(x.getFirst() == 2));
        atoms.put(END_DEVICE, x -> booleanInterval(x.getFirst() == 3));

        atoms.put(HIGH_HUMIDITY, x -> booleanInterval(x.getSecond() >= HIGH_H));
        atoms.put(NORMAL_HUMIDITY, x -> booleanInterval(x.getSecond() <= OK_H));
        return atoms;
    }

    private static
    Map<String, Function<Pair<Integer, Double>, AbstractInterval<Double>>>
    setOnlineAtoms()
    {
        Map<String, Function<Pair<Integer, Double>, AbstractInterval<Double>>>
                atoms = new HashMap<>();
        atoms.put(COORDINATOR, x -> doubleInterval(x.getFirst() == 1 ? 1 : 0));
        atoms.put(ROUTER, x -> doubleInterval(x.getFirst() == 2 ? 1 : 0));
        atoms.put(END_DEVICE, x -> doubleInterval(x.getFirst() == 3 ? 1 : 0));

        atoms.put(HIGH_HUMIDITY, x -> doubleInterval(HIGH_H - x.getSecond()));
        atoms.put(NORMAL_HUMIDITY, x -> doubleInterval(OK_H - x.getSecond()));
        return atoms;
    }

    private static AbstractInterval<Double> doubleInterval(double value) {
        return new AbstractInterval<>(value, value);
    }

    private static AbstractInterval<Boolean> booleanInterval(boolean cond) {
        return cond ? new AbstractInterval<>(true, true) :
                      new AbstractInterval<>(false, false);
    }
}
