package io.github.moonlightsuite.moonlight.util;

import io.github.moonlightsuite.moonlight.core.io.DataHandler;
import io.github.moonlightsuite.moonlight.core.base.Pair;
import io.github.moonlightsuite.moonlight.domain.DoubleDomain;
import io.github.moonlightsuite.moonlight.core.formula.Formula;
import io.github.moonlightsuite.moonlight.formula.Parameters;
import io.github.moonlightsuite.moonlight.offline.monitoring.TemporalMonitoring;
import io.github.moonlightsuite.moonlight.offline.monitoring.temporal.TemporalMonitor;
import io.github.moonlightsuite.moonlight.offline.signal.*;
import io.github.moonlightsuite.moonlight.core.base.MoonLightRecord;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertFalse;


class FormulaAndSignalGeneratorTest {

    @Test
    void test() {
        Map<String, Function<Double, ?>> functionalMap = new HashMap<>();
        functionalMap.put("a", t -> Math.pow(t, 2.));
        functionalMap.put("b", Math::cos);
        functionalMap.put("c", Math::sin);
        RecordHandler factory = RecordHandler.createFactory(
        		new Pair<>("a", DataHandler.REAL),
        		new Pair<>("b",DataHandler.REAL),
        		new Pair<>("c",DataHandler.REAL)
        );
        SignalCreator signalCreator = new SignalCreator(factory,functionalMap);
        VariableArraySignal signal = signalCreator.generate(0, 100, 0.1);
        FormulaGenerator formulaGenerator = new FutureFormulaGenerator(new Random(1), signal.getEnd(), signalCreator.getVariableNames());
        Formula generatedFormula = formulaGenerator.getFormula(3);
        System.out.println(generatedFormula.toString());
        long timeInit = System.currentTimeMillis();
        HashMap<String, Function<Parameters, Function<MoonLightRecord, Double>>> mappa = new HashMap<>();
        int index_of_x = 0;
        //a is the atomic proposition: a>=0
        mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class));
        mappa.put("b", y -> assignment -> assignment.get(index_of_x, Double.class));
        mappa.put("c", y -> assignment -> assignment.get(index_of_x, Double.class));
        TemporalMonitoring<MoonLightRecord, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
        TemporalMonitor<MoonLightRecord, Double> m = monitoring.monitor(generatedFormula);
        Signal<Double> outputSignal = m.monitor(signal);
        long timeEnd = System.currentTimeMillis();
        SignalCursor<Double, MoonLightRecord> expected = signal.getIterator(true);
        SignalCursor<Double, Double> actual = outputSignal.getIterator(true);
        while (!actual.isCompleted()) {
            assertFalse(expected.isCompleted());
            Double valueActual = actual.getCurrentValue();
            MoonLightRecord valueExpected = expected.getCurrentValue();
            // assertEquals(valueExpected.get(0, Double.class), valueActual);
            expected.forward();
            actual.forward();
        }
        System.out.println("TIME MoonLight: " + (timeEnd - timeInit) / 1000.);
    }
}