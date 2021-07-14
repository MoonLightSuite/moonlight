package eu.quanticol.moonlight.examples.sensors;

import eu.quanticol.moonlight.domain.*;
import eu.quanticol.moonlight.formula.*;
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
import eu.quanticol.moonlight.util.Plotter;
import eu.quanticol.moonlight.util.Stopwatch;
import eu.quanticol.moonlight.util.Utils;
import eu.quanticol.moonlight.utility.matlab.MatlabRunner;
import eu.quanticol.moonlight.utility.matlab.configurator.MatlabDataConverter;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Note: requires Matlab R2020a+ & Machine Learning toolbox plugin
 * in running environment
 */
public class SensorsOnline {
    // Experiment observation settings
    private static final int RND_SEED = 17;
    private static final Plotter plt = new Plotter(10.0);
    private static final int ITERATIONS = 100;
    private static final List<Stopwatch> stopwatches = new ArrayList<>();
    private static final Logger LOG = Logger.getLogger("SensorsOnline");
    private static final List<String> output = new ArrayList<>();

    // Matlab settings
    private static final String MATLAB_SCRIPT = "sensorNetworkExample";
    private static final String LOCAL_PATH = getLocalPath();


    private static Map<String,
                       Function<Parameters,
                                Function<Pair<Integer,Double>, Boolean>>>
                                                                 atomicFormulas;
    private static HashMap<String, Function<SpatialModel<Double>,
                               DistanceStructure<Double, ?>>> distanceFunctions;
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

    private static final double nodes = 10.0;
    private static Double[] nodesType;
    private static double[] temperature;

    public static void main(String[] args) {
        MatlabRunner matlab = new MatlabRunner(LOCAL_PATH);
        Object[] graph = Objects.requireNonNull(runSimulator(matlab));
        locSvc = Utils.createLocServiceFromSetMatrix(graph);
        setAtomicFormulas();
        setDistanceFunctions();

        Formula f1 = formula1();
        repeatedRunner("F1 offline", () -> checkOffline(f1));
        repeatedRunner("F1 online IO", () -> checkOnline(f1, false));
        repeatedRunner("F1 online OOO", () -> checkOnline(f1, true));

        Formula f2 = formula2();
        repeatedRunner("F2 offline", () -> checkOffline(f2));
        repeatedRunner("F2 online IO", () -> checkOnline(f2, false));
        repeatedRunner("F2 online OOO", () -> checkOnline(f2, true));

//        checkOffline(f2);
//        checkOnline(f2, false);
//        checkOnline(f2, true);

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

    private static Formula formula() {
        Formula isType1 = new AtomicFormula(COORDINATOR);
        return new SomewhereFormula(DISTANCE, isType1);
    }

    private static void setDistanceFunctions() {
        distanceFunctions = new HashMap<>();
        distanceFunctions.put(DISTANCE,
                m -> new DistanceStructure<>(x -> x , new DoubleDistance(),
                        0.0, 10.0, m));
    }

    private static void setAtomicFormulas() {
        atomicFormulas = new HashMap<>();
        atomicFormulas.put(COORDINATOR, p -> (x -> x.getFirst() == 1));
        atomicFormulas.put(ROUTER, p -> (x -> x.getFirst() == 2));
        atomicFormulas.put(END_DEVICE, p -> (x -> x.getFirst() == 3));

        atomicFormulas.put(HIGH_HUMIDITY, p -> (x -> x.getSecond() >= HIGH_H));
        atomicFormulas.put(NORMAL_HUMIDITY, p -> (x -> x.getSecond() < OK_H));
    }

    private static Object[] runSimulator(MatlabRunner matlab)
    {
        /// Trace generation
        matlab.eval(MATLAB_SCRIPT);


        // Reading results
        //nodes = matlab.getVar("num_nodes");
        temperature = matlab.getVar("temperature");
        //Object data = matlab.getVar("inputValues");
        nodesType = MatlabDataConverter.getArray(matlab.getVar("nodes_type"),
                                                 Double.class);

        //TODO hack, please rewrite
        MatlabRunner matlab2 = new MatlabRunner(LOCAL_PATH);
        matlab2.eval("mobility");
        return matlab2.getVar("cgraph1");

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
                new SpatialTemporalSignal<>((int) nodes);

        IntStream.range(0, (int) nodes - 1)
                .forEach(time -> stSignal
                        .add(time, (location ->
                                        new Pair<>(nodesType[location]
                                                        .intValue(),
                                                   temperature[location])
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
                new TimeChain<>(nodes - 1);
        result.add(chain);

        IntStream.range(0, (int) nodes - 1)
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
        return IntStream.range(0, (int) nodes).boxed().map(location ->
                new Pair<>(nodesType[location].intValue(), temperature[location])
        ).collect(Collectors.toList());
    }

    private static void checkOnline(Formula f, boolean shuffle)
    {


        OnlineSpaceTimeMonitor<Double, Pair<Integer, Double>, Double> m =
                onlineMonitorInit(f);

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
    onlineMonitorInit(Formula f)
    {
        Map<String, Function<Pair<Integer,Double>, AbstractInterval<Double>>>
            atoms = setOnlineAtoms();

        return new OnlineSpaceTimeMonitor<>(f, (int) nodes, new DoubleDomain(),
                                            locSvc, atoms, distanceFunctions);
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
