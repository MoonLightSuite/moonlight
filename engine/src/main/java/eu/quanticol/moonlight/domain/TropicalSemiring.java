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

package eu.quanticol.moonlight.domain;

/**
 * TODO: this class seems not to be used
 * @author loreti
 */
public class TropicalSemiring implements Semiring<Double> {

	@Override
	public Double conjunction(Double x, Double y) {
		return x + y;
	}

	@Override
	public Double disjunction(Double x, Double y) {
		return Math.min(x, y);
	}

	@Override
	public Double min() {
		return 0.0;
	}

	@Override
	public Double max() {
		return Double.POSITIVE_INFINITY;
	}

}
