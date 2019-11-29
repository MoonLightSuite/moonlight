package eu.quanticol.moonlight.examples.temporal.matlab;

import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.formula.Formula;
import eu.quanticol.moonlight.formula.Parameters;
import eu.quanticol.moonlight.io.FormulaToTaliro;
import eu.quanticol.moonlight.monitoring.TemporalMonitoring;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.Assignment;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCreatorDouble;
import eu.quanticol.moonlight.signal.VariableArraySignal;
import eu.quanticol.moonlight.util.FormulaGenerator;
import eu.quanticol.moonlight.util.FutureFormulaGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;


public class NotWorkingMonitors2 {
    private static FormulaToTaliro toTaliro = new FormulaToTaliro();

    public static void main(String[] args) {
        //IllegalArgument
        //psi ='( []_[73.11469360198954,97.35039017579481] ( c \/ a ) )';
        test(2,3);

        //loop
        //psi ='( <>_[73.05198636144607,75.43037618911796] ( []_[23.217671731444288,24.334803932613358] ( b U_[0.16383672750155606,0.1835545260439489] a ) ) )';
        //test(5,3);

        //IllegalArgument
        //test(6,3);

        //IllegalArgument
        //test(8,3);

        //IllegalArgument
        //test(12,3);

        //test(1,4);
    }

    private static void test(int seed, int formulaLength) {
        Map<String, Function<Double, Double>> functionalMap = new HashMap<>();
        functionalMap.put("a", t -> Math.pow(t, 2.));
        functionalMap.put("b", Math::cos);
        functionalMap.put("c", Math::sin);
        SignalCreatorDouble signalCreator = new SignalCreatorDouble(functionalMap);
        VariableArraySignal signal = signalCreator.generate(0, 100, 0.1);
        FormulaGenerator formulaGenerator = new FutureFormulaGenerator(new Random(seed), signal.getEnd(), signalCreator.getVariableNames());
        Formula generatedFormula = formulaGenerator.getFormula(formulaLength);
        System.out.println(toTaliro.toTaliro(generatedFormula));
        HashMap<String, Function<Parameters, Function<Assignment, Double>>> mappa = new HashMap<>();
        //a is the atomic proposition: a>=0
        mappa.put("a", y -> assignment -> assignment.get(0, Double.class));
        mappa.put("b", y -> assignment -> assignment.get(1, Double.class));
        mappa.put("c", y -> assignment -> assignment.get(2, Double.class));
        TemporalMonitoring<Assignment, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
        TemporalMonitor<Assignment, Double> m = monitoring.monitor(generatedFormula, null);
        Signal<Double> outputSignal = m.monitor(signal);
        outputSignal.getIterator(true).value();
    }
}
