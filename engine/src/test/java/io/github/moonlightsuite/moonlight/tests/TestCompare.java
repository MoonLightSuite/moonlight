/*******************************************************************************
 * MoonLight: a light-weight framework for runtime monitoring
 * Copyright (C) 2018-2021
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package io.github.moonlightsuite.moonlight.tests;

import io.github.moonlightsuite.moonlight.core.formula.Formula;
import io.github.moonlightsuite.moonlight.formula.*;
import io.github.moonlightsuite.moonlight.formula.classic.AndFormula;
import io.github.moonlightsuite.moonlight.formula.classic.NegationFormula;
import io.github.moonlightsuite.moonlight.formula.temporal.EventuallyFormula;
import io.github.moonlightsuite.moonlight.formula.temporal.GloballyFormula;
import io.github.moonlightsuite.moonlight.io.json.IllegalFileFormat;
import io.github.moonlightsuite.moonlight.io.json.JSonTemporalSignalDeserializer;
import io.github.moonlightsuite.moonlight.offline.monitoring.TemporalMonitoring;
import io.github.moonlightsuite.moonlight.offline.monitoring.temporal.TemporalMonitor;
import io.github.moonlightsuite.moonlight.core.base.MoonLightRecord;
import io.github.moonlightsuite.moonlight.offline.signal.RecordHandler;
import io.github.moonlightsuite.moonlight.offline.signal.Signal;
import io.github.moonlightsuite.moonlight.offline.signal.SignalCursor;
import io.github.moonlightsuite.moonlight.core.io.DataHandler;
import io.github.moonlightsuite.moonlight.domain.BooleanDomain;
import io.github.moonlightsuite.moonlight.domain.DoubleDomain;
import io.github.moonlightsuite.moonlight.core.formula.Interval;
import io.github.moonlightsuite.moonlight.core.base.Pair;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class TestCompare {

    @Test
    void traceGeneric() throws IllegalFileFormat {
        //formula
        Formula a = new AtomicFormula("a");
        Formula b = new AtomicFormula("b");
        Formula aeb = new AndFormula(a, b);
        //signal
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceGeneric/trace.json").getFile());
        try {
        	RecordHandler factory = RecordHandler.createFactory(
        		new Pair<String,DataHandler<?>>("G",DataHandler.REAL),
        		new Pair<String,DataHandler<?>>("I",DataHandler.REAL),
        		new Pair<String,DataHandler<?>>("X",DataHandler.REAL)
        	);

            String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            Signal<MoonLightRecord> signal = new JSonTemporalSignalDeserializer(factory).load(contents);
            HashMap<String, Function<Parameters, Function<MoonLightRecord, Boolean>>> mappa = new HashMap<>();
            int index_of_x = 0;
            //a is the atomic proposition: G>2
            mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class) > 2);
            //a is the atomic proposition: G<5
            mappa.put("b", y -> assignment -> assignment.get(index_of_x, Double.class) < 5);
            //TemporalMonitoring<Assignment, Double> monitoring = new TemporalMonitoring<Assignment, Double>(mappa, new DoubleDomain());
            TemporalMonitoring<MoonLightRecord, Boolean> monitoring = new TemporalMonitoring<>(mappa, new BooleanDomain());
            TemporalMonitor<MoonLightRecord,Boolean> m = monitoring.monitor(aeb);
            Signal<Boolean> outputSignal = m.monitor(signal);
            assertFalse(outputSignal.getValueAt(0.0));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testTraceEzio1() throws IllegalFileFormat {
        //FORMULA: (y<=30)/\(y>=-30)
        //TALIRO: (0,30)
        //BREACH: (0,30)
        double expectedRobustnessInZero = 30.0;
        Formula a = new AtomicFormula("a");
        Formula b = new AtomicFormula("b");
        Formula aeb = new AndFormula(a, b);
        //signal
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceEzio/traceEzio.json").getFile());
        try {
        	RecordHandler factory = RecordHandler.createFactory(
            		new Pair<String,DataHandler<?>>("y",DataHandler.REAL)
            	);
        	String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            Signal<MoonLightRecord> signal = new JSonTemporalSignalDeserializer(factory).load(contents);
            HashMap<String, Function<Parameters, Function<MoonLightRecord, Double>>> mappa = new HashMap<>();
            int index_of_x = 0;
            //a is the atomic proposition: y>=-30
            mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class) + 30);
            //b is the atomic proposition: y<=30
            mappa.put("b", y -> assignment -> -assignment.get(index_of_x, Double.class) + 30);
            TemporalMonitoring<MoonLightRecord, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
            TemporalMonitor<MoonLightRecord,Double> m = monitoring.monitor(aeb);
            Signal<Double> outputSignal = m.monitor(signal);
            assertEquals(expectedRobustnessInZero, outputSignal.getValueAt(0.0), 1E-15);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testTraceEzio2() throws IllegalFileFormat {
        //FORMULA: <>_[926,934]((y<=30)/\(y>=-30))
        //TALIRO: (0,27)
        //BREACH: (0,27)
        //formula
        double expectedRobustnessInZero = 27;
        Formula a = new AtomicFormula("a");
        Formula b = new AtomicFormula("b");
        Formula conjunction = new AndFormula(a, b);
        Formula eventually = new EventuallyFormula(conjunction, new Interval(926, 934));
        //signal
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceEzio/traceEzio.json").getFile());
        try {
        	RecordHandler factory = RecordHandler.createFactory(
            		new Pair<String,DataHandler<?>>("y",DataHandler.REAL)
            	);
        	String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            Signal<MoonLightRecord> signal = new JSonTemporalSignalDeserializer(factory).load(contents);
            long timeInit = System.currentTimeMillis();
            HashMap<String, Function<Parameters, Function<MoonLightRecord, Double>>> mappa = new HashMap<>();
            int index_of_x = 0;
            //a is the atomic proposition: y>=-30
            mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class) + 30);
            //b is the atomic proposition: y<=30
            mappa.put("b", y -> assignment -> -assignment.get(index_of_x, Double.class) + 30);
            TemporalMonitoring<MoonLightRecord, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
            TemporalMonitor<MoonLightRecord, Double> m = monitoring.monitor(eventually);
            Signal<Double> outputSignal = m.monitor(signal);
            long timeEnd = System.currentTimeMillis();
            assertEquals(expectedRobustnessInZero, outputSignal.getValueAt(0.0), 1E-15);
            System.out.println("TIME MoonLight: " + (timeEnd - timeInit) / 1000.);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }


    @Test
    void testIdentity() throws IllegalFileFormat {
        //FORMULA: []_[0,500](a>=0)
        //TALIRO: //
        //BREACH: //
        //formula
        //double expectedRobustnessInZero = 0;
        Formula a = new AtomicFormula("a");
        Formula globallyFormula = new GloballyFormula(a, new Interval(0, 500));
        //signal
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceIdentity/traceLaura.json").getFile());
        try {
        	RecordHandler factory = RecordHandler.createFactory(
            		new Pair<String,DataHandler<?>>("a",DataHandler.REAL),
            		new Pair<String,DataHandler<?>>("b",DataHandler.REAL)
            	);
        	String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            Signal<MoonLightRecord> signal = new JSonTemporalSignalDeserializer(factory).load(contents);
            long timeInit = System.currentTimeMillis();
            HashMap<String, Function<Parameters, Function<MoonLightRecord, Double>>> mappa = new HashMap<>();
            int index_of_x = 0;
            //a is the atomic proposition: a>=0
            mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class));
            TemporalMonitoring<MoonLightRecord, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
            TemporalMonitor<MoonLightRecord, Double> m = monitoring.monitor(globallyFormula);
            Signal<Double> outputSignal = m.monitor(signal);
            long timeEnd = System.currentTimeMillis();
            SignalCursor<Double, MoonLightRecord> expected = signal.getIterator(true);
            SignalCursor<Double, Double> actual = outputSignal.getIterator(true);
            while (!actual.isCompleted()) {
                assertFalse(expected.isCompleted());
                Double valueActual = actual.getCurrentValue();
                MoonLightRecord valueExpected = expected.getCurrentValue();
                assertEquals(valueExpected.get(0, Double.class), valueActual);
                expected.forward();
                actual.forward();
            }
            System.out.println("TIME MoonLight: " + (timeEnd - timeInit) / 1000.);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testRobustnessLaura2() throws IllegalFileFormat {
        //FORMULA: !<>_[0,500]!(a>=0)
        //TALIRO: //
        //BREACH: //
        Formula a = new AtomicFormula("a");
        Formula notA = new NegationFormula(a);
        Formula eventually = new EventuallyFormula(notA, new Interval(0, 500));
        Formula notEventuallyNotA = new NegationFormula(eventually);
        //signal
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceIdentity/traceLaura.json").getFile());
        try {
        	RecordHandler factory = RecordHandler.createFactory(
            		new Pair<String,DataHandler<?>>("a",DataHandler.REAL),
            		new Pair<String,DataHandler<?>>("b",DataHandler.REAL)
            	);
        	String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            Signal<MoonLightRecord> signal = new JSonTemporalSignalDeserializer(factory).load(contents);
            HashMap<String, Function<Parameters, Function<MoonLightRecord, Double>>> mappa = new HashMap<>();
            int index_of_x = 0;
            //a is the atomic proposition: a>=0
            mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class));
            TemporalMonitoring<MoonLightRecord, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
            TemporalMonitor<MoonLightRecord, Double> m = monitoring.monitor(notEventuallyNotA);
            Signal<Double> outputSignal = m.monitor(signal);
            SignalCursor<Double, MoonLightRecord> expected = signal.getIterator(true);
            SignalCursor<Double, Double> actual = outputSignal.getIterator(true);
            assertEquals(500.0, outputSignal.getEnd());
            while (!actual.isCompleted()) {
                assertFalse(expected.isCompleted());
                Double nextActual = actual.getCurrentValue();
                MoonLightRecord nextExpected = expected.getCurrentValue();
                double time = expected.getCurrentTime();
//                if (time > 500) {
//                    break;
//                }
                assertEquals(nextExpected.get(0, Double.class), nextActual, "Time: " + time);
                actual.forward();
                expected.forward();
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }


}


