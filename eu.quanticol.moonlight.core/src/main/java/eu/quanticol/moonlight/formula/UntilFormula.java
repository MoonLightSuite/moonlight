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
	
	private final Formula firstArgument;
	
	private final Formula secondArgument;
	
	private final Interval interval;
	
	public UntilFormula( Formula firstArgument , Formula secondArgument ) {
		this(firstArgument,secondArgument,null);
	}

	public UntilFormula(Formula firstArgument, Formula secondArgument, Interval interval) {
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
		return (interval == null);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstArgument == null) ? 0 : firstArgument.hashCode());
		result = prime * result + ((interval == null) ? 0 : interval.hashCode());
		result = prime * result + ((secondArgument == null) ? 0 : secondArgument.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UntilFormula other = (UntilFormula) obj;
		if (firstArgument == null) {
			if (other.firstArgument != null)
				return false;
		} else if (!firstArgument.equals(other.firstArgument))
			return false;
		if (interval == null) {
			if (other.interval != null)
				return false;
		} else if (!interval.equals(other.interval))
			return false;
		if (secondArgument == null) {
			if (other.secondArgument != null)
				return false;
		} else if (!secondArgument.equals(other.secondArgument))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UntilFormula [firstArgument=" + firstArgument + ", secondArgument=" + secondArgument + ", interval="
				+ interval + "]";
	}
	
}
