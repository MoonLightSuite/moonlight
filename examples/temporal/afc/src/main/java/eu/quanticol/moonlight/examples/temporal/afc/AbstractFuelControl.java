package eu.quanticol.moonlight.examples.temporal.afc;


import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;
import com.mathworks.engine.MatlabEngine;
import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.online.OnlineTimeMonitor;
import eu.quanticol.moonlight.signal.online.SegmentInterface;
import eu.quanticol.moonlight.signal.online.TimeSegment;
import eu.quanticol.moonlight.signal.online.Update;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class AbstractFuelControl {
    private static final String BREACH_PATH = System.getProperty("BREACH_PATH");

    private static final String MOONLIGHT_ATOM = "bigError";
    private static double[] breachInput;
    private static double[] moonlightInput;

    public static void main(String[] args)
            throws InterruptedException, ExecutionException, URISyntaxException,
                   PythonExecutionException, IOException
    {
        MatlabEngine eng = MatlabEngine.startMatlab();

        String localPath = Paths.get(Objects.requireNonNull(
                AbstractFuelControl.class
                    .getResource("afc_breach_monitoring.m"))
                    .toURI()).getParent().toAbsolutePath().toString();

        eng.eval("addpath(\"" + BREACH_PATH + "\")");
        eng.eval("addpath(\"" + localPath + "\")");

        System.out.println("General Execution Starting at: " +
                           System.currentTimeMillis());





        List<List<SegmentInterface<Double, AbstractInterval<Double>>>>
                moonlightColl = runMoonlight(eng);

        List<SegmentInterface<Double, AbstractInterval<Double>>>
        moonlight = moonlightColl.get(moonlightColl.size() - 1);

        List<List<Double>> mRes = processMoonlight(moonlightColl);

        List<Double> mStart = mRes.get(0);

        List<Double> mEnd = mRes.get(1);

        plot(mStart, mEnd, "Moonlight");

        List<SegmentInterface<Double, AbstractInterval<Double>>>
                breach = runBreach(eng);

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

        plot(bStart, bEnd, "Breach");

        plotInput("alw_[10, 30] ((abs(AF[t]-AFRef[t]) > 0.05) => (ev_[0, 1] (abs(AF[t]-AFRef[t]) < 0.05)))");

        if(Arrays.equals(breachInput, moonlightInput))
            assertEquals(breach, moonlight);

        eng.close();
    }

    private static List<List<Double>> processMoonlight(
        List<List<SegmentInterface<Double, AbstractInterval<Double>>>> results)
    {
        List<List<Double>> r = new ArrayList<>();
        r.add(new ArrayList<>());
        r.add(new ArrayList<>());

        for(List<SegmentInterface<Double, AbstractInterval<Double>>> v: results)
        {
            r.get(0).add(v.get(0).getValue().getStart());
            r.get(1).add(v.get(0).getValue().getEnd());
        }

        return r;
    }

    private static void plot(List<Double> dataDown, List<Double> dataUp, String name)
            throws PythonExecutionException, IOException
    {
        dataDown = filterValues(dataDown);
        dataUp = filterValues(dataUp);
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
    }

    private static void plotInput(String name)
            throws PythonExecutionException, IOException
    {
        Plot plt = Plot.create();
        plt.plot()
                .add(Arrays.stream(breachInput)
                           .boxed().collect(Collectors.toList()))
                .label("input");
        plt.xlabel("times");
        plt.ylabel("|AF-AFRef|");
        //plt.text(1, 0.5, "text");
        plt.title(name);
        plt.legend();
        plt.show();
    }

    private static List<Double> filterValues(List<Double> vs) {
        vs = vs.stream().map(v -> v.equals(Double.POSITIVE_INFINITY) ?  1 : v)
                        .map(v -> v.equals(Double.NEGATIVE_INFINITY) ? -1 : v)
                        .map(v -> v.equals(20.0) ? 1 : v)
                        .collect(Collectors.toList());
        return vs;
    }



    private static List<SegmentInterface<Double, AbstractInterval<Double>>>
    runBreach(MatlabEngine eng) throws ExecutionException,
                                       InterruptedException
    {
        long before = System.currentTimeMillis();

        eng.eval("afc_breach_monitoring");

        long after = System.currentTimeMillis();

        System.out.println("Breach Execution Time (sec): " +
                            (after - before) / 1000.);

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
    runMoonlight(MatlabEngine eng) throws ExecutionException,
                                          InterruptedException
    {
        long before = System.currentTimeMillis();

        eng.eval("afc_moonlight_monitoring");

        double[] input = eng.getVariable("input");
        moonlightInput = input;

        List<Update<Double, Double>> updates = genUpdates(input);
        OnlineTimeMonitor<Double, Double> m = instrument();

        List<List<SegmentInterface<Double, AbstractInterval<Double>>>>
                result = new ArrayList<>();

        for(Update<Double, Double> u: updates) {
            //System.out.println(u.toString());
            result.add(new ArrayList<>(m.monitor(u).getSegments()));
        }

        long after = System.currentTimeMillis();

        System.out.println("Moonlight Execution Time (sec): " +
                            (after - before) / 1000.);

        return result;
    }

    private static OnlineTimeMonitor<Double, Double> instrument()
    {
        Formula f = new GloballyFormula(
                new OrFormula(
                        new NegationFormula(new AtomicFormula(MOONLIGHT_ATOM)),
                        new EventuallyFormula(
                                new NegationFormula(
                                new AtomicFormula(MOONLIGHT_ATOM))
                                ,  new Interval(0.0, 1.0))
                ),
                new Interval(10.0, 30.0));
//        f = new OrFormula(
//                new NegationFormula(new AtomicFormula(MOONLIGHT_ATOM)),
//                new EventuallyFormula(
//                        new NegationFormula(
//                                new AtomicFormula(MOONLIGHT_ATOM))
//                        ,  new Interval(0.0, 1.0))
//        );
//        f = new EventuallyFormula(
//                new NegationFormula(
//                        new AtomicFormula(MOONLIGHT_ATOM))
//                ,  new Interval(0.0, 1.0));
//
//        f = new NegationFormula(new AtomicFormula(MOONLIGHT_ATOM));

        // alw_[10, 30] ((abs(AF[t]-AFRef[t]) > 0.05) => (ev_[0, 1] (abs(AF[t]-AFRef[t]) < 0.05)))

        HashMap<String, Function<Double, AbstractInterval<Double>>>
                atoms = new HashMap<>();

        atoms.put(MOONLIGHT_ATOM,
                trc -> new AbstractInterval<>(trc - 0.05,
                        trc - 0.05));

        return new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
    }

    private static List<Update<Double, Double>> genUpdates(double[] values) {
        List<Update<Double, Double>> updates = new ArrayList<>();
        double di = 0;
        for(int i = 0; i < values.length; i++) {
            double vi = Math.round(di * 100.0)/100.0;
            double vj = Math.round((di + 0.1) * 100.0)/100.0;
                updates.add(new Update<>(vi, vj, values[i]));

            di = Math.round((i + 1) * 0.1 * 100.0)/100.0;
        }
        return updates;
    }

    private static List<SegmentInterface<Double, AbstractInterval<Double>>>
    condenseSignal(
            List<SegmentInterface<Double, AbstractInterval<Double>>> ss)
    {
        List<SegmentInterface<Double, AbstractInterval<Double>>> out =
                new ArrayList<>();
        SegmentInterface<Double, AbstractInterval<Double>> bound = ss.get(0);
        out.add(ss.get(0));

        for(SegmentInterface<Double, AbstractInterval<Double>> curr: ss) {
            if(!curr.getValue().equals(bound.getValue())) {
                bound = curr;
                out.add(curr);
            }
        }

        return out;
    }
}
