package eu.quanticol.moonlight.formula;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.domain.IntervalDomain;
import eu.quanticol.moonlight.monitoring.online.OnlineTimeMonitoring;
import eu.quanticol.moonlight.monitoring.temporal.online.LegacyOnlineTemporalMonitoring;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.online.*;
import eu.quanticol.moonlight.util.MultiValuedTrace;
import eu.quanticol.moonlight.util.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The formula of this example comes from:
 * https://doi.org/10.1007/978-3-642-39799-8_19
 */
class RoSIBerkeleyTest2 {
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
    void berkleyTestEmpty() {
        // Trace generation...
        SignalInterface<Double, AbstractInterval<Double>> trace =
                new OnlineSignal<>(new DoubleDomain());

        // Monitor Instrumentation...
        OnlineTimeMonitoring<Double, Double> m =
                instrument(testFormula());

        assertEquals(ANY, test(null, m).getValueAt(T0));
    }

    @Test
    void berkleyTestAtT2() {
        // Trace generation...
        SignalInterface<Double, AbstractInterval<Double>> trace =
                new OnlineSignal<>(new DoubleDomain());

        // Monitor Instrumentation...
        OnlineTimeMonitoring<Double, Double> m =
                instrument(testFormula());

        AbstractInterval<Double> expected;

        Update<Double, Double> u = new Update<>(T0, T1, 1.0);
        m.monitor(u);   //Signal init

        //Test at T2!
        u = new Update<>(T1, T2, 2.0);
        expected = ANY;
        assertEquals(expected, test(u, m).getValueAt(T0));
    }

    @Test
    void berkleyTestAtT3() {
        // Trace generation...
        SignalInterface<Double, AbstractInterval<Double>> trace =
                new OnlineSignal<>(new DoubleDomain());

        // Monitor Instrumentation...
        OnlineTimeMonitoring<Double, Double> m =
                instrument(testFormula());

        AbstractInterval<Double> expected;

        Update<Double, Double> u = new Update<>(T0, T1, 1.0);
        m.monitor(u);   //Signal init

        //Update at T2...
        u = new Update<>(T1, T2, 2.0);
        test(u, m);

        //Test at T3!
        u = new Update<>(T2, T3, -1.0);
        expected = new AbstractInterval<>(-1.0, Double.POSITIVE_INFINITY);
        assertEquals(expected, test(u, m).getValueAt(T0));
    }

    @Test
    void berkleyTestAtT4() {
        OnlineSignal<Double> result;
        AbstractInterval<Double> expected;

        // Monitor Instrumentation...
        OnlineTimeMonitoring<Double, Double> m =
                instrument(testFormula());

        Update<Double, Double> u = new Update<>(T0, T1, 1.0);
        m.monitor(u);   //Signal init

        //Update at T2...
        u = new Update<>(T1, T2, 2.0);
        test(u, m);
        //Update at T3...
        u = new Update<>(T2, T3, -1.0);
        test(u, m);

        //Test at T4!
        u = new Update<>(T3, T4, -2.0);
        Object[] sgms = test(u, m).getSegments().toArray();
        ImmutableSegment<AbstractInterval<Double>> s0 =
                new ImmutableSegment<>(0.0, new AbstractInterval<>(-1.0, -1.0));
        ImmutableSegment<AbstractInterval<Double>> s1 =
                new ImmutableSegment<>(3.0, new AbstractInterval<>(-2.0, -2.0));
        ImmutableSegment<AbstractInterval<Double>> s2 =
                new ImmutableSegment<>(5.0, new AbstractInterval<>(-2.0, P_INF));
        ImmutableSegment<AbstractInterval<Double>> s3 =
                new ImmutableSegment<>(9.0, new AbstractInterval<>(N_INF, P_INF));
        assertEquals(s0, sgms[0]);
        assertEquals(s1, sgms[1]);
        assertEquals(s2, sgms[2]);
        assertEquals(s3, sgms[3]);

        //Test at T5!
        //result = new AbstractInterval<>(-1.0, Double.POSITIVE_INFINITY);
        //assertEquals(result, test(u, m, T0));
    }

    @Test
    void berkleyTestAtT5() {
        SignalInterface<Double, AbstractInterval<Double>> result;
        AbstractInterval<Double> expected;

        // Monitor Instrumentation...
        OnlineTimeMonitoring<Double, Double> m =
                instrument(testFormula());

        Update<Double, Double> u = new Update<>(T0, T1, 1.0);
        m.monitor(u);   //Signal init

        //Update at T2...
        u = new Update<>(T1, T2, 2.0);
        test(u, m);
        //Update at T3...
        u = new Update<>(T2, T3, -1.0);
        test(u, m);

        //Test at T4!
        u = new Update<>(T3, T4, -2.0);
        test(u, m);

        //Test at T5!
        u = new Update<>(T4, T5, 2.0);

        Object[] sgms = test(u, m).getSegments().toArray();
        ImmutableSegment<AbstractInterval<Double>> s0 =
                new ImmutableSegment<>(0.0, new AbstractInterval<>(-1.0, -1.0));
        ImmutableSegment<AbstractInterval<Double>> s1 =
                new ImmutableSegment<>(3.0, new AbstractInterval<>(-2.0, -2.0));
        ImmutableSegment<AbstractInterval<Double>> s2 =
                new ImmutableSegment<>(5.0, new AbstractInterval<>(2.0, 2.0));
        ImmutableSegment<AbstractInterval<Double>> s3 =
                new ImmutableSegment<>(8.0, new AbstractInterval<>(2.0, P_INF));
        ImmutableSegment<AbstractInterval<Double>> s4 =
                new ImmutableSegment<>(12.0, new AbstractInterval<>(N_INF, P_INF));
        assertEquals(s0, sgms[0]);
        assertEquals(s1, sgms[1]);
        assertEquals(s2, sgms[2]);
        assertEquals(s3, sgms[3]);
        assertEquals(s4, sgms[4]);
    }

/*
    @Disabled("Re-engeneering Sliding Window")
    @Test
    void berkleyTestT3() {
        // Trace generation...
        //Signal<List<Comparable<?>>> trace = init();
        SignalInterface<Double, AbstractInterval<Double>> trace =
                new OnlineSignal<>(new DoubleDomain());

        // Monitor Instrumentation...
        LegacyOnlineTemporalMonitoring<List<Comparable<?>>, Interval> m = instrument();

        // Update with data up to T3...
        List<Pair<Integer, Interval>> xValues = new ArrayList<>();
        List<Pair<Integer, Interval>> yValues = new ArrayList<>();
        xValues.add(new Pair<>(-1, new Interval(T2, T3)));
        yValues.add(new Pair<>(-1, new Interval(T2, T3)));
        trace = update(trace, xValues, yValues);

        //Test at T3!
        assertEquals(Interval.any(), test(trace, m));
    }

    @Disabled("This seems to be a corner case and requires investigation.")
    @Test
    void berkleyTestT4() {
        // Trace generation...
        Signal<List<Comparable<?>>> trace = init();

        // Monitor Instrumentation...
        LegacyOnlineTemporalMonitoring<List<Comparable<?>>, Interval> m = instrument();

        // Update with data up to T3...
        List<Pair<Integer, Interval>> xValues = new ArrayList<>();
        List<Pair<Integer, Interval>> yValues = new ArrayList<>();
        xValues.add(new Pair<>(-1, new Interval(T2, T3)));
        yValues.add(new Pair<>(-1, new Interval(T2, T3)));

        // Update with data up to T4...
        xValues.add(new Pair<>(-2, new Interval(T3, T4)));
        yValues.add(new Pair<>(1, new Interval(T3, T4)));
        trace = update(trace, xValues, yValues);

        //Test at T4!
        assertEquals(new Interval(-2.0), test(trace, m));
    }

    @Disabled("Re-engeneering Sliding Window")
    @Test
    void berkleyTestT5() {
        // Trace generation...
        Signal<List<Comparable<?>>> trace = init();

        // Monitor Instrumentation...
        LegacyOnlineTemporalMonitoring<List<Comparable<?>>, Interval> m = instrument();

        // Update with data up to T3...
        List<Pair<Integer, Interval>> xValues = new ArrayList<>();
        List<Pair<Integer, Interval>> yValues = new ArrayList<>();
        xValues.add(new Pair<>(-1, new Interval(T2, T3)));
        yValues.add(new Pair<>(-1, new Interval(T2, T3)));

        // Update with data up to T4...
        xValues.add(new Pair<>(-2, new Interval(T3, T4)));
        yValues.add(new Pair<>(1, new Interval(T3, T4)));

        // Update with data up to T5...
        xValues.add(new Pair<>(2, new Interval(T4, T5)));
        yValues.add(new Pair<>(1, new Interval(T4, T5)));
        trace = update(trace, xValues, yValues);

        //Test at T5!
        assertEquals(new Interval(-2.0), test(trace, m));
    }

    @Disabled("Re-engeneering Sliding Window")
    @Test
    void berkleyTestTMax() {
        // Trace generation...
        Signal<List<Comparable<?>>> trace = init();

        // Monitor Instrumentation...
        LegacyOnlineTemporalMonitoring<List<Comparable<?>>, Interval> m = instrument();

        // Update with data up to T3...
        List<Pair<Integer, Interval>> xValues = new ArrayList<>();
        List<Pair<Integer, Interval>> yValues = new ArrayList<>();
        xValues.add(new Pair<>(-1, new Interval(T2, T3)));
        yValues.add(new Pair<>(-1, new Interval(T2, T3)));

        // Update with data up to T4...
        xValues.add(new Pair<>(-2, new Interval(T3, T4)));
        yValues.add(new Pair<>(1, new Interval(T3, T4)));

        // Update with data up to T5...
        xValues.add(new Pair<>(2, new Interval(T4, T5)));
        yValues.add(new Pair<>(1, new Interval(T4, T5)));

        // Update with data up to the end...
        xValues.add(new Pair<>(-1, new Interval(T5, T_MAX)));
        yValues.add(new Pair<>(1, new Interval(T5, T_MAX)));
        trace = update(trace, xValues, yValues);

        //Test at end of time!
        assertEquals(new Interval(-2.0), test(trace, m));
    }

    @Disabled("Re-engeneering Sliding Window")
    @Test
    void berkleyTestAllTogether() {
        // Trace generation...
        Signal<List<Comparable<?>>> trace = init();

        // Monitor Instrumentation...
        LegacyOnlineTemporalMonitoring<List<Comparable<?>>, Interval> m = instrument();

        //Test at T2!
        assertEquals(Interval.any(), test(trace, m));

        // Update with data up to T3...
        List<Pair<Integer, Interval>> xValues = new ArrayList<>();
        List<Pair<Integer, Interval>> yValues = new ArrayList<>();
        xValues.add(new Pair<>(-1, new Interval(T2, T3)));
        yValues.add(new Pair<>(-1, new Interval(T2, T3)));
        update(trace, xValues, yValues);

        //Test at T3!
        assertEquals(Interval.any(), test(trace, m));

        // Update with data up to T4...
        xValues = new ArrayList<>();
        yValues = new ArrayList<>();
        xValues.add(new Pair<>(-2, new Interval(T3, T4)));
        yValues.add(new Pair<>(1, new Interval(T3, T4)));
        update(trace, xValues, yValues);

        //Test at T4!
        //TODO: disabled for same issue as in berkeleyTestT4()
        //assertEquals(new Interval(-2.0), test(trace, m));

        // Update with data up to T5...
        xValues = new ArrayList<>();
        yValues = new ArrayList<>();
        xValues.add(new Pair<>(2, new Interval(T4, T5)));
        yValues.add(new Pair<>(1, new Interval(T4, T5)));
        update(trace, xValues, yValues);

        //Test at T5!
        assertEquals(new Interval(-2.0), test(trace, m));

        // Update with data up to the end...
        xValues = new ArrayList<>();
        yValues = new ArrayList<>();
        xValues.add(new Pair<>(-1, new Interval(T5, T_MAX)));
        yValues.add(new Pair<>(1, new Interval(T5, T_MAX)));
        update(trace, xValues, yValues);

        //Test at end of time!
        assertEquals(new Interval(-2.0), test(trace, m));
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

    /**
     * Actual parametric test runner.
     * @param m monitoring process to use
     * @return an Interval corresponding to the final result of the monitoring
     */
    private static OnlineSignal<Double> test(
            Update<Double, Double> u,
            OnlineTimeMonitoring<Double, Double> m)
    {
        try {
            OnlineSignal<Double> r =
                    (OnlineSignal<Double>) m.monitor(u);

            return r;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Updates a trace by attaching new data to the previous one
     * @param signal previous signal to update
     * @param xValues list of values for the X signal
     * @param yValues list of values for the Y signal
     * @return a new traces that combines this data with the old one
     */
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

    /**
     * Adds the input data to the given output
     * @param input input data
     * @param output output list
     * @param time time instant of interest
     */
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


    /**
     * @return a Monitoring object, ready to run
     */
    private static OnlineTimeMonitoring<Double, Double> instrument(Formula f)
    {
        HashMap<String,
                Function<Double, AbstractInterval<Double>>>
                atoms = new HashMap<>();

        //positiveX is the atomic proposition: x >= 0
        atoms.put("positiveX", trc -> new AbstractInterval<>(trc, trc));

        return new OnlineTimeMonitoring<>(f, new DoubleDomain(), atoms);
    }

    /**
     * @return we return the formula from the paper example
     */
    private static Formula testFormula() {
        Formula atomX = new AtomicFormula("positiveX");
        Formula atomY = new AtomicFormula("positiveY");

        return //new GloballyFormula(
                 //   new OrFormula(
                                    new EventuallyFormula(atomX,
                                                   new Interval(B, C))
                                    //,
                                  //new NegationFormula(atomX)),
                    //new Interval(0, A))
        ;
    }

    /**
     * Given two list of values, generates a Multivalued spatial-temporal trace.
     * @param traceLength length of the trace
     * @param xValues values for the first coordinate
     * @param yValues values for the second coordinate
     * @return a MultiValuedTrace
     *
     * @see MultiValuedTrace
     */
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

    /**
     * Given a piecewise constant integer function,
     * returns a corresponding array of values, one for any integer.
     * @param function function piecewise definitions
     * @return Array of function values
     */
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
}