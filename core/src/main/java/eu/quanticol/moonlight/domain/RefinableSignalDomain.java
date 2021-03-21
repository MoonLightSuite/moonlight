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

package eu.quanticol.moonlight.domain;

import eu.quanticol.moonlight.signal.DataHandler;

/**
 * This extension of Semiring introduces some elements that are key for
 * signal interpretation.
 * More precisely, (S, (-)) is a Signal domain when:
 * <ul>
 * <li> S is an idempotent Semiring </li>
 * <li> (-) is a negation function</li>
 * </ul>
 * Moreover, we include:
 * - Syntactic sugar for the implication connective
 * - An accompanying DataHandler for data parsing
 *
 * @param <R> Set over which the Semiring (and the SignalDomain) is defined
 *
 * @see Semiring
 * @see DataHandler
 */
public interface RefinableSignalDomain<R extends Comparable<? super R>> extends SignalDomain<R>
{
}
