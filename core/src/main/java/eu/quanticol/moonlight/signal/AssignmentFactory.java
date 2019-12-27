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
import java.util.Map;
import java.util.stream.IntStream;

import eu.quanticol.moonlight.util.Pair;

public class AssignmentFactory {
	
	
	private SignalDataHandler<?>[] varTypes;
	private Map<String,Integer> variableIndex;
		
	
	public AssignmentFactory( SignalDataHandler<?> ... varTypes ) {
		this( null , varTypes );
	}

	public AssignmentFactory( Map<String,Integer> variableIndex, SignalDataHandler<?> ... varTypes ) {
		this.varTypes = Arrays.copyOf(varTypes, varTypes.length);
		this.variableIndex = variableIndex;
	}
	
	@SafeVarargs
	public static AssignmentFactory createFactory( Pair<String, SignalDataHandler<?>> ... variables ) {
		SignalDataHandler<?>[] dataHandlers = new SignalDataHandler<?>[variables.length];
		Map<String,Integer> variableIndex = new HashMap<>();
		int counter = 0;
		for (Pair<String, SignalDataHandler<?>> p : variables) {
			dataHandlers[counter] = p.getSecond();
			if (variableIndex.put(p.getFirst(), counter++)!=null) {
				throw new IllegalArgumentException("Duplicated variable "+p.getFirst()+"!");
			}
		}
		return new AssignmentFactory(variableIndex,dataHandlers);
	}
	
	public Assignment fromObject( Object ... values ) {
		if (values.length != varTypes.length) {
			throw new IllegalArgumentException("Wrong data size! (Expected "+varTypes.length+" is "+values.length);
		}
		return build(IntStream.range(0, values.length).boxed().map(i -> varTypes[i].fromObject(values[i])).toArray());
	}

	private Assignment build(Object[] values) {
		return new Assignment(i -> varTypes[i], values);
	}

	public Assignment fromString( String ... strings ) {
		if (strings.length != varTypes.length) {
			throw new IllegalArgumentException("Wrong data size! (Expected "+varTypes.length+" is "+strings.length+")");
		}
		return build(IntStream.range(0, strings.length).boxed().map(i -> varTypes[i].fromString(strings[i])).toArray());
	}
	
	public Assignment fromObject( Map<String,Object> values ) {
		checkNumberOfVariables( values.size() );
		Object[] data = new Object[varTypes.length];
		values.forEach((v,o) -> {
			int variableIndex = getVariableIndex(v);
			if (variableIndex<0) {
				throwUnknownVariableException(v);
			}
			SignalDataHandler<?> handler = varTypes[variableIndex]; 
			if (!handler.checkType(o)) {
				throwVariableTypeException(v,handler.getTypeOf().getTypeName(),o.getClass().getTypeName());
			}
			data[variableIndex] = handler.fromObject(o);
		});
		return build(data);
	}

	private void throwUnknownVariableException(String v) {
		throw new IllegalArgumentException("Unknown variable "+v);
	}

	private void throwVariableTypeException(String v, String expected, String actual) {
		throw new IllegalArgumentException( "Wrong data type for variable"+v+"; expected "+expected+" is "+actual);
		
	}

	public int getVariableIndex(String v) {
		Integer index = variableIndex.getOrDefault(v,-1);
		return index;
	}
	
	public Assignment fromString( Map<String,String> values ) {
		checkNumberOfVariables( values.size() );
		Object[] data = new Object[varTypes.length];
		values.forEach((v,o) -> {
			int variableIndex = getVariableIndex(v);
			if (variableIndex<0) {
				throwUnknownVariableException(v);
			}
			SignalDataHandler<?> handler = varTypes[variableIndex]; 
			data[variableIndex] = handler.fromString(o);
		});
		return build(data);
	}

	public boolean checkNumberOfVariables(int size) {
		if (size != variableIndex.size()) {
			throw new IllegalArgumentException("Wrong number of variables! (Expected "+varTypes.length+" is "+size+")");
		}
		return true;
	}

	public boolean checkVariableType(String v, String type) {
		int variableIndex = getVariableIndex(v);
		if (variableIndex<0) {
			throwUnknownVariableException(v);
		}
		if (!varTypes[variableIndex].checkTypeCode(type)) {
			throwVariableTypeException(v, varTypes[variableIndex].getTypeCode(), type); 
		}
		return true;
	}

	public Map<String, Integer> getVariableIndex() {
		return variableIndex;
	}
	
	
}
