package eu.quanticol.moonlight.examples.pollution;

import eu.quanticol.moonlight.domain.*;
import eu.quanticol.moonlight.io.DataReader;
import eu.quanticol.moonlight.io.FileType;
import eu.quanticol.moonlight.io.parsing.AdjacencyExtractor;
import eu.quanticol.moonlight.io.parsing.ParsingStrategy;
import eu.quanticol.moonlight.io.parsing.RawTrajectoryExtractor;
import eu.quanticol.moonlight.formula.AtomicFormula;
import eu.quanticol.moonlight.formula.Formula;
import eu.quanticol.moonlight.formula.GloballyFormula;
import eu.quanticol.moonlight.formula.SomewhereFormula;
import eu.quanticol.moonlight.monitoring.online.OnlineSpaceTimeMonitor;
import eu.quanticol.moonlight.signal.online.OnlineSpaceTimeSignal;
import eu.quanticol.moonlight.signal.online.TimeSignal;
import eu.quanticol.moonlight.signal.online.Update;
import eu.quanticol.moonlight.space.DistanceStructure;
import eu.quanticol.moonlight.space.LocationService;
import eu.quanticol.moonlight.space.SpatialModel;
import eu.quanticol.moonlight.space.StaticLocationService;

import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;
import eu.quanticol.moonlight.util.Plotter;
import eu.quanticol.moonlight.util.Stopwatch;

import java.io.IOException;
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

    private static final String NOT_CRITICAL_NO2 = "notCriticalNO2";


    private static final Plotter plt = new Plotter();

    private static double km(double meters) {
        return meters * 1000;
    }

    public static void main(String[] argv) {
        LOG.setLevel(Level.ALL);
        InputStream file = Pollution.class.getResourceAsStream(SPACE_FILE);
        ParsingStrategy<SpatialModel<Double>> ex = new AdjacencyExtractor();
        DataReader<SpatialModel<Double>> data = new DataReader<>(file,
                                                                 FileType.CSV,
                                                                 ex);
        SpatialModel<Double> space = data.read();

        LocationService<Double, Double> ls = new StaticLocationService<>(space);
        SignalDomain<Double> d = new DoubleDomain();

        LOG.info(() -> "The space model has: " + space.size() + " nodes.");


        List<Update<Double, List<Double>>> updates = importUpdates(space.size());

        LOG.info("Signal loaded correctly!");


        OnlineSpaceTimeMonitor<Double, Double, Double> m =
                new OnlineSpaceTimeMonitor<>(formula1(), space.size(), d,
                        ls, atoms(), dist());

        TimeSignal<Double, List<AbstractInterval<Double>>> s =
                                new OnlineSpaceTimeSignal<>(space.size(), d);

        //updates = updates.subList(0, 2000);

        for(Update<Double, List<Double>> u : updates) {
            s = m.monitor(u);
            //LOG.info(() -> "Monitoring for " + u + " completed!");
        }

        List<Double> rhoUp = s.getSegments().stream()
                              .map(i -> i.getValue().get(0).getEnd())
                              .collect(Collectors.toList());
        List<Double> rhoDown = s.getSegments().stream()
                                .map(i -> i.getValue().get(0).getEnd())
                                .collect(Collectors.toList());
        rhoDown = new ArrayList<>();
        rhoDown.add(1.0);
        rhoUp = new ArrayList<>();
        rhoUp.add(1.0);
        plt.plot(rhoUp, rhoDown, "F1");

        final TimeSignal<Double, List<AbstractInterval<Double>>> output = s;
        LOG.info(() -> "Monitoring result of F1: " + output.getSegments());


        Stopwatch rec = Stopwatch.start();
        OnlineSpaceTimeMonitor<Double, Double, Double> m2 =
                new OnlineSpaceTimeMonitor<>(formula2(), space.size(), d,
                                         ls, atoms(), dist());

        for(Update<Double, List<Double>> u : updates) {
            s = m2.monitor(u);
            //LOG.info(() -> "Monitoring for " + u + " completed!");
        }
        rec.stop();
        System.out.println("F2 Sequential time: " + rec.getDuration());

        rec = Stopwatch.start();
        OnlineSpaceTimeMonitor<Double, Double, Double> m3 =
                new OnlineSpaceTimeMonitor<>(formula2(), space.size(), d,
                        ls, atoms(), dist(), true);

        for(Update<Double, List<Double>> u : updates) {
            s = m3.monitor(u);
            //LOG.info(() -> "Monitoring for " + u + " completed!");
        }
        rec.stop();
        System.out.println("F2 Parallel time: " + rec.getDuration());

        rec = Stopwatch.start();
        OnlineSpaceTimeMonitor<Double, Double, Double> m4 =
                new OnlineSpaceTimeMonitor<>(formula2(), space.size(), d,
                        ls, atoms(), dist(), true);

        for(Update<Double, List<Double>> u : updates) {
            s = m4.monitor(u);
            //LOG.info(() -> "Monitoring for " + u + " completed!");
        }
        rec.stop();
        System.out.println("F2 Parallel time#2: " + rec.getDuration());


        rhoUp = s.getSegments().stream()
                .map(i -> i.getValue().get(0).getEnd())
                .collect(Collectors.toList());
        rhoDown = s.getSegments().stream()
                .map(i -> i.getValue().get(0).getEnd())
                .collect(Collectors.toList());
        //plot(rhoDown, rhoUp, "F2");

        final TimeSignal<Double, List<AbstractInterval<Double>>> output2 = s;
        //LOG.info(() -> "Monitoring result of F2: " + output2.getSegments());

    }

    private static List<Update<Double, List<Double>>> importUpdates(int size) {
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

        return updates;
    }

    private static
    Map<String, Function<SpatialModel<Double>, DistanceStructure<Double, ?>>>
    dist()
    {
        Map<String,
            Function<SpatialModel<Double>,
            DistanceStructure<Double, ?>>> ds = new HashMap<>();
        ds.put("nearby",  g -> distance(0.0, km(10)).apply(g));
        return ds;
    }

    private static Formula formula1() {
        Formula atomX = new AtomicFormula(NOT_CRITICAL_NO2);

        return new GloballyFormula(atomX, new Interval(0, 3));
    }

    private static Formula formula2() {
        Formula atomX = new AtomicFormula(NOT_CRITICAL_NO2);

        return new SomewhereFormula("nearby", atomX);
    }

    private static void plot(List<Double> dataDown, List<Double> dataUp, String name)
    {
        try {
            Plot plt = Plot.create();
            plt.plot()
                    .add(dataUp)
                    .label("rho_up");
            plt.plot()
                    .add(dataDown)
                    .label("rho_down");
//        plt.plot()
//                .add(Arrays.stream(breachInput)
//                           .boxed().collect(Collectors.toList()))
//                .label("input");
            plt.xlabel("times");
            plt.ylabel("robustness");
            //plt.text(1, 0.5, "text");
            plt.title(name);
            plt.legend();
            plt.show();
        } catch (PythonExecutionException| IOException e) {
            System.err.println("unable to plot!");
            e.printStackTrace();
        }
    }

    private static
    HashMap<String, Function<Double, AbstractInterval<Double>>> atoms()
    {
        HashMap<String, Function<Double, AbstractInterval<Double>>>
                atoms = new HashMap<>();

        // notCriticalNO2 is the atomic proposition: NO2 < k
        atoms.put(NOT_CRITICAL_NO2, trc -> new AbstractInterval<>(trc, trc));

        return atoms;
    }

    public static double[][] transposeMatrix(double [][] m){
        double[][] temp = new double[m[0].length][m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp[j][i] = m[i][j];
        return temp;
    }


    public static Function<SpatialModel<Double>, DistanceStructure<Double, Double>> distance(double lowerBound, double upperBound) {
        return g -> new DistanceStructure<>(x -> x, new DoubleDistance(), lowerBound, upperBound, g);
    }
}
