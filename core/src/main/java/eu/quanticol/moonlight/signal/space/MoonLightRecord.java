/*******************************************************************************
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
 *******************************************************************************/
package eu.quanticol.moonlight.signal.space;

import eu.quanticol.moonlight.signal.DataHandler;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 */
public class MoonLightRecord {
	
	private Object[] values;
	private Function<Integer, DataHandler<?>> handlers;

	public MoonLightRecord(Function<Integer,DataHandler<?>> handlers , Object[] values ) {
		this.values = values;
		this.handlers = handlers;
	}
	
	public <T> T get( int i , Class<T> varType) {
		return varType.cast(values[i]);
	}

	public Object get( int i ) {
		return values[i];
	}

	public Class<?> getTypeOf( int i ) {
		return handlers.apply(i).getTypeOf();
	}

	public <T> boolean hasType( int i , Class<T> varType ) {
		return varType.isAssignableFrom(getTypeOf(i));				
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MoonLightRecord)) return false;
		MoonLightRecord record = (MoonLightRecord) o;
		return Arrays.equals(values, record.values);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(values);
	}

	@Override
	public String toString() {
		return IntStream.range(0,values.length).mapToObj(i -> values[i].toString()).collect(Collectors.joining(";"));
	}

	public Object[] getValues() {
		return Arrays.copyOf(values,values.length);
	}
}
