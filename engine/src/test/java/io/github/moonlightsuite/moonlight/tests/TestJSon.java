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

import io.github.moonlightsuite.moonlight.core.formula.Formula;
import io.github.moonlightsuite.moonlight.formula.*;
import io.github.moonlightsuite.moonlight.formula.classic.AndFormula;
import io.github.moonlightsuite.moonlight.formula.classic.NegationFormula;
import io.github.moonlightsuite.moonlight.formula.classic.OrFormula;
import io.github.moonlightsuite.moonlight.formula.temporal.*;
import io.github.moonlightsuite.moonlight.io.FormulaJSonIO;
import io.github.moonlightsuite.moonlight.io.json.IllegalFileFormat;
import io.github.moonlightsuite.moonlight.io.json.JSonTemporalSignalDeserializer;
import io.github.moonlightsuite.moonlight.core.base.MoonLightRecord;
import io.github.moonlightsuite.moonlight.offline.signal.RecordHandler;
import io.github.moonlightsuite.moonlight.offline.signal.Signal;
import io.github.moonlightsuite.moonlight.offline.signal.SignalCursor;
import io.github.moonlightsuite.moonlight.core.io.DataHandler;
import io.github.moonlightsuite.moonlight.core.formula.Interval;
import io.github.moonlightsuite.moonlight.util.BothFormulaGenerator;
import io.github.moonlightsuite.moonlight.util.FormulaGenerator;
import io.github.moonlightsuite.moonlight.core.base.Pair;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class TestJSon {

    private final FormulaGenerator generator = new BothFormulaGenerator("a", "b", "c");
    private final FormulaJSonIO fJSonIO = FormulaJSonIO.getInstance();


    @Test
    void readSignal() throws IllegalFileFormat {
        String code = "{\n" +
                " 	\"trace_type\"  : \"temporal\",\n" +
                " 	\"t\"       	: [0, 0.5, 0.7, 0.8, 15],\n" +
                " 	\"signals\" :   [ {\"name\": \"x\", \"type\" : \"boolean\", \n" +
                "                       \"values\" : [true, true, false, true, false]},\n" +
                "                 	{\"name\": \"y\", \"type\" : \"real\",	\n" +
                "                     \"values\" : [15.6, -14.6, 15.7, 20.3, 25.5 ]},\n" +
                "                 	{\"name\": \"z\", \"type\" : \"integer\", \n" +
                "                     \"values\" : [ 3, 	5,	6,	5,	6   ]}]\n" +
                "}\n" +
                "";
        Object[][] values =
                new Object[][]{
                        {true, 15.6, 3},
                        {true, -14.6, 5},
                        {false, 15.7, 6},
                        {true, 20.3, 5},
                        {false, 25.5, 6}
                };
        double[] times = new double[]{0, 0.5, 0.7, 0.8, 15};

        Class<?>[] types = new Class<?>[]{Boolean.class, Double.class, Integer.class};

        System.out.println(code);
    	RecordHandler factory = RecordHandler.createFactory(
        		new Pair<String,DataHandler<?>>("x",DataHandler.BOOLEAN),
        		new Pair<String,DataHandler<?>>("y",DataHandler.REAL),
        		new Pair<String,DataHandler<?>>("z",DataHandler.INTEGER)
        	);
        Signal<MoonLightRecord> signal = new JSonTemporalSignalDeserializer(factory).load(code);
        assertNotNull(signal);
        assertEquals(0, factory.getVariableIndex("x"));
        assertEquals(1, factory.getVariableIndex("y"));
        assertEquals(2, factory.getVariableIndex("z"));
        SignalCursor<Double, MoonLightRecord> iterator = signal.getIterator(true);
        for (int i = 0; i < times.length; i++) {
            assertFalse(iterator.isCompleted());
            MoonLightRecord next = iterator.getCurrentValue();
            assertEquals(times[i], iterator.getCurrentTime(), 0.0);
            for (int j = 0; j < 3; j++) {
                assertEquals(values[i][j], next.get(j, types[j]));
            }
            iterator.forward();
        }
    }

    @Test
    void testFormulaGenerator() {
        for (int i = 0; i < 10; i++) {
            Formula f1 = generator.getFormula();
            assertNotNull(f1);
        }
    }

    @Disabled("json formulae are not supported anymore")
    @Test
    void testFormulaSimple() {
        AtomicFormula a1 = new AtomicFormula("a");
        testSerializeDeserialize(a1);
        AtomicFormula a1b = new AtomicFormula("b");
        testSerializeDeserialize(a1b);
        AndFormula a2 = new AndFormula(a1, a1);
        testSerializeDeserialize(a2);
        OrFormula a3 = new OrFormula(a2, a2);
        testSerializeDeserialize(a3);
        EventuallyFormula a4 = new EventuallyFormula(a3, new Interval(0, 100));
        testSerializeDeserialize(a4);
        GloballyFormula a5 = new GloballyFormula(a4, new Interval(0, 100));
        testSerializeDeserialize(a5);
        UntilFormula a6 = new UntilFormula(a5, a5, new Interval(0, 100));
        testSerializeDeserialize(a6);
        UntilFormula a7 = new UntilFormula(a5, a5);
        testSerializeDeserialize(a7);
        SinceFormula a8 = new SinceFormula(a1, a1b, new Interval(0, 100));
        testSerializeDeserialize(a8);
        HistoricallyFormula a9 = new HistoricallyFormula(a1);
        testSerializeDeserialize(a9);
        HistoricallyFormula a10 = new HistoricallyFormula(a1, new Interval(0, 100));
        testSerializeDeserialize(a10);
        OnceFormula a11 = new OnceFormula(a1, new Interval(0, 100));
        testSerializeDeserialize(a11);
        OnceFormula a12 = new OnceFormula(a1);
        testSerializeDeserialize(a12);
        NegationFormula a13 = new NegationFormula(a1);
        testSerializeDeserialize(a13);
    }


    private void testSerializeDeserialize(Formula f1) {
        String code = fJSonIO.toJson(f1);
        Formula f2 = fJSonIO.fromJson(code);
        assertEquals(f1, f2);
    }

    @Disabled("json formulae are not supported anymore")
    @Test
    void testFormulaJson() {
        for (int i = 0; i < 1000; i++) {
            Formula f1 = generator.getFormula();
            testSerializeDeserialize(f1);
        }
    }

}


