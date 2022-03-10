/*******************************************************************************
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
 *******************************************************************************/
package eu.quanticol.moonlight.offline.signal;

import eu.quanticol.moonlight.core.base.MoonLightRecord;

import java.util.Map;

/**
 * @author loreti
 *
 */
public class VariableArraySignal extends Signal<MoonLightRecord>{
	
	private RecordHandler factory;
	
	public VariableArraySignal(RecordHandler factory) {
		this.factory = factory;
	}
	
	public void addFromMap( double t , Map<String,Object> values ) {
		super.add(t,factory.fromObjectArray(values));
	}

	public void addFromString( double t , Map<String,String> values ) {
		super.add(t,factory.fromStringArray(values));
	}	
	
	public void addFromObject( double t , Object ... values ) {
		super.add(t, factory.fromObjectArray(values));
	}

	public int getVariableIndex(String name) {
		return factory.getVariableIndex(name);
	}
	

}
