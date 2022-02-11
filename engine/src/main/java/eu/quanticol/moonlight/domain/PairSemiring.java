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

package eu.quanticol.moonlight.domain;

import eu.quanticol.moonlight.util.Pair;

/**
 * @deprecated generalized by {@link ListDomain}
 * TODO: this class seems not to be used
 * @author loreti
 */
@Deprecated
public class PairSemiring<T,R> implements Semiring<Pair<T,R>> {
	
	private final Semiring<T> firstSemiring;
	private final Semiring<R> secondSemiring;
	private final Pair<T,R> min;
	private final Pair<T,R> max;
	
	public PairSemiring( Semiring<T> firstSemiring, Semiring<R> secondSemiring) {
		this.firstSemiring = firstSemiring;
		this.secondSemiring = secondSemiring;
		this.min = new Pair<>(firstSemiring.min(),secondSemiring.min());
		this.max = new Pair<>(firstSemiring.max(),secondSemiring.max());
	}

	@Override
	public Pair<T, R> conjunction(Pair<T, R> x, Pair<T, R> y) {
		return new Pair<>( 
			firstSemiring.conjunction(x.getFirst(), y.getFirst()),
			secondSemiring.conjunction(x.getSecond(), y.getSecond())	
		);
	}

	@Override
	public Pair<T, R> disjunction(Pair<T, R> x, Pair<T, R> y) {
		return new Pair<>( 
				firstSemiring.disjunction(x.getFirst(), y.getFirst()),
				secondSemiring.disjunction(x.getSecond(), y.getSecond())	
			);
	}

	@Override
	public Pair<T, R> min() {
		return this.min;
	}

	@Override
	public Pair<T, R> max() {
		return this.max;
	}

}
