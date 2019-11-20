/*
 MoonLight: a light-weight framework for runtime monitoring
 Copyright (C) 2018

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
package eu.quanticol.moonlight.tests;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.formula.Formula;
import eu.quanticol.moonlight.formula.Parameters;
import eu.quanticol.moonlight.monitoring.TemporalMonitoring;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.Assignment;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCreatorDouble;
import eu.quanticol.moonlight.signal.VariableArraySignal;
import eu.quanticol.moonlight.util.FormulaGenerator;
import eu.quanticol.moonlight.util.FutureFormulaGenerator;
import org.junit.jupiter.api.Test;

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
        Map<String, Function<Double, Double>> functionalMap = new HashMap<>();
        functionalMap.put("a", t -> Math.pow(t, 2.));
        functionalMap.put("b", Math::cos);
        functionalMap.put("c", Math::sin);
        SignalCreatorDouble signalCreator = new SignalCreatorDouble(functionalMap);
        VariableArraySignal signal = signalCreator.generate(0, 1, 0.1);
        FormulaGenerator formulaGenerator = new FutureFormulaGenerator(new Random(seed), signal.getEnd(), signalCreator.getVariableNames());
        Formula generatedFormula = formulaGenerator.getFormula(formulaLength);
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


