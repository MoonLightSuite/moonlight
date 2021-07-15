package eu.quanticol.moonlight.examples.sensors;

import eu.quanticol.moonlight.domain.*;
import eu.quanticol.moonlight.formula.AtomicFormula;
import eu.quanticol.moonlight.formula.Formula;
import eu.quanticol.moonlight.formula.Parameters;
import eu.quanticol.moonlight.formula.SomewhereFormula;
import eu.quanticol.moonlight.monitoring.SpatialTemporalMonitoring;
import eu.quanticol.moonlight.monitoring.online.OnlineSpaceTimeMonitor;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.signal.online.*;
import eu.quanticol.moonlight.space.DistanceStructure;
import eu.quanticol.moonlight.space.LocationService;
import eu.quanticol.moonlight.space.SpatialModel;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.Stopwatch;
import eu.quanticol.moonlight.util.Utils;
import eu.quanticol.moonlight.utility.matlab.MatlabRunner;
import eu.quanticol.moonlight.utility.matlab.configurator.MatlabDataConverter;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Note: requires Matlab R2020a+ & Machine Learning toolbox plugin
 * in running environment
 */
public class SensorsOnline {
    private static Map<String,
                       Function<Parameters,
                                Function<Pair<Integer,Integer>, Boolean>>>
                                                                 atomicFormulas;

    private static HashMap<String, Function<SpatialModel<Double>,
                               DistanceStructure<Double, ?>>> distanceFunctions;

    private static LocationService<Double, Double> locSvc;

    private static final String MATLAB_SCRIPT = "sensorNetworkExample";
    private static final String LOCAL_PATH = getLocalPath();

    // Device types
    private static final String COORDINATOR = "type1";
    private static final String ROUTER = "type2";
    private static final String END_DEVICE = "type3";

    // Distance functions
    private static final String DISTANCE = "dist";

    private static double nodes;
    private static Double[] nodesType;

    public static void main(String[] args) {
        //TODO populate input signal with more
        Object[] graph = Objects.requireNonNull(runSimulator());
        locSvc = Utils.createLocServiceFromSetMatrix(graph);

        setAtomicFormulas();
        setDistanceFunctions();
        Formula sWhere = formula();

        checkOffline(sWhere);
        checkOnline(sWhere);
        checkOnlineShuffled(sWhere);
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

    private static Object[] runSimulator() {
        return runModel(new MatlabRunner(LOCAL_PATH));
    }

    private static Formula formula() {
        Formula isType1 = new AtomicFormula(COORDINATOR);
        return new SomewhereFormula(DISTANCE, isType1);
    }

    private static void setDistanceFunctions() {
        distanceFunctions = new HashMap<>();
        distanceFunctions.put(DISTANCE,
                m -> new DistanceStructure<>(x -> x , new DoubleDistance(),
                        0.0, 1.0, m));
    }

    private static void setAtomicFormulas() {
        atomicFormulas = new HashMap<>();
        atomicFormulas.put(COORDINATOR, p -> (x -> x.getFirst() == 1));
        atomicFormulas.put(ROUTER, p -> (x -> x.getFirst() == 2));
        atomicFormulas.put(END_DEVICE, p -> (x -> x.getFirst() == 3));
    }

    private static Object[] runModel(MatlabRunner matlab)
    {
        /// Trace generation
        matlab.eval(MATLAB_SCRIPT);

        // Reading results
        nodes = matlab.getVar("num_nodes");
        Object data = matlab.getVar("inputValues");
        nodesType = MatlabDataConverter.getArray(matlab.getVar("nodes_type"),
                                                 Double.class);

        Object[] cGraph1 = matlab.getVar("cgraph1");
        Object[] cGraph2 = matlab.getVar("cgraph2");

        return cGraph1;
    }

    private static void checkOffline(Formula f)
    {
        SpatialTemporalSignal<Pair<Integer, Integer>> stSignal =
                generateSTSignal();

        SpatialTemporalMonitor<Double, Pair<Integer, Integer>, Boolean>
                m = new SpatialTemporalMonitoring<>(atomicFormulas,
                                                    distanceFunctions,
                                                    new BooleanDomain(),
                                                    false)
                                .monitor(f, null);

        // Actual monitoring...
        Stopwatch rec = Stopwatch.start();
        SpatialTemporalSignal<Boolean> sOut = m.monitor(locSvc, stSignal);
        rec.stop();

        System.out.println("Offline execution time: " + rec.getDuration() +
                           "ms");

        List<Signal<Boolean>> signals = sOut.getSignals();
    }

    private static SpatialTemporalSignal<Pair<Integer, Integer>>
    generateSTSignal()
    {
        SpatialTemporalSignal<Pair<Integer, Integer>> stSignal =
                new SpatialTemporalSignal<>(nodesType.length);

        IntStream.range(0, (int) nodes - 1)
                .forEach(time -> stSignal
                        .add(time, (location ->
                                        new Pair<>(nodesType[location]
                                                .intValue(), time)
                                   )
                            )
                );
        return stSignal;
    }

    private static List<TimeChain<Double, List<Pair<Integer, Integer>>>>
    generateSTUpdates()
    {
        List<TimeChain<Double, List<Pair<Integer, Integer>>>> result  =
                new ArrayList<>();
        TimeChain<Double, List<Pair<Integer, Integer>>> chain =
                new TimeChain<>(nodes - 1);
        result.add(chain);

        IntStream.range(0, (int) nodes - 1)
                .forEach(time ->
                        {
                            List<Pair<Integer, Integer>> locations =
                                    spaceDataFromTime(time);
                            SegmentInterface<Double,
                                            List<Pair<Integer, Integer>>>
                                    segment = new TimeSegment<>((double) time,
                                                                locations);
                            chain.add(segment);
                    }
                );

        return result;
    }

    private static List<Pair<Integer, Integer>> spaceDataFromTime(int time) {
        return IntStream.range(0, (int) nodes).boxed().map(location ->
                new Pair<>(nodesType[location].intValue(), time)
        ).collect(Collectors.toList());
    }

    private static void checkOnline(Formula f)
    {
        Stopwatch rec = Stopwatch.start();

        OnlineSpaceTimeMonitor<Double, Pair<Integer, Integer>, Boolean> m =
                onlineMonitorInit(f);

        List<TimeChain<Double, List<Pair<Integer, Integer>>>> updates =
                generateSTUpdates();

        updates.forEach(m::monitor);

        rec.stop();

        System.out.println("Online execution time: " + rec.getDuration() +
                           "ms");
    }

    private static void checkOnlineShuffled(Formula f)
    {
        Stopwatch rec = Stopwatch.start();

        OnlineSpaceTimeMonitor<Double, Pair<Integer, Integer>, Boolean> m =
                onlineMonitorInit(f);

        List<Update<Double, List<Pair<Integer, Integer>>>> updates =
                generateSTUpdates().get(0).toUpdates();

        Collections.shuffle(updates, new Random(6));

        updates.forEach(m::monitor);

        rec.stop();

        System.out.println("Online shuffled execution time: " +
                           rec.getDuration() + "ms");
    }

    private static
    OnlineSpaceTimeMonitor<Double, Pair<Integer, Integer>, Boolean>
    onlineMonitorInit(Formula f)
    {
        Map<String, Function<Pair<Integer,Integer>, AbstractInterval<Boolean>>>
            atoms = setOnlineAtoms();

        return new OnlineSpaceTimeMonitor<>(f, (int) nodes, new BooleanDomain(),
                                            locSvc, atoms, distanceFunctions);
    }

    private static
    Map<String, Function<Pair<Integer,Integer>, AbstractInterval<Boolean>>>
    setOnlineAtoms()
    {
        Map<String, Function<Pair<Integer,Integer>, AbstractInterval<Boolean>>>
                atoms = new HashMap<>();
        atoms.put(COORDINATOR, x -> booleanInterval(x.getFirst() == 1));
        atoms.put(ROUTER, x -> booleanInterval(x.getFirst() == 2));
        atoms.put(END_DEVICE, x -> booleanInterval(x.getFirst() == 3));
        return atoms;
    }

    private static AbstractInterval<Boolean> booleanInterval(boolean cond) {
        return cond ? new AbstractInterval<>(true, true) :
                      new AbstractInterval<>(false, false);
    }
}
