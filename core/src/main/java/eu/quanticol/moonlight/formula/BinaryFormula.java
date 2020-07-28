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
 * Interface implemented by any logic formula over a binary logical operator.
 * It is required to support the visit-based online monitoring.
 *
 * @see Formula for more on the interface hierarchy
 */
public interface BinaryFormula extends Formula {

    /**
     * @return the first argument of the binary logical operator
     */
    Formula getFirstArgument();

    /**
     * @return the second argument of the binary logical operator
     */
    Formula getSecondArgument();
}
