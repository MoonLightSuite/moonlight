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

import eu.quanticol.moonlight.domain.Interval;

/**
 *
 */
public class GloballyFormula implements Formula {

    private final Formula argument;
    private Interval interval;

    public GloballyFormula(Formula argument) {
        this(argument, null);
    }

    public GloballyFormula(Formula argument, Interval interval) {
        this.argument = argument;
        this.interval = interval;
    }

    @Override
    public <T, R> R accept(FormulaVisitor<T, R> visitor, T parameters) {
        return visitor.visit(this, parameters);
    }

    /**
     * @return the argument
     */
    public Formula getArgument() {
        return argument;
    }

    /**
     * @return the interval
     */
    public Interval getInterval() {
        return interval;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((argument == null) ? 0 : argument.hashCode());
        result = prime * result + ((interval == null) ? 0 : interval.hashCode());
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
        GloballyFormula other = (GloballyFormula) obj;
        if (argument == null) {
            if (other.argument != null)
                return false;
        } else if (!argument.equals(other.argument))
            return false;
        if (interval == null) {
            if (other.interval != null)
                return false;
        } else if (!interval.equals(other.interval))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "GloballyFormula [argument=" + argument + ", interval=" + interval + "]";
    }

    public boolean isUnbounded() {
        return interval == null;
    }


//	public Interval getInterval( Parameters p ) {
//		return this.interval.apply(p);
//	}

}
