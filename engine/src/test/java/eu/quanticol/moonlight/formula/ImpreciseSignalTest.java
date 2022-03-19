package eu.quanticol.moonlight.formula;

import eu.quanticol.moonlight.core.base.Box;
import eu.quanticol.moonlight.core.formula.Formula;
import eu.quanticol.moonlight.domain.AbsIntervalDomain;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.formula.classic.NegationFormula;
import eu.quanticol.moonlight.formula.classic.OrFormula;
import eu.quanticol.moonlight.formula.temporal.EventuallyFormula;
import eu.quanticol.moonlight.formula.temporal.GloballyFormula;
import eu.quanticol.moonlight.offline.monitoring.TemporalMonitoring;
import eu.quanticol.moonlight.offline.signal.Signal;
import eu.quanticol.moonlight.core.base.Pair;
import eu.quanticol.moonlight.util.MultiValuedTrace;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled("This whole class should be reworked")
class ImpreciseSignalTest {
    private static final int X_SIGNAL = 0;
    private static final int Y_SIGNAL = 1;

    /* PROBLEM CONSTANTS */
    private static final int A = 6;
    private static final int B = 10;
    private static final int C = 14;

    /* TIME POINTS AT WHICH THE MONITORING WILL BE TESTED */
    private static final double T4 = 19;
    private static final double T5 = 22;
    private static final double T_MAX = 24;

    @Disabled("This seems to be a corner case and requires investigation.")
    @Test
    void berkleyTestT4() {
        List<Pair<Box<Double>, Box<Double>>> xValues = new ArrayList<>();
        xValues.add(pair(interval(1), interval(0, 4)));
        xValues.add(pair(interval(2), interval(4, 8)));
        xValues.add(pair(interval(-1), interval(8, 13)));
        xValues.add(pair(interval(-2), interval(13, 19)));

        List<Pair<Box<Double>, Box<Double>>> yValues = new ArrayList<>();
        yValues.add(pair(interval(-1), interval(0, 4)));
        yValues.add(pair(interval(2), interval(4, 8)));
        yValues.add(pair(interval(-1), interval(8, 13)));
        yValues.add(pair(interval(1), interval(13, T4)));

        assertEquals(new Box<>(-2.0, -2.0), test(T4, xValues, yValues));
    }

    private static <F, S> Pair<F, S> pair(F first, S second) {
        return new Pair<>(first, second);
    }

    private static Box<Double> interval(double a, double b) {
        return new Box<>(a, b);
    }

    private static Box<Double> interval(double a) {
        return new Box<>(a, a);
    }

    @Test
    void berkleyTestT5() {
        List<Pair<Box<Double>, Box<Double>>> xValues = new ArrayList<>();
        xValues.add(pair(interval(1), interval(0, 4)));
        xValues.add(pair(interval(2), interval(4, 8)));
        xValues.add(pair(interval(-1), interval(8, 13)));
        xValues.add(pair(interval(-2), interval(13, 19)));
        xValues.add(pair(interval(2), interval(19, T5)));

        List<Pair<Box<Double>, Box<Double>>> yValues = new ArrayList<>();
        yValues.add(pair(interval(-1), interval(0, 4)));
        yValues.add(pair(interval(2), interval(4, 8)));
        yValues.add(pair(interval(-1), interval(8, 13)));
        yValues.add(pair(interval(1), interval(13, T5)));

        assertEquals(interval(-2), test(T5, xValues, yValues));
    }

    @Test
    void berkleyTestTMax() {
        List<Pair<Box<Double>, Box<Double>>> xValues = new ArrayList<>();
        xValues.add(pair(interval(1), interval(0, 4)));
        xValues.add(pair(interval(2), interval(4, 8)));
        xValues.add(pair(interval(-1), interval(8, 13)));
        xValues.add(pair(interval(-2), interval(13, 19)));
        xValues.add(pair(interval(2), interval(19, 22)));
        xValues.add(pair(interval(-1), interval(22, T_MAX)));

        List<Pair<Box<Double>, Box<Double>>> yValues = new ArrayList<>();
        yValues.add(pair(interval(-1), interval(0, 4)));
        yValues.add(pair(interval(2), interval(4, 8)));
        yValues.add(pair(interval(-1),interval(8, 13)));
        yValues.add(pair(interval(1), interval(13, T_MAX)));

        assertEquals(interval(-2), test(T_MAX, xValues, yValues));
    }

    /**
     * Actual parametric test runner.
     *
     * @param traceLength length of the input trace
     * @param xValues     values for the first input signal
     * @param yValues     values for the second input signal
     * @return an Box<Double> corresponding to the final result of the monitoring
     */
    private static Box<Double> test(double traceLength,
                                    List<Pair<Box<Double>, Box<Double>>> xValues,
                                    List<Pair<Box<Double>, Box<Double>>> yValues) {
        try {
            // Signals generator...
            Signal<List<Comparable<?>>> trace = traceGenerator((int) traceLength,
                                                               xValues,
                                                               yValues);
            // Formula selection...
            Formula formula = testFormula();

            // Generate Monitors...
            Signal<Box<Double>> m = monitor(formula, trace);

            return m.getIterator(true).getCurrentValue();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Runs a temporal monitor on the given input.
     *
     * @param formula input formula to monitor
     * @param trace   input data over which the formula is monitored
     * @return a signal corresponding to the result of the monitoring.
     */
    private static Signal<Box<Double>> monitor(Formula formula,
                                               Signal<List<Comparable<?>>> trace) {
        //a is the atomic proposition: a >= 0
        HashMap<String,
                Function<Parameters, Function<List<Comparable<?>>, Box<Double>>>>
                atoms = new HashMap<>();

        atoms.put("positiveX", ps -> trc ->
                (Box<Double>) trc.get(X_SIGNAL));
        atoms.put("positiveY", ps -> trc ->
                (Box<Double>) trc.get(Y_SIGNAL));

        TemporalMonitoring<List<Comparable<?>>, Box<Double>> monitoring =
                new TemporalMonitoring<>(atoms, new AbsIntervalDomain<>(new DoubleDomain()));

        return monitoring.monitor(formula, null).monitor(trace);
    }

    private static Formula testFormula() {
        Formula atomX = new AtomicFormula("positiveX");
        Formula atomY = new AtomicFormula("positiveY");

        return new GloballyFormula(
                    new OrFormula(new EventuallyFormula(atomX,
                                                        new Interval(B, C)),
                                  new NegationFormula(atomY)),
                    new Interval(0, A));
    }

    /**
     * Given two list of values, generates a multivalued trace.
     *
     * @param traceLength length of the trace
     * @param xValues     values for the first coordinate
     * @param yValues     values for the second coordinate
     * @return a MultiValuedTrace
     * @see MultiValuedTrace
     */
    private static Signal<List<Comparable<?>>> traceGenerator(
                                    int traceLength,
                                    List<Pair<Box<Double>, Box<Double>>> xValues,
                                    List<Pair<Box<Double>, Box<Double>>> yValues)
    {
        Box<Double>[][] xSignal =
                (Box<Double>[][])new Object[1][traceLength]; // 1 location
        xSignal[0] = valuesFromIntervals(xValues);

        Box<Double>[][] ySignal =
                (Box<Double>[][])new Object[1][traceLength]; // 1 location
        ySignal[0] = valuesFromIntervals(yValues);

        MultiValuedTrace trace = new MultiValuedTrace(1, traceLength);
        trace.setDimension(xSignal, X_SIGNAL)
             .setDimension(ySignal, Y_SIGNAL)
             .initialize();

        return trace.getSignals().get(0);   //list discarded (only one location)
    }

    /**
     * Given a piecewise constant integer function,
     * returns a corresponding array of values, one for any integer.
     *
     * @param function function piecewise definitions
     * @return Array of function values
     */
    private static Box<Double>[] valuesFromIntervals(List<Pair<Box<Double>, Box<Double>>>
                                                         function) {
        int end = (int) Math.round(function.get(function.size() - 1).getSecond().getEnd());

        Box<Double>[] data = (Box<Double>[]) new Object[end];

        for (int i = 0; i < end; i++) {
            for (Pair<Box<Double>, Box<Double>> piece : function) {
                if (piece.getSecond().contains((double) i)) {
                    data[i] = piece.getFirst();
                    break;
                }
            }
        }
        return data;
    }
}
