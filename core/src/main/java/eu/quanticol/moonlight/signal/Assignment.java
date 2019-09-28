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
package eu.quanticol.moonlight.signal;

import java.util.Arrays;
import java.util.HashMap;

/**
 *
 */
public class Assignment {
	
	private Object[] values;
	private Class<?>[] varTypes;
	
	public Assignment( Class<?>[] varTypes , Object[] values ) {
		if (varTypes.length != values.length) {
			throw new IllegalArgumentException();
		}
		
		this.values = values;
		this.varTypes = varTypes;
	}
	
	public <T> T get( int i , Class<T> varType) {
		if (varType.isAssignableFrom(varTypes[i])) {
			return varType.cast(values[i]);
		}
		throw new ClassCastException();
	}
	
	public Class<?> getTypeOf( int i ) {
		return this.varTypes[i];
	}

	
}
