/*
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
 */

package eu.quanticol.moonlight.formula.temporal;

import eu.quanticol.moonlight.core.formula.Formula;
import eu.quanticol.moonlight.core.formula.TemporalFormula;
import eu.quanticol.moonlight.core.formula.UnaryFormula;
import eu.quanticol.moonlight.core.formula.Interval;

/**
 * Historically operator
 * @param argument sub-formula on which the operator is applied
 * @param interval (optional) time interval to which the operator is restricted.
 *                 A missing interval means the operator
 *                 is <code>unbounded</code>.
 */
public record HistoricallyFormula(Formula argument, Interval interval)
        implements UnaryFormula, TemporalFormula
{
    public HistoricallyFormula(Formula argument) {
        this(argument, null);
    }

    @Override
    public Formula getArgument() {
        return argument;
    }

    @Override
    public Interval getInterval() {
        return interval;
    }

    @Override
    public boolean isUnbounded() {
        return interval == null;
    }
}
