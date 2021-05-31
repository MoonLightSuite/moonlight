package eu.quanticol.moonlight.examples.temporal.afc;


import com.mathworks.engine.MatlabEngine;
import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.online.OnlineTimeMonitor;
import eu.quanticol.moonlight.signal.online.SegmentInterface;
import eu.quanticol.moonlight.signal.online.TimeSegment;
import eu.quanticol.moonlight.signal.online.Update;
import eu.quanticol.moonlight.util.Plotter;
import eu.quanticol.moonlight.util.Stopwatch;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AbstractFuelControl {
    //TODO: set environment variable or replace with path to breach
    private static final String BREACH_PATH = System.getProperty("BREACH_PATH");

    private static final String MOONLIGHT_ATOM = "bigError";
    private static double[] breachInput;
    private static double[] moonlightInput;

    private static final List<String> output = new ArrayList<>();

    private static final List<Stopwatch> stopwatches = new ArrayList<>();

    private static final boolean PLOTTING = false;

    private static final Plotter plt = new Plotter();

    public static void main(String[] args) {
        repeatedRunner("In-Order M", () -> runMoonlight(false));

        //repeatedRunner("Out-Of-Order M", () -> runMoonlight(true));

        repeatedRunner("Breach", AbstractFuelControl::runBreach);


        System.out.println("------> Experiment results (sec):");

        for (int i = 0; i < output.size(); i++) {
            System.out.println(output.get(i));
        }

        if(PLOTTING)
            plt.plot(Arrays.stream(breachInput)
                    .boxed().collect(Collectors.toList()),
                    "alw_[10, 30] ((abs(AF[t]-AFRef[t]) > 0.05) => " +
                            "(ev_[0, 1] (abs(AF[t]-AFRef[t]) < 0.05)))",
                    "|AF-AFRef|");

    }

    private static void repeatedRunner(String title, Runnable task) {
        final int iterations = 1;
        double tot = 0;
        for (int i = 0; i < iterations; i++) {
            task.run();
            tot += stopwatches.get(i).getDuration();
        }

        tot = (tot / iterations) / 1000.;

        output.add(title + " Execution time (avg over 100):" + tot);

        stopwatches.clear();
    }

    private static void runBreach() {
        MatlabEngine eng = matlabInit();
        try {
            //eng.putVariable("max_sim", 50);
            List<SegmentInterface<Double, AbstractInterval<Double>>>
                    breach = executeBreach(eng);

            List<Double> bStart = IntStream.range(0, breach.size())
                    .boxed()
                    .map(i -> breach.get(i)
                            .getValue()
                            .getStart())
                    .collect(Collectors.toList());

            List<Double> bEnd = IntStream.range(0, breach.size())
                    .boxed()
                    .map(i -> breach.get(i)
                            .getValue()
                            .getEnd())
                    .collect(Collectors.toList());
            eng.close();

            if (PLOTTING)
                plt.plot(bStart, bEnd, "Breach");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void runMoonlight(boolean shuffle) {
        MatlabEngine eng = matlabInit();
        try {
            List<List<SegmentInterface<Double, AbstractInterval<Double>>>>
                    moonlightColl = executeMoonlight(eng, shuffle);
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
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private static MatlabEngine matlabInit() {
        try {
            MatlabEngine eng = MatlabEngine.startMatlab();

            eng = null;
            eng.putVariable(" test",2);
            String localPath = Paths.get(Objects.requireNonNull(
                    AbstractFuelControl.class
                            .getResource("afc_breach_monitoring.m"))
                    .toURI()).getParent().toAbsolutePath().toString();

            eng.eval("addpath(\"" + BREACH_PATH + "\")");
            eng.eval("addpath(\"" + localPath + "\")");

            return eng;
        } catch (URISyntaxException | ExecutionException |
                InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

        return null;
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

    private static List<SegmentInterface<Double, AbstractInterval<Double>>>
    executeBreach(MatlabEngine eng) throws ExecutionException,
            InterruptedException
    {
        Stopwatch rec = Stopwatch.start();
        eng.eval("afc_breach_monitoring");
        long duration = rec.stop();
        stopwatches.add(rec);

        System.out.println("Breach Execution Time (sec): " +
                duration / 1000.);

        double[] rhoLow = eng.getVariable("rho_low");
        double[] rhoUp = eng.getVariable("rho_up");
        breachInput = eng.getVariable("input");

        List<SegmentInterface<Double, AbstractInterval<Double>>> output =
                IntStream.range(0, rhoLow.length).boxed()
                        .map(i -> (SegmentInterface<Double,
                                AbstractInterval<Double>>)
                                new TimeSegment<>((double) i,
                                        new AbstractInterval<>(rhoLow[i],
                                                rhoUp[i])))
                        .collect(Collectors.toList());

        //return condenseSignal(output);
        return output;
    }

    private static List<List<SegmentInterface<Double, AbstractInterval<Double>>>>
    executeMoonlight(MatlabEngine eng, boolean shuffle)
            throws ExecutionException, InterruptedException
    {
        // Model execution recording....
        Stopwatch rec = Stopwatch.start();
        eng.eval("afc_moonlight_monitoring");
        long duration = rec.stop();
        //stopwatches.add(rec);
        output.add("Symulink Model execution time: " + duration / 1000.);

        double[] input = eng.getVariable("input");
        moonlightInput = input;

        // Adapting Matlab variable for Moonlight Online
        List<Update<Double, Double>> updates = genUpdates(input, shuffle, 0.1);
        OnlineTimeMonitor<Double, Double> m = instrument();

        List<List<SegmentInterface<Double, AbstractInterval<Double>>>>
                result = new ArrayList<>();

        // Moonlight execution recording...
        rec = Stopwatch.start();
        for (Update<Double, Double> u : updates) {
            //System.out.println(u.toString());
            result.add(new ArrayList<>(m.monitor(u).getSegments()));
        }
        duration = rec.stop();
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
}
