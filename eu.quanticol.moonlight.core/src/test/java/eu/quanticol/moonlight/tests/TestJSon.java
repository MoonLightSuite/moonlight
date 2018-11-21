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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.quanticol.moonlight.formula.AndFormula;
import eu.quanticol.moonlight.formula.AtomicFormula;
import eu.quanticol.moonlight.formula.EventuallyFormula;
import eu.quanticol.moonlight.formula.Formula;
import eu.quanticol.moonlight.formula.GloballyFormula;
import eu.quanticol.moonlight.formula.HystoricallyFormula;
import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.formula.NegationFormula;
import eu.quanticol.moonlight.formula.OnceFormula;
import eu.quanticol.moonlight.formula.OrFormula;
import eu.quanticol.moonlight.formula.SinceFormula;
import eu.quanticol.moonlight.formula.UntilFormula;
import eu.quanticol.moonlight.io.FormulaJSonIO;
import eu.quanticol.moonlight.io.JSonSignalReader;
import eu.quanticol.moonlight.signal.Assignment;
import eu.quanticol.moonlight.signal.Sample;
import eu.quanticol.moonlight.signal.SignalIterator;
import eu.quanticol.moonlight.signal.VariableArraySignal;
import eu.quanticol.moonlight.util.FormulaGenerator;


public class TestJSon {
	
	private FormulaGenerator generator = new FormulaGenerator("a","b","c");
	private FormulaJSonIO fJSonIO = FormulaJSonIO.getInstance();


	@Test
	public void readSignal() {
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
				new Object[][] { 
					{ true , 15.6 , 3 } ,
					{ true , -14.6 , 5 } ,
					{ false , 15.7 , 6 } ,
					{ true , 20.3 , 5 } ,
					{ false , 25.5 , 6 }    
				};
		double[] times = new double[] { 0, 0.5, 0.7, 0.8, 15};
		
		Class<?>[] types = new Class<?>[] { Boolean.class , Double.class , Integer.class };
		
		System.out.println(code);
		VariableArraySignal signal = JSonSignalReader.readSignal(code);
		assertNotNull(signal);
		assertEquals(0,signal.getVariableIndex("x"));
		assertEquals(1,signal.getVariableIndex("y"));
		assertEquals(2,signal.getVariableIndex("z"));
		SignalIterator<Assignment> iterator = signal.getIterator();
		for( int i=0 ; i<times.length ; i++ ) {
			assertTrue( iterator.hasNext() );
			Sample<Assignment> next = iterator.next();
			assertEquals(times[i],next.getTime(),0.0);
			for (int j=0 ; j<3 ; j++ ) {
				assertEquals(values[i][j],next.getValue().get(j, types[j]));
			}
		}
	}
	
	@Test
	public void testFormulaGenerator( ) {
		for( int i=0 ; i<10; i++ ) {
			Formula f1 = generator.getFormula();
			assertNotNull(f1);
		}
	}

	
	@Test
	public void testFormulaSimple( ) {
		AtomicFormula a1 = new AtomicFormula("a");
		testSerializeDeserialize(a1);
		AtomicFormula a1b = new AtomicFormula("b");
		testSerializeDeserialize(a1b);
		AndFormula a2 = new AndFormula(a1, a1);
		testSerializeDeserialize(a2);
		OrFormula a3 = new OrFormula(a2, a2);
		testSerializeDeserialize(a3);
		EventuallyFormula a4 = new EventuallyFormula(a3, new Interval(0,100));
		testSerializeDeserialize(a4);
		GloballyFormula a5 = new GloballyFormula(a4, new Interval(0,100));
		testSerializeDeserialize(a5);
		UntilFormula a6 = new UntilFormula(a5,a5, new Interval(0,100));
		testSerializeDeserialize(a6);
		UntilFormula a7 = new UntilFormula(a5,a5);
		testSerializeDeserialize(a7);
		SinceFormula a8 = new SinceFormula(a1,a1b, new Interval(0,100));
		testSerializeDeserialize(a8);
		HystoricallyFormula a9 = new HystoricallyFormula(a1);
		testSerializeDeserialize(a9);
		HystoricallyFormula a10 = new HystoricallyFormula(a1,new Interval(0,100));
		testSerializeDeserialize(a10);
		OnceFormula a11 = new OnceFormula(a1,new Interval(0,100));
		testSerializeDeserialize(a11);
		OnceFormula a12 = new OnceFormula(a1);
		testSerializeDeserialize(a12);
		NegationFormula a13 = new NegationFormula(a1);
		testSerializeDeserialize(a13);
	}
	
	
	
	private void testSerializeDeserialize(Formula f1) {
		String code = fJSonIO.toJson( f1 );
		Formula f2 = fJSonIO.fromJson( code );
		assertEquals(f1,f2);
	}

	@Test
	public void testFormulaJson( ) {
		for( int i=0 ; i<1000; i++ ) {
			Formula f1 = generator.getFormula();
			testSerializeDeserialize(f1);
		}
	}
	
}


