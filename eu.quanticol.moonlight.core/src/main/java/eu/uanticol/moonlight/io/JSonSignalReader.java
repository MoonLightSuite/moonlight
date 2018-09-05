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
package eu.uanticol.moonlight.io;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import eu.quanticol.moonlight.signal.AssignmentFactory;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.VariableArraySignal;

/**
 *
 */
public class JSonSignalReader {
	
	
	private static JSonSignalReader instance;
	private GsonBuilder gson;
	
	
	private JSonSignalReader() {
		this.gson = new GsonBuilder();
		this.gson.registerTypeAdapter(VariableArraySignal.class, new VariableSignalDeserializer());
	}
	
	public static JSonSignalReader getInstance() {
		if (instance == null) {
			instance = new JSonSignalReader();
		}
		return instance;
	}
	
	
	public static VariableArraySignal readSignal( String signal ) {
		return getInstance().gson.create().fromJson(signal, VariableArraySignal.class);
	}
	
	
	private class VariableSignalDeserializer implements JsonDeserializer<VariableArraySignal> {

		@Override
		public VariableArraySignal deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2)
				throws JsonParseException {
			
			JsonObject jso = arg0.getAsJsonObject();
			
			JsonArray timeArray = jso.get("t").getAsJsonArray();
			Double[] times = toArray(timeArray,Double.class,new Double[timeArray.size()],arg2);
			JsonArray signals = jso.get("signals").getAsJsonArray();
			String[] variables = getVariables(signals);
			Class<?>[] varTypes = getSignalsType(signals);
			VariableArraySignal result = new VariableArraySignal(variables, new AssignmentFactory(varTypes));
			Object[][] values = getValues(times,variables, varTypes,signals,arg2);
			for( int i=0 ;i<times.length; i++) {
				result.add(times[i], values[i]);
			}
			return result;
		}


		private Class<?>[] getSignalsType(JsonArray jsa) {
			Class<?>[] types = new Class<?>[jsa.size()];
			for( int i=0 ; i<types.length ; i++) {
				types[i] = getVariableType(jsa.get(i).getAsJsonObject().get("type").getAsString());
			}
			return types;		
		}


		
	}
	
	public <T> T[] toArray(JsonArray jArray, Class<T> varType, T[] target, JsonDeserializationContext arg2) {
		for( int i=0 ; i<target.length ; i++ ) {
			target[i] = arg2.deserialize(jArray.get(i), varType);
		}
		return target;
	}

	public String[] getVariables(JsonArray jsa) {
		String[] variables = new String[jsa.size()];
		for( int i=0 ; i<variables.length ; i++) {
			variables[i] = jsa.get(i).getAsJsonObject().get("name").getAsString();
		}
		return variables;		
	}
	
	public Class<?> getVariableType(String str) {
		if ("boolean".equals(str)) {
			return Boolean.class;
		}
		if ("real".equals(str)) {
			return Double.class;
		}
		if ("integer".equals(str)) {
			return Integer.class;
		}
		throw new IllegalArgumentException("Unknown signal type "+str);
	}

	private Object[][] getValues(Double[] times, String[] variables, Class<?>[] varTypes, JsonArray signals, JsonDeserializationContext arg2) {
		Object[][] toReturn = new Object[times.length][variables.length];
		JsonArray[] values = new JsonArray[variables.length];
		for (int i=0 ; i<variables.length ; i++ ) {
			values[i] = signals.get(i).getAsJsonObject().get("values").getAsJsonArray();
		}
		for (int i=0 ; i<times.length; i++) {
			for (int j=0; j<variables.length; j++ ) {
				toReturn[i][j] = arg2.deserialize(values[j].get(i), varTypes[j]);
			}
		}
		return toReturn;
	}

}
