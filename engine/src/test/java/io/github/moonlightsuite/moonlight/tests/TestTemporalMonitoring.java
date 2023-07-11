/*
 MoonLight: a light-weight framework for runtime monitoring
 Copyright (C) 2018-2021

 See the NOTICE file distributed with this work for additional information
 regarding copyright ownership.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package io.github.moonlightsuite.moonlight.tests;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import io.github.moonlightsuite.moonlight.core.base.Pair;
import io.github.moonlightsuite.moonlight.core.formula.Formula;
import io.github.moonlightsuite.moonlight.formula.*;
import io.github.moonlightsuite.moonlight.formula.temporal.EventuallyFormula;
import io.github.moonlightsuite.moonlight.offline.monitoring.TemporalMonitoring;
import io.github.moonlightsuite.moonlight.offline.monitoring.temporal.TemporalMonitor;
import io.github.moonlightsuite.moonlight.core.base.MoonLightRecord;
import io.github.moonlightsuite.moonlight.offline.signal.RecordHandler;
import io.github.moonlightsuite.moonlight.offline.signal.Signal;
import io.github.moonlightsuite.moonlight.offline.signal.SignalCreator;
import io.github.moonlightsuite.moonlight.core.io.DataHandler;
import io.github.moonlightsuite.moonlight.offline.signal.VariableArraySignal;
import io.github.moonlightsuite.moonlight.domain.DoubleDomain;
import io.github.moonlightsuite.moonlight.core.formula.Interval;
import io.github.moonlightsuite.moonlight.util.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestTemporalMonitoring {

    @Test
    void testFormula1() {
        test(1, 3);
    }

    @Test
    void testFormula2() {
        test(5,3);

        //IllegalArgument
        //test(6,3);

        //IllegalArgument
        //test(8,3);

        //IllegalArgument
        //test(12,3);

        //test(1,4);
    }

    @Test
    void testFormula3() {
    	test(6,3);
    }

    @Test
    void testFormula4() {
        test(8,3);
    }

    @Test
    void testFormula5() {
        test(12,3);
    }

    @Test
    void testFormula6() {
    	test(1,4);
    }

    private void test(int seed, int formulaLength) {
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
        VariableArraySignal signal = signalCreator.generate(0, 1, 0.1);
        FormulaGenerator formulaGenerator = new FutureFormulaGenerator(new Random(seed), signal.getEnd(), signalCreator.getVariableNames());
        Formula generatedFormula = formulaGenerator.getFormula(formulaLength);
        HashMap<String, Function<Parameters, Function<MoonLightRecord, Double>>> mappa = new HashMap<>();
        //a is the atomic proposition: a>=0
        mappa.put("a", y -> assignment -> assignment.get(signal.getVariableIndex("a"), Double.class));
        mappa.put("b", y -> assignment -> assignment.get(signal.getVariableIndex("b"), Double.class));
        mappa.put("c", y -> assignment -> assignment.get(signal.getVariableIndex("c"), Double.class));
        TemporalMonitoring<MoonLightRecord, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
        TemporalMonitor<MoonLightRecord, Double> m = monitoring.monitor(generatedFormula);
        Signal<Double> outputSignal = m.monitor(signal);
        outputSignal.getIterator(true).getCurrentValue();
    }

    @Test void testGloballyOutOfTime() {
        double[] times = new double[] {0.0, 0.1, 0.2, 0.3, 0.5, 0.6};
        Signal<Double> signal = SignalGenerator.createSignal(times, d -> d);
        TemporalMonitor<Double,Double> monitor = TemporalMonitor.globallyMonitor(TemporalMonitor.atomicMonitor(d -> d),
                new DoubleDomain(),new Interval(0.5,10.0));
        Signal<Double> output = monitor.monitor(signal);
        assertTrue(output.isEmpty());
    }

    @Test void testEventuallyOutOfTime() {
        double[] times = new double[] {0.0, 0.1, 0.2, 0.3, 0.5, 0.6};
        Signal<Double> signal = SignalGenerator.createSignal(times, d -> d);
        TemporalMonitor<Double,Double> monitor = TemporalMonitor.eventuallyMonitor(TemporalMonitor.atomicMonitor(d -> d),
                new DoubleDomain(),new Interval(0.5,10.0));
        Signal<Double> output = monitor.monitor(signal);
        assertTrue(output.isEmpty());
    }

    @Test void testGloballySameTime() {
        double[] times = new double[] {0.0, 0.1, 0.2, 0.3, 0.5, 0.6};
        Signal<Double> signal = SignalGenerator.createSignal(times, d -> d);
        TemporalMonitor<Double,Double> monitor = TemporalMonitor.globallyMonitor(TemporalMonitor.atomicMonitor(d -> d),
                new DoubleDomain(),new Interval(0.0,0.6));
        Signal<Double> output = monitor.monitor(signal);
        assertEquals(1,output.size());
        assertEquals( 0.0, output.getValueAt(0.0));
    }

    @Test void testEventuallySameTime() {
        double[] times = new double[] {0.0, 0.1, 0.2, 0.3, 0.5, 0.6};
        Signal<Double> signal = SignalGenerator.createSignal(times, d -> d);
        TemporalMonitor<Double,Double> monitor = TemporalMonitor.eventuallyMonitor(TemporalMonitor.atomicMonitor(d -> d),
                new DoubleDomain(),new Interval(0.0,0.6));
        Signal<Double> output = monitor.monitor(signal);
        assertEquals(1,output.size());
        assertEquals( 0.6, output.getValueAt(0.0));
    }

    @Test void testUntilExceed() {
        double[] times = new double[] {0.0, 0.1, 0.2, 0.3, 0.5, 0.6};
        Signal<Double> signal = SignalGenerator.createSignal(times, d -> d);
        TemporalMonitor<Double,Double> monitor = TemporalMonitor.untilMonitor(TemporalMonitor.atomicMonitor(d -> d),
                new Interval(0.0,1.0),
                TemporalMonitor.atomicMonitor(d -> d),
                new DoubleDomain());
        Signal<Double> output = monitor.monitor(signal);
        assertTrue(output.isEmpty());
    }

    @Test void testUntilSame() {
        double[] times = new double[] {0.0, 0.1, 0.2, 0.3, 0.5, 0.6};
        Signal<Double> signal = SignalGenerator.createSignal(times, d -> d);
        TemporalMonitor<Double,Double> monitor = TemporalMonitor.untilMonitor(TemporalMonitor.atomicMonitor(d -> d),
                new Interval(0.0,0.6),
                TemporalMonitor.atomicMonitor(d -> d),
                new DoubleDomain());
        Signal<Double> output = monitor.monitor(signal);
        assertFalse(output.isEmpty());
        assertEquals(1,output.size());
        assertEquals(0.0,output.getValueAt(0.0));
    }

    @Test
    void testeveWrongSimpleLaura() {
        double step = 0.1;
        double signalLowerBound = 0;
        double signalUpperBound = 0.5;
        double formulaLowerBound = 0.1;
        double formulaUpperBound = 0.3;
        double expectedOutputLowerBound = signalLowerBound;
        double expectedOutputUpperBound = signalUpperBound - formulaUpperBound;
        Signal<Double> signal = Utils.createSignal(signalLowerBound, signalUpperBound, step, x -> x);
        Formula phi = new EventuallyFormula(new AtomicFormula("test"), new Interval(formulaLowerBound, formulaUpperBound));
        TemporalMonitoring<Double, Double> monitoring = new TemporalMonitoring<>(new DoubleDomain());
        monitoring.addProperty("test", p -> (x -> x));
        TemporalMonitor<Double, Double> m = monitoring.monitor(phi);
        Signal<Double> result = m.monitor(signal);
        assertEquals(expectedOutputUpperBound, result.getEnd(), 0.0000001);
    }

}

