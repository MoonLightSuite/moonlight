package eu.quanticol.moonlight.formula;

import eu.quanticol.moonlight.monitoring.TemporalMonitoring;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.util.FormulaGenerator;
import eu.quanticol.moonlight.util.FutureFormulaGenerator;
import eu.quanticol.moonlight.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public class RoSIRandomTest {
    private static final int RANDOMNESS_SEED = 14;
    private static final int FORMULA_LENGTH = 5;

    public static void main(String[] args) {
        System.out.println(RANDOMNESS_SEED + "," + FORMULA_LENGTH + "-->");
        test();
    }

    private static void test() {
        try {
            // Signals generator...
            Pair<Signal<Record>, String[]> trace = traceGenerator();

            // Formula selection...
            Formula formula = testFormula(trace);

            // Generate Monitors...
            TemporalMonitor<Record, Interval> m = generateMonitoring(formula);

            Signal<Record> signal = trace.getFirst();
            int nReps = 20;
            for (int i = 0; i < nReps; i++) {
                Signal<Interval> outputSignal = m.monitor(signal);
                outputSignal.getIterator(true).value();
                //System.out.println("Output result:" + outputSignal.getIterator(true).value());
            }
            Interval value = m.monitor(signal).getIterator(true).value();
            System.out.println("Robustness result:" + value.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static TemporalMonitor<Record, Interval> generateMonitoring(Formula formula) {
        //a is the atomic proposition: a >= 0
        HashMap<String, Function<Parameters, Function<Record, Interval>>> atoms = new HashMap<>();
        atoms.put("a", y -> assignment -> assignment.get(0, Interval.class));
        atoms.put("b", y -> assignment -> assignment.get(1, Interval.class));
        atoms.put("c", y -> assignment -> assignment.get(2, Interval.class));

        TemporalMonitoring<Record, Interval> monitoring =
                new TemporalMonitoring<>(atoms, new IntervalDomain());

        return monitoring.monitor(formula, null);
    }

    private static Formula testFormula(Pair<Signal<Record>, String[]> trace) {
        Random random = new Random(RANDOMNESS_SEED);
        Signal<Record> signal = trace.getFirst();
        String[] vars = trace.getSecond();

        FormulaGenerator formulaGenerator = new FutureFormulaGenerator(random,
                                                        signal.getEnd(), vars);

        Formula f = formulaGenerator.getFormula(FORMULA_LENGTH);

        return new UntilFormula(new NegationFormula(f), f);
    }

    private static Pair<Signal<Record>, String[]> traceGenerator() {
        Map<String, Function<Double, ?>> functionalMap = new HashMap<>();
        functionalMap.put("a", t -> toInterval(Math.pow(t, 2.)));
        functionalMap.put("b", t-> toInterval(Math.cos(t)));
        functionalMap.put("c", t-> toInterval(Math.sin(t)));

        double timeStep = 1;
        double endTime = 100;

        RecordHandler factory = RecordHandler.createFactory(
                new Pair<>("a", DataHandler.INTERVAL),
                new Pair<>("b", DataHandler.INTERVAL),
                new Pair<>("c", DataHandler.INTERVAL)
        );

        SignalCreator signalCreator = new SignalCreator(factory, functionalMap);
        VariableArraySignal signal = signalCreator.generate(0, endTime, timeStep);

        return new Pair<>(signal, signalCreator.getVariableNames());
    }

    /**
     * Given a number, returns a singleton Interval containing it
     * @param number the numeric value contained in the interval
     * @return an Interval containing only that number.
     */
    private static Interval toInterval(Number number) {
        return new Interval((double) number, (double) number);
    }

}