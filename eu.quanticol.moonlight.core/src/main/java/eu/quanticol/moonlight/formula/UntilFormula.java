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

public class UntilFormula implements Formula {
	
	private final Formula left;
	
	private final Formula right;
	
	private final Function<Parameters,Interval> interval;
	
	public UntilFormula( Formula left , Formula right ) {
		this(left,right,null);
	}

	public UntilFormula(Formula left, Formula right, Function<Parameters,Interval> interval) {
		this.left = left;
		this.right = right;
		this.interval = interval;
	}

	@Override
	public <T, R> R accept(FormulaVisitor<T, R> visitor, T parameters) {
		return visitor.visit(this, parameters);
	}

	/**
	 * @return the left
	 */
	public Formula getLeft() {
		return left;
	}

	/**
	 * @return the right
	 */
	public Formula getRight() {
		return right;
	}

	/**
	 * @return the interval
	 */
	public Function<Parameters, Interval> getInterval() {
		return interval;
	}

	
	public Interval getInterval( Parameters p ) {
		if (interval != null) {
			return interval.apply(p);
		}
		return null;
	}
	
	public boolean isUnbounded() {
		return (interval != null);
	}
	
}
