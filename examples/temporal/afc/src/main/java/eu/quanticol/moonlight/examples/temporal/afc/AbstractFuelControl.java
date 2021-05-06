package eu.quanticol.moonlight.examples.temporal.afc;


import com.mathworks.engine.MatlabEngine;
import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.io.FormulaToBreach;
import eu.quanticol.moonlight.monitoring.online.OnlineTimeMonitor;
import eu.quanticol.moonlight.signal.online.SegmentInterface;
import eu.quanticol.moonlight.signal.online.TimeSegment;
import eu.quanticol.moonlight.signal.online.TimeSignal;
import eu.quanticol.moonlight.signal.online.Update;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class AbstractFuelControl {
    private static final FormulaToBreach converter = new FormulaToBreach();
    private static final String BREACH_PATH =
                                    "C:\\Users\\ennio\\source\\repos\\breach";

    private static final String MOONLIGHT_ATOM = "smallError";

    public static void main(String[] args)
            throws InterruptedException, ExecutionException, URISyntaxException
    {
        MatlabEngine eng = MatlabEngine.startMatlab();

        String localPath = Paths.get(AbstractFuelControl.class
                .getResource("afc_breach_monitoring.m")
                .toURI()).getParent().toAbsolutePath().toString();

        eng.eval("addpath(\"" + BREACH_PATH + "\")");
        eng.eval("addpath(\"" + localPath + "\")");

        System.out.println("General Execution Starting at: " +
                            System.currentTimeMillis());

        List<SegmentInterface<Double, AbstractInterval<Double>>>
                breach = runBreach(eng);

        List<SegmentInterface<Double, AbstractInterval<Double>>>
                moonlight = runMoonlight(eng);

        assertEquals(breach, moonlight);
        eng.close();
    }

    private static List<SegmentInterface<Double, AbstractInterval<Double>>>
    runBreach(MatlabEngine eng) throws ExecutionException,
                                       InterruptedException
    {
        long before = System.currentTimeMillis();

        eng.eval("afc_breach_monitoring");

        long after = System.currentTimeMillis();

        System.out.println("Breach Execution Time (msec): " +
                            (after - before) / 1000.);

        double[] rhoLow = eng.getVariable("rho_low");
        double[] rhoUp = eng.getVariable("rho_up");

        return IntStream.range(0, rhoLow.length).boxed()
                        .map(i -> new TimeSegment<>((double) i,
                                  new AbstractInterval<>(rhoLow[i], rhoUp[i])))
                        .collect(Collectors.toList());
    }

    private static List<SegmentInterface<Double, AbstractInterval<Double>>>
    runMoonlight(MatlabEngine eng) throws ExecutionException,
                                          InterruptedException
    {
        long before = System.currentTimeMillis();

        eng.eval("afc_moonlight_monitoring");

        double[] input = eng.getVariable("input");

        List<Update<Double, Double>> updates = genUpdates(input);
        OnlineTimeMonitor<Double, Double> m = instrument();

        TimeSignal<Double, AbstractInterval<Double>> result = null;
        for(Update<Double, Double> u: updates) {
            try {
                result = m.monitor(u);
            } catch (Exception e) {
                System.out.println("Suppressing error at update: " +
                                    u.toString());
            }
        }

        long after = System.currentTimeMillis();

        System.out.println("Moonlight Execution Time (msec): " +
                            (after - before) / 1000.);

        return Objects.requireNonNull(result).getSegments();
    }

    private static OnlineTimeMonitor<Double, Double> instrument()
    {
        Formula f = new GloballyFormula(
                new OrFormula(
                        new AtomicFormula(MOONLIGHT_ATOM),
                        new EventuallyFormula(
                                new AtomicFormula(MOONLIGHT_ATOM)
                                ,  new Interval(0.0, 1.0))
                ),
                new Interval(10.0, 30.0))
                ;

        // alw_[10, 30] ((abs(AF[t]-AFref[t]) > 0.05) => (ev_[0, 1] (abs(AF[t]-AFref[t]) < 0.05)))

        HashMap<String, Function<Double, AbstractInterval<Double>>>
                atoms = new HashMap<>();

        atoms.put(MOONLIGHT_ATOM,
                trc -> new AbstractInterval<>(0.05 - trc,
                        0.05 - trc));

        return new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
    }

    private static List<Update<Double, Double>> genUpdates(double[] values) {
        List<Update<Double, Double>> updates = new ArrayList<>();
        for(int i = 0; i < values.length; i++) {
            updates.add(new Update<>((double)i, (double)i + 1, values[i]));
        }
        return updates;
    }
}
