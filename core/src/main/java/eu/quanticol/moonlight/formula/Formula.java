/*
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
 */

package eu.quanticol.moonlight.formula;


/**
 * Interface implemented by any logic formula.
 * It is required to support the visit-based monitoring.
 *
 * @see FormulaVisitor implementations to understand how the formula is visited.
 */
public interface Formula {

    /**
     *
     * @param visitor the visiting monitoring program
     * @param parameters optional parameters of the monitoring process.
     * @param <T> Signal Trace Type
     * @param <R> Semantic Interpretation Semiring Type
     * @return a value corresponding to the value of the formula on R.
     */
    <T, R> R accept(FormulaVisitor<T, R> visitor, T parameters);

 }
