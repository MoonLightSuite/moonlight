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
package eu.quanticol.moonlight.formula;

public interface SignalDomain<R> extends Semiring<R> {
	
	R negation(R x);

	boolean equalTo(R x, R y);
	
	public default R  implies(R x, R y) {
		return disjunction(negation(x), y);
	}
	
	R valueOf(boolean b);
	
	R valueOf(double v);
	
	default R valueOf(int v) {
		return valueOf((double) v);
	}
	
	R computeLessThan(double v1, double v2);

	R computeLessOrEqualThan(double v1, double v2);

	R computeEqualTo(double v1, double v2);
	
	R computeGreaterThan(double v1, double v2);
	
	R computeGreaterOrEqualThan(double v1, double v2);

}
