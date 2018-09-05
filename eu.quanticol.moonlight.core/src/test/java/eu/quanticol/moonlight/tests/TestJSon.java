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

import eu.quanticol.moonlight.signal.Assignment;
import eu.quanticol.moonlight.signal.Sample;
import eu.quanticol.moonlight.signal.SignalIterator;
import eu.quanticol.moonlight.signal.VariableArraySignal;
import eu.uanticol.moonlight.io.JSonSignalReader;


public class TestJSon {

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
	
	
	
}


