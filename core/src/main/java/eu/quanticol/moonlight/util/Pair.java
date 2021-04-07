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

package eu.quanticol.moonlight.util;

import java.util.Objects;

/**
 * Immutable generic class to support pairs of objects
 * @param <F> Type of the first object
 * @param <S> Type of the second object
 *
 * @author loreti
 */
public class Pair<F, S> {
	private final F first;
	private final S second;

	/**
	 * @param first object
	 * @param second object
	 */
	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * @return the first
	 */
	public F getFirst() {
		return first;
	}

	/**
	 * @return the second
	 */
	public S getSecond() {
		return second;
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair<?,?> other = (Pair<?,?>) obj;
		return Objects.equals(first, other.first)
				&& Objects.equals(second, other.second);
	}

	@Override
	public String toString() {
		return "<" + first + " , " + second + ">";
	}
	
	

}
