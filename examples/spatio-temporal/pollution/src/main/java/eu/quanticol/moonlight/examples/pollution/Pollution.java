package eu.quanticol.moonlight.examples.pollution;

import eu.quanticol.moonlight.core.base.Box;
import eu.quanticol.moonlight.core.formula.Formula;
import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.signal.TimeSignal;
import eu.quanticol.moonlight.core.space.DefaultDistanceStructure;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.domain.*;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.formula.spatial.SomewhereFormula;
import eu.quanticol.moonlight.formula.temporal.EventuallyFormula;
import eu.quanticol.moonlight.io.DataReader;
import eu.quanticol.moonlight.io.DataWriter;
import eu.quanticol.moonlight.io.parsing.FileType;
import eu.quanticol.moonlight.io.parsing.AdjacencyExtractor;
import eu.quanticol.moonlight.io.parsing.ParsingStrategy;
import eu.quanticol.moonlight.io.parsing.PrintingStrategy;
import eu.quanticol.moonlight.io.parsing.RawTrajectoryExtractor;
import eu.quanticol.moonlight.online.monitoring.OnlineSpatialTemporalMonitor;
import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.online.signal.Update;
import eu.quanticol.moonlight.space.StaticLocationService;
import eu.quanticol.moonlight.util.Plotter;
import eu.quanticol.moonlight.util.Stopwatch;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Pollution {
    private static final Logger LOG = Logger.getLogger(Pollution.class.getName());
    private static final String SPACE_FILE = "lombardy_dist.csv";
    private static final String SIGNAL_FILE = "lombardy_no2.csv";

    private static final String NOT_CRITICAL_NO2 = "NO2 < K";
    private static final String CRITICAL_NO2 = "NO2 > K";
    private static final String DISTANCE = "nearby";
    private static final double K = 50;


    private static LocationService<Double, Double> ls;
    private static int size;

    private static final double MAX_VALUE = 100.0;


    private static final Plotter plt = new Plotter(MAX_VALUE);

    private static double km(double meters) {
        return meters * 1000;
    }



    public static void main(String[] argv) {
        LOG.setLevel(Level.ALL);

        SpatialModel<Double> space = loadSpatialModel();
        size = space.size();
        ls = new StaticLocationService<>(space);
        LOG.info(() -> "The space model has: " + size + " nodes.");
        TimeChain<Double, List<Double>> updates = importUpdates(space.size());
        LOG.info("Signal loaded correctly!");

        execute("F1", formula1(), updates, false);

        execute("F2", formula2(), updates, false);
    }

    private static SpatialModel<Double> loadSpatialModel() {
        InputStream file = Pollution.class.getResourceAsStream(SPACE_FILE);
        ParsingStrategy<SpatialModel<Double>> ex = new AdjacencyExtractor();
        DataReader<SpatialModel<Double>> data = new DataReader<>(file,
                                                                 FileType.CSV,
                                                                 ex);
        return data.read();
    }

    private static void execute(String name,
                                Formula f,
                                TimeChain<Double, List<Double>> updates,
                                boolean parallel)
    {
        SignalDomain<Double> d = new DoubleDomain();
        OnlineSpatialTemporalMonitor<Double, Double, Double> m =
                new OnlineSpatialTemporalMonitor<>(f, size, d,
                        ls, atoms(), dist(), parallel);

        TimeSignal<Double, List<Box<Double>>> s = null;

        Stopwatch rec = Stopwatch.start();

        //plt.plotOne(updates, name, 51, "input");

//        List<Update<Double, List<Double>>> ups = updates.toUpdates();
//        for(Update<Double, List<Double>> u: ups)
//            s = m.monitor(u);
        s = m.monitor(updates);
        rec.stop();

        LOG.info("Execution Time of Monitor " + name +
                ": " + rec.getDuration() + "ms");
        plt.plotOne(s.getSegments(), name, 51);
        //plt.plotAll(s.getSegments(), name);
        //LOG.info("Monitoring result of " + name + ": " + s.getSegments());

        storeResults(s.getSegments(), name);
    }

    private static void storeResults(
            TimeChain<Double, List<Box<Double>>> data,
            String name)
    {
        PrintingStrategy<double[][]> st = new RawTrajectoryExtractor(size);
        List<Update<Double, List<Box<Double>>>> trace = data.toUpdates();
        int times = trace.size();
        int locations = trace.get(0).getValue().size();
        double[][] resultUp = new double[times][locations];
        double[][] resultDown = new double[times][locations];

        for(int t = 0; t < times; t++) {
            for( int l = 0; l < locations; l++) {
                Box<Double> value = trace.get(t).getValue().get(l);
                double valueUp = flattenInfinity(value.getEnd());
                double valueDown = flattenInfinity(value.getStart());
                resultUp[t][l] = valueUp;
                resultDown[t][l] = valueDown;
            }
        }
        resultUp = transposeMatrix(resultUp);
        resultDown = transposeMatrix(resultDown);

        LOG.info("Saving output in: " + name);
        new DataWriter<>(name + "_up.csv", FileType.CSV, st).write(resultUp);
        new DataWriter<>(name + "_down.csv", FileType.CSV, st).write(resultDown);
    }

    private static double flattenInfinity(Double value) {
        if(value.equals(Double.POSITIVE_INFINITY))
            value = MAX_VALUE;
        else if(value.equals(Double.NEGATIVE_INFINITY)) {
            value = - MAX_VALUE;
        }
//        LOG.warning("Infinite value detected, substituting it with "
//                    + fallback);
        return value;
    }

    private static TimeChain<Double, List<Double>> importUpdates(int size) {
        ParsingStrategy<double[][]> ex = new RawTrajectoryExtractor(size);
        InputStream file = Pollution.class.getResourceAsStream(SIGNAL_FILE);
        double[][] trace = new DataReader<>(file, FileType.CSV, ex).read();
        trace = transposeMatrix(trace);

        List<Update<Double, List<Double>>> updates = new ArrayList<>();
        for(int i = 0; i < trace.length; i++) {
            updates.add(new Update<>((double) i,
                                (double) i + 1,
                                     Arrays.stream(trace[i])
                                           .map(n -> n == -20.0 ?
                                                   Double.NEGATIVE_INFINITY : n)
                                           .boxed()
                                           .collect(Collectors.toList())));
        }

        return Update.asTimeChain(updates);
    }

    private static
    Map<String, Function<SpatialModel<Double>, DistanceStructure<Double, ?>>>
    dist()
    {
        Map<String,
            Function<SpatialModel<Double>,
                    DistanceStructure<Double, ?>>> ds = new HashMap<>();
        ds.put(DISTANCE,  g -> distance(0.0, km(10)).apply(g));
        return ds;
    }

    private static Formula formula1() {
        Formula atomX = new AtomicFormula(NOT_CRITICAL_NO2);

        return new EventuallyFormula(atomX, new Interval(0, 3));
    }

    private static Formula formula2() {
        Formula atomY = new AtomicFormula(NOT_CRITICAL_NO2);

        return new SomewhereFormula(DISTANCE, atomY);
    }

    private static
    HashMap<String, Function<Double, Box<Double>>> atoms()
    {
        HashMap<String, Function<Double, Box<Double>>>
                atoms = new HashMap<>();

        // criticalNO2 is the atomic proposition: NO2 > k
        atoms.put(CRITICAL_NO2, trc -> {
            Box<Double> v = doubleToInterval(trc);
            return new Box<>(v.getStart() - K, v.getEnd() - K);
        });

        // notCriticalNO2 is the atomic proposition: NO2 < k
        atoms.put(NOT_CRITICAL_NO2, trc -> {
            Box<Double> v = doubleToInterval(trc);
            return new Box<>(K - v.getEnd(), K - v.getStart());
        });

        return atoms;
    }

    private static Box<Double> doubleToInterval(Double value) {
        // We add the artificial offset of +20 and then we can safely
        // add the [-15, +15] offset, so that we don't have degenerate
        // traces having values < 0 (which would be unfeasible observations)
        double fixedValue = value + 20;
        if(value.equals(Double.NEGATIVE_INFINITY))
            return new Box<>(Double.NEGATIVE_INFINITY,
                                          Double.POSITIVE_INFINITY);

        return new Box<>(fixedValue - 15, fixedValue + 15);
    }

    public static double[][] transposeMatrix(double [][] m){
        double[][] temp = new double[m[0].length][m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp[j][i] = m[i][j];
        return temp;
    }


    static Function<SpatialModel<Double>, DistanceStructure<Double, Double>>
    distance(double lowerBound, double upperBound)
    {
        return g -> new DefaultDistanceStructure<>(x -> x,
                                            new DoubleDomain(),
                                            lowerBound, upperBound,
                                            g);
    }
}
