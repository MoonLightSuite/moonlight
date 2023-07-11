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

package io.github.moonlightsuite.moonlight.core.signal;

import io.github.moonlightsuite.moonlight.core.base.Semiring;
import io.github.moonlightsuite.moonlight.core.io.DataHandler;
import io.github.moonlightsuite.moonlight.core.io.SerializableData;

/**
 * This extension of Semiring introduces some elements that are key for
 * signal interpretation.
 * More precisely, (S, (-)) is a Signal domain when:
 * <ul>
 * 	<li> S is an idempotent Semiring </li>
 * 	<li> (-) is a negation function</li>
 * </ul>
 * Moreover, we include:
 * - Syntactic sugar for the implication connective
 * - An accompanying DataHandler for data parsing
 *
 * @param <R> Set over which the Semiring (and the SignalDomain) is defined
 * @see Semiring
 * @see DataHandler
 */
public interface SignalDomain<R> extends Semiring<R>, SerializableData<R> {

    /**
     * Unknown element: this is an element of the set that represents
     * undefined areas of the signal.
     * Examples of this could be 0 for real numbers,
     * a third value for booleans, or the total interval for intervals.
     *
     * @return the element of the set representing absence of knowledge
     */
    R any();

    /**
     * Negation function that s.t. De Morgan laws, double negation
     * and inversion of the idempotent elements hold.
     *
     * @param x element to negate
     * @return the negation of the x element
     */
    R negation(R x);

	/* TODO: Some doubts about the following methods:
	     - equalTo(x, y) seems useless (couldn't just use x.equals(y)?)
	     - compare(x, y) ? come Comparable
	 */

    /**
     * Shorthand for returning an operational implication
     *
     * @param x the premise of the implication
     * @param y the conclusion of the implication
     * @return the element that results from the implication
     */
    default R implies(R x, R y) {
        return disjunction(negation(x), y);
    }

    boolean equalTo(R x, R y);


}


