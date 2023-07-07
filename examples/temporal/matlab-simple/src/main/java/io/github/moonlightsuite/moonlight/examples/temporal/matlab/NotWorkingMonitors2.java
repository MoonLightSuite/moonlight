package io.github.moonlightsuite.moonlight.examples.temporal.matlab;

import io.github.moonlightsuite.moonlight.core.base.MoonLightRecord;
import io.github.moonlightsuite.moonlight.core.base.Pair;
import io.github.moonlightsuite.moonlight.core.formula.Formula;
import io.github.moonlightsuite.moonlight.core.io.DataHandler;
import io.github.moonlightsuite.moonlight.domain.DoubleDomain;
import io.github.moonlightsuite.moonlight.formula.Parameters;
import io.github.moonlightsuite.moonlight.io.FormulaToTaliro;
import io.github.moonlightsuite.moonlight.offline.monitoring.TemporalMonitoring;
import io.github.moonlightsuite.moonlight.offline.monitoring.temporal.TemporalMonitor;
import io.github.moonlightsuite.moonlight.offline.signal.RecordHandler;
import io.github.moonlightsuite.moonlight.offline.signal.Signal;
import io.github.moonlightsuite.moonlight.offline.signal.SignalCreator;
import io.github.moonlightsuite.moonlight.offline.signal.VariableArraySignal;
import io.github.moonlightsuite.moonlight.util.FormulaGenerator;
import io.github.moonlightsuite.moonlight.util.FutureFormulaGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;


public class NotWorkingMonitors2 {
    private static final FormulaToTaliro toTaliro = new FormulaToTaliro();

    public static void main(String[] args) {
        //IllegalArgument
        //psi ='( []_[73.11469360198954,97.35039017579481] ( c \/ a ) )';
        test(2, 3);

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
                new Pair<>("a", DataHandler.REAL),
                new Pair<>("b", DataHandler.REAL),
                new Pair<>("c", DataHandler.REAL)
        );
        SignalCreator signalCreator = new SignalCreator(factory, functionalMap);
        VariableArraySignal signal = signalCreator.generate(0, 100, 0.1);
        FormulaGenerator formulaGenerator = new FutureFormulaGenerator(new Random(seed), signal.getEnd(), signalCreator.getVariableNames());
        Formula generatedFormula = formulaGenerator.getFormula(formulaLength);
        System.out.println(toTaliro.toTaliro(generatedFormula));
        HashMap<String, Function<Parameters, Function<MoonLightRecord, Double>>> mappa = new HashMap<>();
        //a is the atomic proposition: a>=0
        mappa.put("a", y -> assignment -> assignment.get(0, Double.class));
        mappa.put("b", y -> assignment -> assignment.get(1, Double.class));
        mappa.put("c", y -> assignment -> assignment.get(2, Double.class));
        TemporalMonitoring<MoonLightRecord, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
        TemporalMonitor<MoonLightRecord, Double> m = monitoring.monitor(generatedFormula);
        Signal<Double> outputSignal = m.monitor(signal);
        outputSignal.getIterator(true).getCurrentValue();
    }
}
