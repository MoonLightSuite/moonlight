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

/**
 *
 */
public class FormulaFactory<R> {
	
	private final DomainModule<R> module;
	
	public FormulaFactory( DomainModule<R> module ) {
		this.module = module;
	}
	
	public <T> Formula<T,R> conjunction( Formula<T,R> left , Formula<T,R> right ) {
		return new BinaryFormula<T,R>(left, module::conjunction, right);
	}

	public <T> Formula<T,R> disjunction( Formula<T,R> left , Formula<T,R> right ) {
		return new BinaryFormula<T,R>(left, module::disjunction, right);
	}

	public <T> Formula<T,R> negation( Formula<T,R> arg) {
		return new UnaryFormula<>(module::negation, arg);
	}
	
	public <T> Formula<T,R> eventually( double a, double b, Formula<T,R> f ) {
		return new TemporalFormula<>(a, b, module::disjunction, f);
	}
	
	public <T> Formula<T,R> globally( double a, double b, Formula<T,R> f ) {
		return new TemporalFormula<>(a, b, module::conjunction, f);
	}
	

}
