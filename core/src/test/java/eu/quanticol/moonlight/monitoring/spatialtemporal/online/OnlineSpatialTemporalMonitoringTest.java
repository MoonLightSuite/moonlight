package eu.quanticol.moonlight.monitoring.spatialtemporal.online;

import eu.quanticol.moonlight.domain.DoubleDistance;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.domain.IntervalDomain;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.util.MultiValuedTrace;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;


/**
 * The formula of this example comes from:
 * https://doi.org/10.1007/978-3-642-39799-8_19
 */
class SpatialTemporalMonitoringTest {
    private static final int X_SIGNAL = 0;
    private static final int Y_SIGNAL = 1;

    /* PROBLEM CONSTANTS */
    private static final int A = 6;
    private static final int B = 10;
    private static final int C = 14;

    /* TIME POINTS AT WHICH THE MONITORING WILL BE TESTED */
    //private static final int T1 = 4;
    private static final int T2 = 8;
    private static final int T3 = 13;
    private static final int T4 = 19;
    private static final int T5 = 22;
    private static final int T_MAX = 24;

    /* SPATIAL DATA */
    private static final GraphModel<Double> city = new GraphModel<>(7);


    private static final LocationService<Double> LOC_SVC =
                    TestUtils.createLocServiceStatic(0, 1, 3, city);

    @BeforeAll
    static void setup() {
        city.add(0, 2.0, 1);
        city.add(1, 2.0, 0);
        city.add(0, 2.0, 5);
        city.add(5, 2.0, 0);
        city.add(1, 9.0, 2);
        city.add(2, 9.0, 1);
        city.add(2, 3.0, 3);
        city.add(3, 3.0, 2);
        city.add(3, 6.0, 4);
        city.add(4, 6.0, 3);
        city.add(4, 7.0, 5);
        city.add(5, 7.0, 4);
        city.add(6, 4.0, 1);
        city.add(1, 4.0, 6);
        city.add(6, 15.0, 3);
        city.add(3, 15.0, 6);
    }


    @Test
    void berkleyTestT2() {
        // Trace generation...
        SpatialTemporalSignal<List<Comparable<?>>> trace = init();

        // Monitor Instrumentation...
        OnlineSpatialTemporalMonitoring<Double, List<Comparable<?>>, Interval>
                                                            m = instrument();

        //Test at T2!
        assertEquals(Interval.any(), test(trace, m));
    }

    @Test
    void berkleyTestT3() {
        // Trace generation...
        MultiValuedTrace trace = init();

        // Monitor Instrumentation...
        OnlineSpatialTemporalMonitoring<Double, List<Comparable<?>>, Interval>
                                                            m = instrument();

        // Update with data up to T3...
        List<Pair<Integer, Interval>> xValues = new ArrayList<>();
        List<Pair<Integer, Interval>> yValues = new ArrayList<>();
        xValues.add(new Pair<>(-1, new Interval(T2, T3)));
        yValues.add(new Pair<>(-1, new Interval(T2, T3)));
        update(trace, xValues, yValues);

        //Test at T3!
        assertEquals(Interval.any(), test(trace, m));
    }

    //@Disabled("This seems to be a corner case and requires investigation.")
    @Test
    void berkleyTestT4() {
        // Trace generation...
        MultiValuedTrace trace = init();

        // Monitor Instrumentation...
        OnlineSpatialTemporalMonitoring<Double, List<Comparable<?>>, Interval>
                                                            m = instrument();

        // Update with data up to T3...
        List<Pair<Integer, Interval>> xValues = new ArrayList<>();
        List<Pair<Integer, Interval>> yValues = new ArrayList<>();
        xValues.add(new Pair<>(-1, new Interval(T2, T3)));
        yValues.add(new Pair<>(-1, new Interval(T2, T3)));

        // Update with data up to T4...
        xValues.add(new Pair<>(-2, new Interval(T3, T4)));
        yValues.add(new Pair<>(1, new Interval(T3, T4)));
        update(trace, xValues, yValues);

        //Test at T4!
        assertEquals(new Interval(-2.0), test(trace, m));
    }

    @Test
    void berkleyTestT5() {
        // Trace generation...
        MultiValuedTrace trace = init();

        // Monitor Instrumentation...
        OnlineSpatialTemporalMonitoring<Double, List<Comparable<?>>, Interval>
                                                            m = instrument();

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
        update(trace, xValues, yValues);

        //Test at T5!
        assertEquals(new Interval(-2.0), test(trace, m));
    }

    @Test
    void berkleyTestTMax() {
        // Trace generation...
        MultiValuedTrace trace = init();

        // Monitor Instrumentation...
        OnlineSpatialTemporalMonitoring<Double, List<Comparable<?>>, Interval>
                                                            m = instrument();

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
        update(trace, xValues, yValues);

        //Test at end of time!
        assertEquals(new Interval(-2.0), test(trace, m));
    }

    @Test
    void berkleyTestAllTogether() {
        // Trace generation...
        MultiValuedTrace trace = init();

        // Monitor Instrumentation...
        OnlineSpatialTemporalMonitoring<Double, List<Comparable<?>>, Interval>
                                                            m = instrument();

        //Test at T2!
        test(trace, m);
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


    /**
     * @return an input trace, up to T2
     */
    private static MultiValuedTrace init() {
        List<Pair<Integer, Interval>> xValues = new ArrayList<>();
        xValues.add(new Pair<>(1, new Interval(0, 4, true)));
        xValues.add(new Pair<>(2, new Interval(4, T2)));

        List<Pair<Integer, Interval>> yValues = new ArrayList<>();
        yValues.add(new Pair<>(-1, new Interval(0, 4, true)));
        yValues.add(new Pair<>(2, new Interval(4, T2)));

        // We generate a signal and return it...
        return traceGenerator(T2, xValues, yValues);
    }

    /**
     * Actual parametric test runner.
     * @param trace input trace
     * @param p monitoring process to use
     * @return an Interval corresponding to the final result of the monitoring
     */
    private static Interval test(
            SpatialTemporalSignal<List<Comparable<?>>> trace,
            OnlineSpatialTemporalMonitoring<Double,
                                            List<Comparable<?>>,
                                            Interval> p)
    {
        try {
            // We select the formula to test...
            Formula formula = testFormula();
            SpatialTemporalSignal<Interval> m = p.monitor(formula, null)
                    .monitor(LOC_SVC, trace);

            return m.getSignals().get(0).getIterator(true).value();
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
     */
    private static void update(
            MultiValuedTrace signal,
            List<Pair<Integer, Interval>> xValues,
            List<Pair<Integer, Interval>> yValues)
    {
        double init = xValues.get(0).getSecond().getStart();
        double length = xValues.get(xValues.size() - 1).getSecond().getEnd();
        for(double t = init; t <= length; t ++) {
            List<Comparable<?>> values = new ArrayList<>();
            updateValues(xValues, values, t);
            updateValues(yValues, values, t);

            List<List<Comparable<?>>> data = new ArrayList<>();
            for(int i = 0; i < city.size(); i++) {
                data.add(values);
            }
            signal.add(t, data);
            signal.endAt(t);
        }
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
    private static OnlineSpatialTemporalMonitoring<Double, List<Comparable<?>>, Interval> instrument()
    {


        HashMap<String,
                Function<Parameters, Function<List<Comparable<?>>, Interval>>>
                atoms = new HashMap<>();

        //positiveX is the atomic proposition: x >= 0
        atoms.put("positiveX", ps -> trc ->
                new Interval((Integer) trc.get(X_SIGNAL)));
        atoms.put("positiveY", ps -> trc ->
                new Interval((Integer) trc.get(Y_SIGNAL)));

        return new OnlineSpatialTemporalMonitoring<>(atoms, spatialModel(), new IntervalDomain(), false);
    }

    /**
     * @return a set of distance functions over a spatial graph
     */
    private static Map<String, Function<SpatialModel<Double>,
            DistanceStructure<Double, ?>>> spatialModel()
    {
        double range = 10;
        DistanceStructure<Double, Double> trivial =
                new DistanceStructure<>(x -> x,
                        new DoubleDistance(),
                        0.0,
                        range,
                        city);
        Map<String, Function<SpatialModel<Double>,
                DistanceStructure<Double, ?>>>
                distFunctions = new HashMap<>();
        distFunctions.put("trivial", g -> trivial);

        return distFunctions;
    }

    /**
     * @return we return the formula from the paper example
     */
    private static Formula testFormula() {
        Formula atomX = new AtomicFormula("positiveX");
        Formula atomY = new AtomicFormula("positiveY");

        return //new EverywhereFormula("trivial",
                //new GloballyFormula(
                        //new OrFormula(
                                        new EventuallyFormula(atomX,
                                                            new Interval(B, C)) //;
                                        //,
                                //new NegationFormula(atomY))
                ;
                                //atomY);
                        //, new Interval(0, A));
        //)
        //;
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
    private static MultiValuedTrace traceGenerator(
            int traceLength,
            List<Pair<Integer, Interval>> xValues,
            List<Pair<Integer, Interval>> yValues)
    {
        Integer[][] xSignal = new Integer[city.size()][traceLength + 1];
        Integer[][] ySignal = new Integer[city.size()][traceLength + 1];

        for(int i = 0; i < city.size(); i++) {
            xSignal[i] = valuesFromIntervals(xValues);
            ySignal[i] = valuesFromIntervals(yValues);
        }

        MultiValuedTrace trace = new MultiValuedTrace(city.size(),
                                                traceLength + 1);
        trace.setDimension(xSignal, X_SIGNAL)
             .setDimension(ySignal, Y_SIGNAL)
             .initialize();

        return trace;
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