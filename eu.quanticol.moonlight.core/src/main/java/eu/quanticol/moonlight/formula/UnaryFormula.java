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

import java.util.function.Function;

import eu.quanticol.moonlight.signal.Signal;

/**
 *
 */
public class UnaryFormula<R,T> implements Formula<T, R> {

	private final Function<R,R> op;
	private final Formula<T, R> argument;
	
	public UnaryFormula(Function<R, R> op, Formula<T,R> argument) {
		this.argument = argument;
		this.op = op;
	}

	@Override
	public Signal<R> check(Parameters p, Signal<T> s) {
		return argument.check(p, s).apply(op);
	}

}
