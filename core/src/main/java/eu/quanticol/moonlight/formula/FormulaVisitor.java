/*
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
 */

package eu.quanticol.moonlight.formula;

/**
 * Visitor interface to interpret a formula and, recursively, its subformulas.
 *
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see Formula implementations to see how the visitor is accepted
 *
 * @author loreti
 */
public interface FormulaVisitor<T, R> {
	
	static IllegalArgumentException generateException( Class<?> c ) {
		return new IllegalArgumentException(c.getName() + " is not supported by this visitor!");
	}

	default R visit(AtomicFormula atomicFormula, T parameters) {
		throw generateException(atomicFormula.getClass());
	}

	default R visit(AndFormula andFormula, T parameters) {
		throw generateException(andFormula.getClass());
	}

	default R visit(NegationFormula negationFormula, T parameters) {
		throw generateException(negationFormula.getClass());
	}

	default R visit(OrFormula orFormula, T parameters) {
		throw generateException(orFormula.getClass());
	}

	default R visit(EventuallyFormula eventuallyFormula, T parameters) {
		throw generateException(eventuallyFormula.getClass());
	}

	default R visit(GloballyFormula globallyFormula, T parameters) {
		throw generateException(globallyFormula.getClass());
	}

	default R visit(UntilFormula untilFormula, T parameters) {
		throw generateException(untilFormula.getClass());
	}

	default R visit(SinceFormula sinceFormula, T parameters) {
		throw generateException(sinceFormula.getClass());
	}

	default R visit(HistoricallyFormula historicallyFormula, T parameters) {
		throw generateException(historicallyFormula.getClass());
	}

	default R visit(OnceFormula onceFormula, T parameters) {
		throw generateException(onceFormula.getClass());
	}
	
	default R visit(SomewhereFormula somewhereFormula, T parameters ) {
		throw generateException(somewhereFormula.getClass());
	}

	default R visit(EverywhereFormula everywhereFormula, T parameters) {
		throw generateException(everywhereFormula.getClass());		
	}
	
	default R visit(ReachFormula reachFormula, T parameters) {
		throw generateException(reachFormula.getClass());		
	}

	default R visit(EscapeFormula escapeFormula, T parameters) {
		throw generateException(escapeFormula.getClass());		
	}

}
