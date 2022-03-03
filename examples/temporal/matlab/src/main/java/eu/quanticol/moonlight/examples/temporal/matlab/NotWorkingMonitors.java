package eu.quanticol.moonlight.examples.temporal.matlab;

import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.core.formula.Formula;
import eu.quanticol.moonlight.formula.Parameters;
import eu.quanticol.moonlight.io.FormulaToTaliro;
import eu.quanticol.moonlight.monitoring.TemporalMonitoring;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.io.MoonLightRecord;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCreator;
import eu.quanticol.moonlight.signal.DataHandler;
import eu.quanticol.moonlight.signal.VariableArraySignal;
import eu.quanticol.moonlight.util.FormulaGenerator;
import eu.quanticol.moonlight.util.FutureFormulaGenerator;
import eu.quanticol.moonlight.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;


public class NotWorkingMonitors {
    private static FormulaToTaliro toTaliro = new FormulaToTaliro();

    public static void main(String[] args) {
        //IllegalArgument
        //psi ='( ( ! (b) U_[0.5752733232533694,10.656429075388495] ! (c) ) U_[73.08781907032805,84.1239890649923] ( ( a U_[6.321583316818297,6.85606905535994] b ) U_[5.517200031740415,8.563282221045833] ( c U_[1.1462474874941326,3.4784242997775863] b ) ) )';
        test(1, 3);

        //IllegalArgument
        //psi ='( []_[73.11469360198954,97.35039017579481] ( c \/ a ) )';
        //test(2,3);

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
        Map<String, Function<Double, ?>> functionalMap = new HashMap<>();
        functionalMap.put("a", t -> Math.pow(t, 2.));
        functionalMap.put("b", Math::cos);
        functionalMap.put("c", Math::sin);
        RecordHandler factory = RecordHandler.createFactory(
        		new Pair<>("a",DataHandler.REAL),
        		new Pair<>("b",DataHandler.REAL),
        		new Pair<>("c",DataHandler.REAL)
        );
        SignalCreator signalCreator = new SignalCreator(factory,functionalMap);
        VariableArraySignal signal = signalCreator.generate(0, 100, 0.1);
        FormulaGenerator formulaGenerator = new FutureFormulaGenerator(new Random(seed), signal.end(), signalCreator.getVariableNames());
        Formula generatedFormula = formulaGenerator.getFormula(formulaLength);
        System.out.println(toTaliro.toTaliro(generatedFormula));
        HashMap<String, Function<Parameters, Function<MoonLightRecord, Double>>> mappa = new HashMap<>();
        //a is the atomic proposition: a>=0
        mappa.put("a", y -> assignment -> assignment.get(0, Double.class));
        mappa.put("b", y -> assignment -> assignment.get(1, Double.class));
        mappa.put("c", y -> assignment -> assignment.get(2, Double.class));
        TemporalMonitoring<MoonLightRecord, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
        TemporalMonitor<MoonLightRecord, Double> m = monitoring.monitor(generatedFormula, null);
        Signal<Double> outputSignal = m.monitor(signal);
        outputSignal.getIterator(true).value();
    }
}
