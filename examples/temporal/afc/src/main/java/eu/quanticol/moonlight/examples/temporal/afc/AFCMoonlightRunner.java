package eu.quanticol.moonlight.examples.temporal.afc;


import com.mathworks.engine.MatlabEngine;
import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.io.DataReader;
import eu.quanticol.moonlight.io.FileType;
import eu.quanticol.moonlight.io.parsing.ParsingStrategy;
import eu.quanticol.moonlight.io.parsing.RawTrajectoryExtractor;
import eu.quanticol.moonlight.monitoring.online.OnlineTimeMonitor;
import eu.quanticol.moonlight.signal.online.SegmentInterface;
import eu.quanticol.moonlight.signal.online.Update;
import eu.quanticol.moonlight.util.Plotter;
import eu.quanticol.moonlight.util.Stopwatch;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class AFCMoonlightRunner {
    private static final String MOONLIGHT_ATOM = "bigError";
    private static final int ITERATIONS = 1;

    private static final List<String> output = new ArrayList<>();
    private static final List<Stopwatch> stopwatches = new ArrayList<>();

    private static final boolean PLOTTING = false;

    private static final Plotter plt = new Plotter();

    public static void main(String[] args) {
//        repeatedRunner("In-Order 20", () -> runMoonlight(false, "20"));
//        repeatedRunner("In-Order 50", () -> runMoonlight(false, "50"));
//        repeatedRunner("In-Order 100", () -> runMoonlight(false, "100"));
        repeatedRunner("In-Order 200", () -> runMoonlight(false, "200"));

        //repeatedRunner("Out-Of-Order M", () -> runMoonlight(true));

        System.out.println("------> Experiment results (sec):");
        for (String s : output) {
            System.out.println(s);
        }
    }

    private static void repeatedRunner(String title, Runnable task) {
        double tot = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            task.run();
            tot += stopwatches.get(i).getDuration();
        }
        tot = (tot / ITERATIONS) / 1000.; // Converted to seconds

        output.add(title + " Execution time (avg over 100):" + tot);

        stopwatches.clear();
    }

    private static void runMoonlight(boolean shuffle, String id) {
        MatlabEngine eng = matlabInit();
        try {
            List<List<SegmentInterface<Double, AbstractInterval<Double>>>>
                    moonlightColl = executeMoonlight(eng, shuffle, id);
            List<SegmentInterface<Double, AbstractInterval<Double>>>
                    moonlight = moonlightColl.get(moonlightColl.size() - 1);

            List<List<Double>> mRes = processMoonlight(moonlightColl);

            eng.close();

            List<Double> mStart = mRes.get(0);
            List<Double> mEnd = mRes.get(1);

            if (PLOTTING)
                plt.plot(mStart, mEnd, "Moonlight");

        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static MatlabEngine matlabInit() {
        try {
            MatlabEngine eng = MatlabEngine.startMatlab();
            String localPath = Paths.get(Objects.requireNonNull(
                    AFCMoonlightRunner.class
                            .getResource("matlab/afc_breach_monitoring.m"))
                    .toURI()).getParent().toAbsolutePath().toString();

            eng.eval("addpath(\"" + localPath + "\")");

            return eng;
        } catch (URISyntaxException | ExecutionException |
                InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();

            return null;
        }
    }

    private static List<List<Double>> processMoonlight(
            List<List<SegmentInterface<Double, AbstractInterval<Double>>>> results) {
        List<List<Double>> r = new ArrayList<>();
        r.add(new ArrayList<>());
        r.add(new ArrayList<>());

        for (List<SegmentInterface<Double, AbstractInterval<Double>>> v : results) {
            r.get(0).add(v.get(0).getValue().getStart());
            r.get(1).add(v.get(0).getValue().getEnd());
        }

        return r;
    }

    private static List<List<SegmentInterface<Double, AbstractInterval<Double>>>>
    executeMoonlight(MatlabEngine eng, boolean shuffle, String id)
    {
        ParsingStrategy<double[][]> str = new RawTrajectoryExtractor(1);
        double[][] input = new DataReader<>(data_path(id), FileType.CSV, str).read();

        // Adapting Matlab variable for Moonlight Online
        List<Update<Double, Double>> updates = genUpdates(input[0], shuffle, 0.1);
        OnlineTimeMonitor<Double, Double> m = instrument();

        List<List<SegmentInterface<Double, AbstractInterval<Double>>>>
                result = new ArrayList<>();

        // Moonlight execution recording...
        Stopwatch rec = Stopwatch.start();
        for (Update<Double, Double> u : updates) {
            //System.out.println(u.toString());
            result.add(new ArrayList<>(m.monitor(u).getSegments().toList()));
        }
        rec.stop();
        stopwatches.add(rec);

        return result;
    }

    private static OnlineTimeMonitor<Double, Double> instrument() {
        Formula f = new GloballyFormula(
                new OrFormula(
                        new NegationFormula(new AtomicFormula(MOONLIGHT_ATOM)),
                        new EventuallyFormula(
                                new NegationFormula(
                                        new AtomicFormula(MOONLIGHT_ATOM))
                                , new Interval(0.0, 1.0))
                ),
                new Interval(10.0, 30.0));

        // alw_[10, 30] ((abs(AF[t]-AFRef[t]) > 0.05) =>
        //               (ev_[0, 1] (abs(AF[t]-AFRef[t]) < 0.05)))

        HashMap<String, Function<Double, AbstractInterval<Double>>>
                atoms = new HashMap<>();

        atoms.put(MOONLIGHT_ATOM,
                trc -> new AbstractInterval<>(trc - 0.05,
                        trc - 0.05));

        return new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
    }

    private static List<Update<Double, Double>> genUpdates(
            double[] values, boolean shuffle, double scale)
    {
        List<Update<Double, Double>> updates = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            double vi = i * scale;
            double vj = (i + 1) * scale;
            double vv = values[i];
            updates.add(new Update<>(vi, vj, vv));
        }

        if (shuffle)
            Collections.shuffle(updates, new Random(1));

        return updates;
    }

    private static List<SegmentInterface<Double, AbstractInterval<Double>>>
    condenseSignal(List<SegmentInterface<Double, AbstractInterval<Double>>> ss)
    {
        List<SegmentInterface<Double, AbstractInterval<Double>>> out =
                new ArrayList<>();
        SegmentInterface<Double, AbstractInterval<Double>> bound = ss.get(0);
        out.add(ss.get(0));

        for (SegmentInterface<Double, AbstractInterval<Double>> curr : ss) {
            if (!curr.getValue().equals(bound.getValue())) {
                bound = curr;
                out.add(curr);
            }
        }

        return out;
    }

    private static InputStream data_path (String id) {
        return AFCSimulatorRunner.class
                .getResourceAsStream("data/afc_sim_" + id + ".csv");
    }
}
