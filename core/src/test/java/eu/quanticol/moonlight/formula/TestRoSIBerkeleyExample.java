package eu.quanticol.moonlight.formula;

import eu.quanticol.moonlight.domain.*;
import eu.quanticol.moonlight.monitoring.online.OnlineTimeMonitor;
import eu.quanticol.moonlight.signal.online.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The formula of this example comes from:
 * https://doi.org/10.1007/978-3-642-39799-8_19
 */
class TestRoSIBerkeleyExample {
    private static final int X_SIGNAL = 0;
    private static final int Y_SIGNAL = 1;

    /* PROBLEM CONSTANTS */
    private static final int A = 6;
    private static final int B = 10;
    private static final int C = 14;

    /* TIME POINTS AT WHICH THE MONITORING WILL BE TESTED */
    private static final double T0 = 0;
    private static final double T1 = 4;
    private static final double T2 = 8;
    private static final double T3 = 13;
    private static final double T4 = 19;
    private static final double T5 = 22;
    private static final double T_MAX = 24;

    private static final Double P_INF = Double.POSITIVE_INFINITY;
    private static final Double N_INF = Double.NEGATIVE_INFINITY;

    private static final AbstractInterval<Double> ANY =
            new AbstractInterval<>(N_INF, P_INF);

    @Test
    void testEmptySignal() {
        // Monitor Instrumentation...
        OnlineTimeMonitor<List<Double>, Double> m =
                instrument(leftFormula());

        Object[] ss = exec(null, m);

        if(ss != null) {
            assertValue(0.0, ANY, ss[0]);

            // Exactly one segment
            assertEquals(1, ss.length);
        }
        else
            fail("Empty signal should never happen!");
    }

    @Test
    void testLeftFormulaAtT2() {
        Object[] ss = testAtUpdate2(leftFormula());

        if(ss != null) {
            assertValue(0.0, ANY, ss[0]);

            // Exactly three segments
            assertEquals(1, ss.length);
        }
        else
            fail("Empty signal should never happen!");
    }

    @Test
    void testRightFormulaAtT2() {
        Object[] ss = testAtUpdate2(rightFormula());

        if(ss != null) {
            assertValue(T0, new AbstractInterval<>(1.0, 1.0), ss[0]);
            assertValue(T1, new AbstractInterval<>(-2.0, -2.0), ss[1]);
            assertValue(T2, ANY, ss[2]);

            // Exactly three segments
            assertEquals(3, ss.length);
        }
        else
            fail("Empty signal should never happen!");
    }

    @Test
    void testOrFormulaAtT2() {
        Object[] ss = testAtUpdate2(orFormula());

        if(ss != null) {
            assertValue(T0, new AbstractInterval<>(1.0, P_INF), ss[0]);
            assertValue(T1, new AbstractInterval<>(-2.0, P_INF), ss[1]);
            assertValue(T2, ANY, ss[2]);

            // Exactly three updates
            assertEquals(3, ss.length);
        }
        else
            fail("Empty signal should never happen!");
    }

    @Test
    void testWholeFormulaAtT2() {
        Object[] ss = testAtUpdate2(wholeFormula());

        if(ss != null) {
            assertValue(T0, new AbstractInterval<>(-2.0, P_INF), ss[0]);
            assertValue(2, ANY, ss[1]);

            // Exactly three updates
            assertEquals(2, ss.length);
        }
        else
            fail("Empty signal should never happen!");
    }

    @Test
    void testLeftFormulaAtT3() {
        Object[] ss = testAtUpdate3(leftFormula());

        if(ss != null) {
            assertValue(0.0, new AbstractInterval<>(-1.0, P_INF), ss[0]);
            assertValue(3.0, new AbstractInterval<>(N_INF, P_INF), ss[1]);

            // Exactly two segments
            assertEquals(2, ss.length);
        }
        else
            fail("Empty signal should never happen!");
    }

    @Test
    void testWholeFormulaAtT3() {
        Object[] ss = testAtUpdate3(wholeFormula());

        if(ss != null) {
            assertValue(T0, new AbstractInterval<>(-2.0, P_INF), ss[0]);
            assertValue(7, ANY, ss[1]);

            // Exactly three updates
            assertEquals(2, ss.length);
        }
        else
            fail("Empty signal should never happen!");
    }

    @Test
    void testRightFormulaAtT3() {
        Object[] ss = testAtUpdate3(rightFormula());

        if(ss != null) {
            assertValue(T0, new AbstractInterval<>(1.0, 1.0), ss[0]);
            assertValue(T1, new AbstractInterval<>(-2.0, -2.0), ss[1]);
            assertValue(T2, new AbstractInterval<>(1.0, 1.0), ss[2]);
            assertValue(T3, ANY, ss[3]);

            // Exactly four segments
            assertEquals(4, ss.length);
        }
        else
            fail("Empty signal should never happen!");
    }

    @Test
    void testOrFormulaAtT3() {
        Object[] ss = testAtUpdate3(orFormula());

        if(ss != null) {
            assertValue(T0, new AbstractInterval<>(1.0, P_INF), ss[0]);
            assertValue(T1, new AbstractInterval<>(-2.0, P_INF), ss[1]);
            assertValue(T2, new AbstractInterval<>(1.0, P_INF), ss[2]);
            assertValue(T3, ANY, ss[3]);

            // Exactly three updates
            assertEquals(4, ss.length);
        }
        else
            fail("Empty signal should never happen!");
    }

    @Test
    void testLeftFormulaAtT4() {
        Object[] ss = testAtUpdate4(leftFormula());

        if(ss != null) {
            assertValue(0.0, new AbstractInterval<>(-1.0, -1.0), ss[0]);
            assertValue(3.0, new AbstractInterval<>(-2.0, -2.0), ss[1]);
            assertValue(5.0, new AbstractInterval<>(-2.0, P_INF), ss[2]);
            assertValue(9.0, new AbstractInterval<>(N_INF, P_INF), ss[3]);

            // Exactly four segments
            assertEquals(4, ss.length);
        }
        else
            fail("Empty signal should never happen!");
    }

    @Test
    void testRightFormulaAtT4() {
        Object[] ss = testAtUpdate4(rightFormula());

        if(ss != null) {
            assertValue(T0, new AbstractInterval<>(1.0, 1.0), ss[0]);
            assertValue(T1, new AbstractInterval<>(-2.0, -2.0), ss[1]);
            assertValue(T2, new AbstractInterval<>(1.0, 1.0), ss[2]);
            assertValue(T3, new AbstractInterval<>(-1.0, -1.0), ss[3]);
            assertValue(T4, ANY, ss[4]);

            // Exactly five segments
            assertEquals(5, ss.length);
        }
        else
            fail("Empty signal should never happen!");
    }

    @Test
    void testOrFormulaAtT4() {
        Object[] ss = testAtUpdate4(orFormula());

        if(ss != null) {
            assertValue(T0, new AbstractInterval<>(1.0, 1.0), ss[0]);
            assertValue(T1, new AbstractInterval<>(-2.0, -2.0), ss[1]);
            assertValue(5, new AbstractInterval<>(-2.0, P_INF), ss[2]);
            assertValue(T2, new AbstractInterval<>(1.0, P_INF), ss[3]);
            assertValue(T3, new AbstractInterval<>(-1.0, P_INF), ss[4]);
            assertValue(T4, ANY, ss[5]);

            // Exactly four updates
            assertEquals(6, ss.length);
        }
        else
            fail("Empty signal should never happen!");
    }

    //@Disabled("Under investigation")
    @Test
    void testWholeFormulaAtT4() {
        Object[] ss = testAtUpdate4(wholeFormula());

        if(ss != null) {
            assertValue(T0, new AbstractInterval<>(-2.0, -2.0), ss[0]);
            assertValue(5, new AbstractInterval<>(-2.0, P_INF), ss[1]);
            assertValue(8, new AbstractInterval<>(-1.0, P_INF), ss[2]);
            assertValue(T3, ANY, ss[3]);

            // Exactly three updates
            assertEquals(4, ss.length);
        }
        else
            fail("Empty signal should never happen!");
    }

    @Test
    void testLeftFormulaAtT5() {
        Object[] ss = testAtUpdate5(leftFormula());

        if(ss != null) {
            assertValue(0.0, new AbstractInterval<>(-1.0, -1.0), ss[0]);
            assertValue(3.0, new AbstractInterval<>(-2.0, -2.0), ss[1]);
            assertValue(5.0, new AbstractInterval<>(2.0, 2.0), ss[2]);
            assertValue(8.0, new AbstractInterval<>(2.0, P_INF), ss[3]);
            assertValue(12.0, new AbstractInterval<>(N_INF, P_INF), ss[4]);

            // Exactly three segments
            assertEquals(5, ss.length);
        }
        else
            fail("Empty signal should never happen!");

    }

    @Test
    void testRightFormulaAtT5() {
        Object[] ss = testAtUpdate5(rightFormula());

        if(ss != null) {
            assertValue(T0, new AbstractInterval<>(1.0, 1.0), ss[0]);
            assertValue(T1, new AbstractInterval<>(-2.0, -2.0), ss[1]);
            assertValue(T2, new AbstractInterval<>(1.0, 1.0), ss[2]);
            assertValue(T3, new AbstractInterval<>(-1.0, -1.0), ss[3]);
            assertValue(T5, ANY, ss[4]);

            // Exactly six segments
            assertEquals(5, ss.length);
        }
        else
            fail("Empty signal should never happen!");
    }

    @Test
    void testOrFormulaAtT5() {
        Object[] ss = testAtUpdate5(orFormula());

        if(ss != null) {
            assertValue(T0, new AbstractInterval<>(1.0, 1.0), ss[0]);
            assertValue(T1, new AbstractInterval<>(-2.0, -2.0), ss[1]);
            assertValue(5, new AbstractInterval<>(2.0, 2.0), ss[2]);
            assertValue(T2, new AbstractInterval<>(2.0, P_INF), ss[3]);
            assertValue(12, new AbstractInterval<>(1.0, P_INF), ss[4]);
            assertValue(T3, new AbstractInterval<>(-1.0, P_INF), ss[5]);
            assertValue(T5, ANY, ss[6]);

            // Exactly six updates
            assertEquals(7, ss.length);
        }
        else
            fail("Empty signal should never happen!");
    }

    @Test
    void testWholeFormulaAtT5() {
        Object[] ss = testAtUpdate5(wholeFormula());

        if(ss != null) {
            assertValue(T0, new AbstractInterval<>(-2.0, -2.0), ss[0]);
            assertValue(5, new AbstractInterval<>(2.0, 2.0), ss[1]);
            assertValue(6, new AbstractInterval<>(1.0, 2.0), ss[2]);
            assertValue(7, new AbstractInterval<>(-1.0, 2.0), ss[3]);
            assertValue(8, new AbstractInterval<>(-1.0, P_INF), ss[4]);
            assertValue(16, ANY, ss[5]);

            // Exactly three updates
            assertEquals(4, ss.length);
        }
        else
            fail("Empty signal should never happen!");
    }

    /**
     * Actual parametric test runner.
     * @param m monitoring process to use
     * @return an Interval corresponding to the final result of the monitoring
     */
    private static Object[] exec(Update<Double, List<Double>> u,
                                 OnlineTimeMonitor<List<Double>, Double> m)
    {
        try {
            OnlineSignal<Double> r = (OnlineSignal<Double>) m.monitor(u);
            return r.getSegments().toArray();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * @return a Monitoring object, ready to run
     */
    private static OnlineTimeMonitor<List<Double>, Double> instrument(Formula f)
    {
        HashMap<String, Function<List<Double>, AbstractInterval<Double>>>
                                                        atoms = new HashMap<>();

        //positiveX is the atomic proposition: x >= 0
        atoms.put("positiveX",
                    trc -> new AbstractInterval<>(trc.get(X_SIGNAL),
                                                  trc.get(X_SIGNAL)));
        //positiveY is the atomic proposition: y >= 0
        atoms.put("positiveY",
                    trc -> new AbstractInterval<>(trc.get(Y_SIGNAL),
                                                  trc.get(Y_SIGNAL)));

        return new OnlineTimeMonitor<>(f, new DoubleDomain(), atoms);
    }


    private static Formula leftFormula() {
        Formula atomX = new AtomicFormula("positiveX");

        return new EventuallyFormula(atomX, new Interval(B, C));
    }

    /**
     * @return we return the formula from the paper example
     */
    private static Formula orFormula() {
        return new OrFormula(leftFormula(), rightFormula());
    }

    /**
     * @return we return the formula from the paper example
     */
    private static Formula wholeFormula() {
        return new GloballyFormula(orFormula(), new Interval(0, A));
    }

    private static Formula rightFormula() {
        Formula atomY = new AtomicFormula("positiveY");
        return new NegationFormula(atomY);
    }

    private Object[] testAtUpdate2(Formula f) {
        // Monitor Instrumentation...
        OnlineTimeMonitor<List<Double>, Double> m = instrument(f);

        Update<Double, List<Double>> u =
                new Update<>(T0, T1, toList(1.0, -1.0));
        m.monitor(u);   //Signal init

        //Test at T2!
        u = new Update<>(T1, T2, toList(2.0, 2.0));

        return exec(u, m);
    }

    private Object[] testAtUpdate3(Formula f) {
        // Monitor Instrumentation...
        OnlineTimeMonitor<List<Double>, Double> m = instrument(f);

        Update<Double, List<Double>> u =
                new Update<>(T0, T1, toList(1.0, -1.0));
        m.monitor(u);   //Signal init

        //Update at T2...
        u = new Update<>(T1, T2, toList(2.0, 2.0));
        exec(u, m);

        //Test at T3!
        u = new Update<>(T2, T3, toList(-1.0, -1.0));

        return exec(u, m);
    }

    private Object[] testAtUpdate4(Formula f) {
        // Monitor Instrumentation...
        OnlineTimeMonitor<List<Double>, Double> m = instrument(f);

        Update<Double, List<Double>> u =
                new Update<>(T0, T1, toList(1.0, -1.0));
        m.monitor(u);   //Signal init

        //Update at T2...
        u = new Update<>(T1, T2, toList(2.0, 2.0));
        exec(u, m);
        //Update at T3...
        u = new Update<>(T2, T3, toList(-1.0, -1.0));
        exec(u, m);

        //Test at T4!
        u = new Update<>(T3, T4, toList(-2.0, 1.0));

        return exec(u, m);
    }

    private Object[] testAtUpdate5(Formula f) {
        // Monitor Instrumentation...
        OnlineTimeMonitor<List<Double>, Double> m = instrument(f);

        Update<Double, List<Double>> u =
                new Update<>(T0, T1, toList(1.0, -1.0));
        m.monitor(u);   //Signal init

        //Update at T2...
        u = new Update<>(T1, T2, toList(2.0, 2.0));
        exec(u, m);
        //Update at T3...
        u = new Update<>(T2, T3, toList(-1.0, -1.0));
        exec(u, m);

        //Update at T4...
        u = new Update<>(T3, T4, toList(-2.0, 1.0));
        exec(u, m);

        //Test at T5!
        u = new Update<>(T4, T5, toList(2.0, 1.0));

        return exec(u, m);
    }

/*
    private static Signal<List<Comparable<?>>> traceGenerator(int traceLength,
                                      List<Pair<Integer, Interval>> xValues,
                                      List<Pair<Integer, Interval>> yValues) {
        Integer[][] xSignal = new Integer[1][traceLength + 1]; // 1 location
        xSignal[0] = valuesFromIntervals(xValues);

        Integer[][] ySignal = new Integer[1][traceLength + 1]; // 1 location
        ySignal[0] = valuesFromIntervals(yValues);

        MultiValuedTrace trace = new MultiValuedTrace(1, traceLength + 1);
        trace.setDimension(xSignal, X_SIGNAL)
             .setDimension(ySignal, Y_SIGNAL)
             .initialize();

        return trace.getSignals().get(0);   //list discarded (only one location)
    }

    private static Integer[] valuesFromIntervals(List<Pair<Integer, Interval>>
                                                                    function) {
        int end = (int) Math.round(function.get(function.size() - 1)
                                           .getSecond().getEnd());

        Integer[] data = new Integer[end + 1];

        for(int i = 0; i <= end; i++) {
            for (Pair<Integer, Interval> piece : function) {
                if (piece.getSecond().contains((double) i)) {
                    data[i] = piece.getFirst();
                    break;
                }
            }
        }
        return data;
    }


    private static Signal<List<Comparable<?>>> update(
            Signal<List<Comparable<?>>> signal,
            List<Pair<Integer, Interval>> xValues,
            List<Pair<Integer, Interval>> yValues)
    {
        double init = xValues.get(0).getSecond().getStart();
        double length = xValues.get(xValues.size() - 1).getSecond().getEnd();
        for(double t = init; t < length; t ++) {
            List<Comparable<?>> values = new ArrayList<>();
            updateValues(xValues, values, t);
            updateValues(yValues, values, t);

            signal.add(t, values);
            signal.endAt(t);
        }
        return signal;
    }

    private static void updateValues(List<Pair<Integer, Interval>> input,
                                     List<Comparable<?>> output,
                                     double time)
    {
        for(Pair<Integer, Interval> p : input) {
            if(p.getSecond().contains(time)) {
                output.add(p.getFirst());
                break;
            }
        }
    }

    private static SignalInterface<Double, Double> init() {
        List<Pair<Integer, Interval>> xValues = new ArrayList<>();
        xValues.add(new Pair<>(1, new Interval(0, 4, true)));
        xValues.add(new Pair<>(2, new Interval(4, T2)));

        List<Pair<Integer, Interval>> yValues = new ArrayList<>();
        yValues.add(new Pair<>(-1, new Interval(0, 4, true)));
        yValues.add(new Pair<>(2, new Interval(4, T2)));

        // We generate a signal and return it...
        return traceGenerator(T2, xValues, yValues);
    }*/

    private static <T> List<T> toList(T elem1, T elem2) {
        List<T> ls = new ArrayList<>();
        ls.add(elem1);
        ls.add(elem2);
        return ls;
    }

    private static void assertValue(double start,
                                    AbstractInterval<Double> value,
                                    Object segment)
    {
        assertEquals(new TimeSegment<>(start, value), segment);
    }
}