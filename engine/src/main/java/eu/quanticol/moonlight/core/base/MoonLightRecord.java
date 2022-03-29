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

package eu.quanticol.moonlight.core.base;

import eu.quanticol.moonlight.core.io.DataHandler;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MoonlightRecords are essentially tuple types.
 * <p>
 * They are implemented similarly to
 * <a href="https://docs.oracle.com/javaee/7/api/javax/persistence/Tuple.html">
 * Java EE Tuples
 * </a>.
 */
public record MoonLightRecord(Function<Integer, DataHandler<?>> handlers,
							  Object[] values)
{
	/**
	 * Returns the (typed) i-th element of the tuple.
	 *
	 * @param i index in the tuple of the element of interest
	 * @param varType class of the element of interest
	 * @param <T> the return type resulting by casting the element to the class
	 * @return the (typed) i-th element of the tuple.
	 */
	public <T> T get(int i, Class<T> varType) {
		return varType.cast(values[i]);
	}

	/**
	 * Returns the i-th element of a tuple as an Object.
	 *
	 * @param i index in the tuple of the element of interest
	 * @return the Object for the i-th element of the tuple
	 */
	public Object get(int i) {
		return values[i];
	}

	/**
	 * Returns the class of the i-th element of a tuple.
	 *
	 * @param i index in the tuple of the element of interest
	 * @return the class of the i-th element of a tuple
	 */
	public Class<?> getTypeOf(int i) {
		return handlers.apply(i).getTypeOf();
	}

	/**
	 * Checks whether the i-th element of the tuple has the passed type.
	 *
	 * @param i       index in the tuple of the element of interest
	 * @param varType class to check
	 * @param <T>     the type of the class
	 * @return <code>true</code> if it can be cast. <code>false</code> otherwise
	 */
	public <T> boolean hasType(int i, Class<T> varType) {
		return varType.isAssignableFrom(getTypeOf(i));
	}

	/**
	 * @return a copy of the values stored in the tuple, as Objects.
	 */
	public Object[] getValues() {
		return Arrays.copyOf(values, values.length);
	}

	/**
	 * Converts the i-th element to a <code>Double</code>.
	 *
	 * @param i index in the tuple of the element of interest
	 * @return the <code>Double</code> value of the i-th element
	 */
	public double getDoubleOf(int i) {
		return handlers.apply(i).doubleOf(values[i]);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MoonLightRecord r)) return false;
		return Arrays.equals(values, r.values);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(values);
	}

	@Override
	public String toString() {
		return Arrays.stream(values)
				.map(Object::toString)
				.collect(Collectors.joining(";"));
	}
}
