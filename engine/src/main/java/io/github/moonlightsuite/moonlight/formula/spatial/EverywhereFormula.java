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

package io.github.moonlightsuite.moonlight.formula.spatial;

import io.github.moonlightsuite.moonlight.core.formula.Formula;
import io.github.moonlightsuite.moonlight.core.formula.FormulaVisitor;
import io.github.moonlightsuite.moonlight.core.formula.SpatialFormula;
import io.github.moonlightsuite.moonlight.core.formula.UnaryFormula;

/**
 * Everywhere operator
 * @param distanceFunctionId identifier of the distance function to consider
 * @param argument sub-formula on which the operator is applied
 */
public record EverywhereFormula(String distanceFunctionId, Formula argument)
		implements UnaryFormula, SpatialFormula
{
	@Override
	public String getDistanceFunctionId() {
		return distanceFunctionId;
	}

	@Override
	public Formula getArgument() {
		return argument;
	}
}
