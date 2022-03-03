/*******************************************************************************
 * MoonLight: a light-weight framework for runtime monitoring
 * Copyright (C) 2018-2021
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

import eu.quanticol.moonlight.core.formula.Formula;
import eu.quanticol.moonlight.core.formula.FormulaVisitor;

/**
 *
 */
public class AtomicFormula implements Formula {

    private final String atomicId;

    public AtomicFormula(String atomicId) {
        this.atomicId = atomicId;
    }

    public String getAtomicId() {
        return atomicId;
    }

    @Override
    public <T, R> R accept(FormulaVisitor<T, R> visitor, T parameters) {
        return visitor.visit(this, parameters);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((atomicId == null) ? 0 : atomicId.hashCode());
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
        AtomicFormula other = (AtomicFormula) obj;
        if (atomicId == null) {
            if (other.atomicId != null)
                return false;
        } else if (!atomicId.equals(other.atomicId))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AtomicFormula [atomicId=" + atomicId + "]";
    }

}
