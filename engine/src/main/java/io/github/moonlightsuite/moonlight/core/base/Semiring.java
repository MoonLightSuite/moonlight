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

package io.github.moonlightsuite.moonlight.core.base;

/**
 * The {@code Semiring} class describes the behavior of algebraic semirings,
 * as used by our framework.
 *
 * <p> A semiring is the 5-tuple (R, (+), (*), 0, 1) where:
 * <ul>
 *     <li> R represents the definition set (type) of the semiring </li>
 *     <li> (+) represents an associative, commutative, idempotent operator,
 *     		to choose a value among many, also called choose/conjunction </li>
 *     <li> (*) represents an associative, commutative operator to combine
 *          values. Also called combine/disjunction </li>
 *     <li> 0 represents a constant identity element for (+)</li>
 *     <li> 1 represents a constant identity element for (*) </li>
 * </ul>
 *
 * <p>
 *    <ul>
 *     	<li> (*) distributes over (+) </li>
 *     	<li> an operator defining a complete lattice exists </li>
 *      <li> TODO: add properties that must hold for semirings </li>
 *    </ul>
 *
 * @param <R> Set over which the semiring is defined
 *
 * @author loreti
 */
public interface Semiring<R> {
	/**
	 * Associative, commutative, idempotent operator that chooses a value.
	 * @param x first available value
	 * @param y second available value
	 * @return a result satisfying conjunction properties
	 */
	R conjunction(R x, R y);

	/**
	 * Associative, commutative operator that combines values.
	 * @param x first value to combine
	 * @param y second value to combine
	 * @return a result satisfying disjunction properties
	 */
	R disjunction(R x, R y);

	/**
	 * @return the infimum (aka meet) of the lattice defined over the semiring.
	 */
	R min();

	/**
	 * @return the supremum (aka join) of the lattice defined over the semiring.
	 */
	R max();
}
