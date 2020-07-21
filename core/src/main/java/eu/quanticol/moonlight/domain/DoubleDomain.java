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

package eu.quanticol.moonlight.domain;

import eu.quanticol.moonlight.signal.DataHandler;

/**
 * Signal domain to support doubles (i.e. classical robustness).
 *
 * @see SignalDomain
 */
public class DoubleDomain implements SignalDomain<Double> {
	private static final double TOLERANCE = 1E-12;

	@Override
	public Double neutral() {
		return 0.0;
	}

	@Override
	public Double conjunction(Double x, Double y) {
		return Math.min(x, y);
	}

	@Override
	public Double disjunction(Double x, Double y) {
		return Math.max(x, y);
	}

	@Override
	public Double negation(Double x) {
		return -x;
	}

	@Override
	public boolean equalTo(Double x, Double y) {
		return Math.abs(x - y) < TOLERANCE;
	}

	@Override
	public Double min() {
		return Double.NEGATIVE_INFINITY;
	}

	@Override
	public Double max() {
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public Double valueOf(boolean b) {
		return (b?Double.POSITIVE_INFINITY:Double.NEGATIVE_INFINITY);
	}

	@Override
	public Double valueOf(double v) {
		return v;
	}

	@Override
	public Double computeLessThan(double v1, double v2) {
		return v2-v1;
	}

	@Override
	public Double computeLessOrEqualThan(double v1, double v2) {
		return v2-v1;
	}

	@Override
	public Double computeEqualTo(double v1, double v2) {
		return -Math.abs(v1-v2);
	}

	@Override
	public Double computeGreaterThan(double v1, double v2) {
		return v1-v2;
	}

	@Override
	public Double computeGreaterOrEqualThan(double v1, double v2) {
		return v1-v2;
	}

	@Override
	public DataHandler<Double> getDataHandler() {
		return DataHandler.REAL;
	}

}
