/*******************************************************************************
 * MoonLight: a light-weight framework for runtime monitoring
 * Copyright (C) 2018 
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
package eu.quanticol.moonlight.tests;

import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.io.JSonSignalReader;
import eu.quanticol.moonlight.signal.Assignment;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.VariableArraySignal;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class TestFormula {

    @Test
    public void testFormula() {
        //formula
        Formula a = new AtomicFormula("a");
        Formula b = new AtomicFormula("b");
        Formula aeb = new AndFormula(a, b);
        //signal
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("trace.json").getFile());
        try {
            String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            VariableArraySignal signal = JSonSignalReader.readSignal(contents);
            HashMap<String, Function<Parameters, Function<Assignment, Boolean>>> mappa = new HashMap<>();
            int index_of_x = 0;
            //a is the atomic proposition: G>2
            mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class) > 2);
            //a is the atomic proposition: G<5
            mappa.put("b", y -> assignment -> assignment.get(index_of_x, Double.class) < 5);
            //TemporalMonitoring<Assignment, Double> monitoring = new TemporalMonitoring<Assignment, Double>(mappa, new DoubleDomain());
            TemporalMonitoring<Assignment, Boolean> monitoring = new TemporalMonitoring<Assignment, Boolean>(mappa, new BooleanDomain());
            Function<Signal<Assignment>, Signal<Boolean>> m = monitoring.monitor(aeb, null);
            Signal<Boolean> outputSignal = m.apply(signal);
            assertFalse(outputSignal.getIterator().next(0));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRobustnessEzio1() {
        //FORMULA: (y<=30)/\(y>=-30)
        //TALIRO: (0,30)
        //BREACH: (0,30)
        double expectedRobustnessInZero = 30.0;
        Formula a = new AtomicFormula("a");
        Formula b = new AtomicFormula("b");
        Formula aeb = new AndFormula(a, b);
        //signal
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceEzio.json").getFile());
        try {
            String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            VariableArraySignal signal = JSonSignalReader.readSignal(contents);
            HashMap<String, Function<Parameters, Function<Assignment, Double>>> mappa = new HashMap<>();
            int index_of_x = 0;
            //a is the atomic proposition: y>=-30
            mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class)+30);
            //b is the atomic proposition: y<=30
            mappa.put("b", y -> assignment -> -assignment.get(index_of_x, Double.class)+30);
            TemporalMonitoring<Assignment, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
            Function<Signal<Assignment>, Signal<Double>> m = monitoring.monitor(aeb, null);
            Signal<Double> outputSignal = m.apply(signal);
            assertEquals(expectedRobustnessInZero,outputSignal.getIterator().next(0),1E-15);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRobustnessEzio2() {
        //FORMULA: <>_[926,934]((y<=30)/\(y>=-30))
        //TALIRO: (0,27)
        //BREACH: (0,27)
        //formula
        double expectedRobustnessInZero = 27;
        Formula a = new AtomicFormula("a");
        Formula b = new AtomicFormula("b");
        Formula conjunction = new AndFormula(a, b);
        Formula eventually = new EventuallyFormula(conjunction,y->new Interval(926,934));
        //signal
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("traceEzio.json").getFile());
        try {
            String contents = new String(Files.readAllBytes(Paths.get(file.toURI())));
            VariableArraySignal signal = JSonSignalReader.readSignal(contents);
            HashMap<String, Function<Parameters, Function<Assignment, Double>>> mappa = new HashMap<>();
            int index_of_x = 0;
            //a is the atomic proposition: y>=-30
            mappa.put("a", y -> assignment -> assignment.get(index_of_x, Double.class)+30);
            //b is the atomic proposition: y<=30
            mappa.put("b", y -> assignment -> -assignment.get(index_of_x, Double.class)+30);
            TemporalMonitoring<Assignment, Double> monitoring = new TemporalMonitoring<>(mappa, new DoubleDomain());
            Function<Signal<Assignment>, Signal<Double>> m = monitoring.monitor(eventually, null);
            Signal<Double> outputSignal = m.apply(signal);
            assertEquals(expectedRobustnessInZero,outputSignal.getIterator().next(0),1E-15);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }



}


