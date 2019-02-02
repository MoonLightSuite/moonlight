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

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;

import eu.quanticol.moonlight.formula.SlidingWindow;
import eu.quanticol.moonlight.signal.Sample;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCursor;


public class TestSignal {

	
	
	
	public <T> void checkSignal( Signal<T> s, double start, double end, int size, BiFunction<Double,T,Boolean> p, BiFunction<Double,Double,Boolean> step) {
		assertEquals(size,s.size());
		assertEquals("START",start,s.start(),0.0);
		assertEquals("END",end,s.end(),0.0);
		SignalCursor<T> si = s.getIterator(true);
		double previous = start;
		while (!si.completed()) {
			double t = si.time();
			T v = si.value();
			assertTrue("Time: "+t+" Value: "+v,p.apply(t, v));
			if (t!=start) {
				assertTrue("Step: "+previous+"->"+t,step.apply(previous, t));
			}
			previous = t;
			si.forward();
		}
	}
	
	@Test
	public void testCreations() {
		Signal<Boolean> s = new Signal<>();
		for( int i=0 ; i<100 ; i++ ) {
			s.add(i, i%2==0);
		}
		s.endAt(100);
		System.out.println(s);
		assertEquals("start:",0.0,s.start(),0.0);
		assertEquals("end:",100,s.end(),0.0);
		assertEquals(100,s.size());
	}

	@Test
	public void testCreations2() {
		Signal<Boolean> s = TestUtils.createSignal(0.0, 100, 1.0, x -> true);
		assertEquals("start:",0.0,s.start(),0.0);
		assertEquals("end:",100.0,s.end(),0.0);
		assertEquals(1,s.size());
	}
	
	@Test
	public void testIterator() {
		Signal<Boolean> s = TestUtils.createSignal(0.0, 100, 1.0, x -> x.intValue()%2==0);
		SignalCursor<Boolean> si = s.getIterator(true);
		double time = 0.0;
		while (time<100) {
			assertEquals("Time",time,si.time(),0.0);
			assertEquals("Value ("+time+")",((int) time)%2==0,si.value());
			time += 1.0;
			si.forward();
		}
	}

	@Test
	public void testUnaryApply() {
		Signal<Boolean> s = TestUtils.createSignal(0.0, 100, 1.0, x -> x.intValue()%2==0).apply(x -> !x);
		assertEquals("start:",0.0,s.start(),0.0);
		assertEquals("end:",100.0,s.end(),0.0);
		assertEquals(101,s.size());
	}


	@Test
	public void testUnaryApply2() {
		Signal<Boolean> s = TestUtils.createSignal(0.0, 100, 1.0, x -> x.intValue()%2==0).apply(x -> !x);
		checkSignal(s, 0.0, 100, 101, (x,y) -> (x<=100?y==(!(x.intValue()%2==0)):y==true), (x,y) -> (y-x)==1.0 );
	}
	
	@Test
	public void testBinaryApply() {
		Signal<Boolean> s1 = TestUtils.createSignal(0.0, 100, 1.0, x -> x.intValue()%2==0).apply(x -> !x);
		Signal<Boolean> s2 = TestUtils.createSignal(0.0, 100, 1.0, x -> x.intValue()%2!=0).apply(x -> !x);
		Signal<Boolean> s3 = Signal.apply(s1, (x, y)->x||y,s2);
		checkSignal(s3,0.0,100,1, (x,y) -> true, (x,y) -> (x==0.0)&&(y==100.0));
	}

	@Test
	public void testValues() {
		Signal<Boolean> s = new Signal<>();
		for( int i=0 ; i<100 ; i++ ) {
			s.add(i, i%2==0);
		}
		s.endAt(100);
		assertTrue(true);
	}
	
	@Test
	public void testBinaryApply2() {
		Signal<Boolean> s1 = TestUtils.createSignal(50.0, 150, 1.0, x -> x.intValue()%2==0).apply(x -> !x);
		Signal<Boolean> s2 = TestUtils.createSignal(0.0, 100, 1.0, x -> x.intValue()%2!=0).apply(x -> !x);
		Signal<Boolean> s3 = Signal.apply(s1, (x, y)->x||y,s2);
		checkSignal(s3,50.0,100,1, (x,y) -> true, (x,y) -> (x==50.0)&&(y==100.0));
	}


	@Test
	public void testSlidingWindow1() {
		Signal<Double> s1 = TestUtils.createSignal(0.0, 20.0, 0.4, x -> Math.sin(x) );
		SlidingWindow<Double> w = new SlidingWindow<>(0.25, 5.33, Math::min,true);
		Signal<Double> s2 = w.apply(s1);
		assertNotNull(s2);
		assertEquals(s1.end()-5.33, s2.end(),0.0);
	}
	
	@Test
	public void testSlidingWindow2() {
		Signal<Double> s1 = TestUtils.createSignal(0.0, 10, 0.45, x -> ( (((int) (x*2))%2==0)?-1.0:1.0) );
		SlidingWindow<Double> w = new SlidingWindow<>(0.25, 5.75, Math::min,true);
		Signal<Double> s2 = w.apply(s1);
		assertNotNull(s2);
		assertEquals(s1.end()-5.75, s2.end(),0.0);
	}
	
	
	
}


