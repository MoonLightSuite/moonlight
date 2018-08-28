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
package eu.quanticol.moonlight.formula;

import java.util.function.BiFunction;

import eu.quanticol.moonlight.signal.Signal;

/**
 *
 */
public class TemporalFormula<T,R> implements Formula<T,R> {

	private final Formula<T,R> argument;
	private final BiFunction<R,R,R> aggregator;
	private double a;
	private double b;
	
	public TemporalFormula(double a, double b, BiFunction<R,R,R> aggregator, Formula<T, R> argument) {
		super();
		this.a = a;
		this.b = b;
		this.aggregator = aggregator;
		this.argument = argument;
	}

	@Override
	public Signal<R> check(Parameters p, Signal<T> s) {
		Signal<R> s1 = argument.check(p, s);
		SlidingWindow<R> w = new SlidingWindow<R>(a,b,aggregator); 
		return w.apply( s1 );
	}

}
