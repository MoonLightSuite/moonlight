package eu.quanticol.moonlight.formula;

import eu.quanticol.moonlight.util.data.MultiValuedTrace;
import eu.quanticol.moonlight.monitoring.TemporalMonitoring;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.util.Pair;

import java.util.*;
import java.util.function.Function;

/**
 * The formula of this example comes from:
 * https://doi.org/10.1007/978-3-642-39799-8_19
 */
public class RoSIBerkeleyTest {
    private static final int X_SIGNAL = 0;
    private static final int Y_SIGNAL = 1;
    private static final int TRACE_LENGTH = 24;

    private static final int A = 6;
    private static final int B = 10;
    private static final int C = 14;

    public static void main(String[] args) {
        test();
    }

    private static void test() {
        try {
            // Signals generator...
            Signal<List<Comparable<?>>> trace = traceGenerator();

            // Formula selection...
            Formula formula = testFormula();

            // Generate Monitors...
            TemporalMonitor<List<Comparable<?>>, Interval> m =
                                                generateMonitoring(formula);

            int nReps = 20;
            for (int i = 0; i < nReps; i++) {
                Signal<Interval> outputSignal = m.monitor(trace);
                outputSignal.getIterator(true).value();
                System.out.println("Output result:" +
                        outputSignal.getIterator(true).value());
            }
            Interval value = m.monitor(trace).getIterator(true).value();
            System.out.println("Robustness result:" + value.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static TemporalMonitor<List<Comparable<?>>, Interval> generateMonitoring(Formula formula) {
        //a is the atomic proposition: a >= 0
        HashMap<String, Function<Parameters, Function<List<Comparable<?>>, Interval>>> atoms = new HashMap<>();
        atoms.put("positiveX", p -> trace -> toInterval((Integer) trace.get(X_SIGNAL)));
        atoms.put("positiveY", p -> trace -> toInterval((Integer) trace.get(Y_SIGNAL)));

        TemporalMonitoring<List<Comparable<?>>, Interval> monitoring =
                new TemporalMonitoring<>(atoms, new IntervalDomain());

        return monitoring.monitor(formula, null);
    }

    private static Formula testFormula() {
        Formula atomX = new AtomicFormula("positiveX");
        Formula atomY = new AtomicFormula("positiveY");

        return new GloballyFormula(
                    new OrFormula(new EventuallyFormula(atomX,
                                                        new Interval(B, C)),
                                  atomY),
                new Interval(0, A));
    }

    private static Signal<List<Comparable<?>>> traceGenerator() {
        Integer[][] xSignal = new Integer[1][TRACE_LENGTH]; // 1 location
        List<Pair<Integer, Interval>> xValues = new ArrayList<>();
        xValues.add(new Pair<>(1, new Interval(0, 4, true)));
        xValues.add(new Pair<>(2, new Interval(4, 8, true)));
        xValues.add(new Pair<>(-1, new Interval(8, 13, true)));
        xValues.add(new Pair<>(-2, new Interval(13, 19, true)));
        xValues.add(new Pair<>(2, new Interval(19, 22, true)));
        xValues.add(new Pair<>(-1, new Interval(22, 24)));
        xSignal[0] = valuesFromIntervals(xValues);


        Integer[][] ySignal = new Integer[1][TRACE_LENGTH]; // 1 location
        List<Pair<Integer,Interval>> yValues = new ArrayList<>();
        yValues.add(new Pair<>(-1, new Interval(0, 4, true)));
        yValues.add(new Pair<>(2, new Interval(4, 8, true)));
        yValues.add(new Pair<>(-1, new Interval(8, 13, true)));
        yValues.add(new Pair<>(1, new Interval(13, 24)));
        ySignal[0] = valuesFromIntervals(yValues);

        MultiValuedTrace trace = new MultiValuedTrace(1, TRACE_LENGTH);
        trace.setDimension(xSignal, X_SIGNAL)
             .setDimension(ySignal, Y_SIGNAL)
             .initialize();

        return trace.getSignals().get(0); //list discarded (only one location)
    }

    /**
     * Given a piecewise constant integer function, returns the corresponding list of values.
     * @param function function piecewise definitions
     * @return Array of function values
     */
    private static Integer[] valuesFromIntervals(List<Pair<Integer, Interval>> function) {
        int end = (int) function.get(function.size() - 1).getSecond().getEnd();

        Integer[] data = new Integer[end];

        for(int i = 0; i < end; i++) {
            for (Pair<Integer, Interval> piece : function) {
                if (piece.getSecond().contains(i)) {
                    data[i] = piece.getFirst();
                    break;
                }
            }
        }

        return data;
    }

    /**
     * Given a number, returns a singleton Interval containing it
     * @param number the numeric value contained in the interval
     * @return an Interval containing only that number.
     */
    private static Interval toInterval(Number number) {
        return new Interval((Integer) number, (Integer) number);
    }
}