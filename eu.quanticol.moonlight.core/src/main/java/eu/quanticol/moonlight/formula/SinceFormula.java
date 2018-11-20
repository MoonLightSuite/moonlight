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

public class SinceFormula implements Formula {
	
	private final Formula firstArgument;
	
	private final Formula secondArgument;
	
	private final Interval interval;
	
	public SinceFormula( Formula firstArgument , Formula secondArgument ) {
		this(firstArgument,secondArgument,null);
	}

	public SinceFormula(Formula firstArgument, Formula secondArgument, Interval interval) {
		this.firstArgument = firstArgument;
		this.secondArgument = secondArgument;
		this.interval = interval;
	}

	@Override
	public <T, R> R accept(FormulaVisitor<T, R> visitor, T parameters) {
		return visitor.visit(this, parameters);
	}

	/**
	 * @return the left
	 */
	public Formula getFirstArgument() {
		return firstArgument;
	}

	/**
	 * @return the right
	 */
	public Formula getSecondArgument() {
		return secondArgument;
	}

	/**
	 * @return the interval
	 */
	public Interval getInterval() {
		return interval;
	}

	
	
	public boolean isUnbounded() {
		return (interval != null);
	}
	
}
