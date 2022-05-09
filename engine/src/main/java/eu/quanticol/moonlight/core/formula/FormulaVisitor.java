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

package eu.quanticol.moonlight.core.formula;

import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.formula.classic.AndFormula;
import eu.quanticol.moonlight.formula.classic.NegationFormula;
import eu.quanticol.moonlight.formula.classic.OrFormula;
import eu.quanticol.moonlight.formula.spatial.EscapeFormula;
import eu.quanticol.moonlight.formula.spatial.EverywhereFormula;
import eu.quanticol.moonlight.formula.spatial.ReachFormula;
import eu.quanticol.moonlight.formula.spatial.SomewhereFormula;
import eu.quanticol.moonlight.formula.temporal.*;

/**
 * Visitor interface to interpret a formula and, recursively, its subformulae.
 *
 * @param <P> Monitoring parameters Type
 * @param <M> Monitoring type, based on some semantic interpretation type
 *
 * @see Formula implementations to see how the visitor is accepted
 *
 * @deprecated
 * @author loreti
 */
@Deprecated(since = "adoption of java17 pattern matching makes this useless")
public interface FormulaVisitor<P, M> {
	
	static IllegalArgumentException generateException( Class<?> c ) {
		return new IllegalArgumentException(c.getName() +
										  " is not supported by this visitor!");
	}

	/* CLASSICAL LOGIC OPERATORS */
	default M visit(AtomicFormula atomicFormula, P parameters) {
		throw generateException(atomicFormula.getClass());
	}

	default M visit(AndFormula andFormula, P parameters) {
		throw generateException(andFormula.getClass());
	}

	default M visit(NegationFormula negationFormula, P parameters) {
		throw generateException(negationFormula.getClass());
	}

	default M visit(OrFormula orFormula, P parameters) {
		throw generateException(orFormula.getClass());
	}

	/* TEMPORAL LOGIC OPERATORS */

	default M visit(EventuallyFormula eventuallyFormula, P parameters) {
		throw generateException(eventuallyFormula.getClass());
	}

	default M visit(GloballyFormula globallyFormula, P parameters) {
		throw generateException(globallyFormula.getClass());
	}

	default M visit(UntilFormula untilFormula, P parameters) {
		throw generateException(untilFormula.getClass());
	}

	default M visit(SinceFormula sinceFormula, P parameters) {
		throw generateException(sinceFormula.getClass());
	}

	default M visit(HistoricallyFormula historicallyFormula, P parameters) {
		throw generateException(historicallyFormula.getClass());
	}

	default M visit(OnceFormula onceFormula, P parameters) {
		throw generateException(onceFormula.getClass());
	}

	/* SPATIAL LOGIC OPERATORS */
	
	default M visit(SomewhereFormula somewhereFormula, P parameters ) {
		throw generateException(somewhereFormula.getClass());
	}

	default M visit(EverywhereFormula everywhereFormula, P parameters) {
		throw generateException(everywhereFormula.getClass());		
	}
	
	default M visit(ReachFormula reachFormula, P parameters) {
		throw generateException(reachFormula.getClass());		
	}

	default M visit(EscapeFormula escapeFormula, P parameters) {
		throw generateException(escapeFormula.getClass());		
	}

}
