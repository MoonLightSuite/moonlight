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

import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.io.DataHandler;

/**
 * Signal domain to support booleans (i.e. classical satisfaction).
 *
 * @see SignalDomain
 */
public class BooleanDomain implements SignalDomain<Boolean> {

	@Override
	public Boolean any() {
		throw new UnsupportedOperationException("Booleans don't have a " +
											    "third value");
	}

    @Override
    public Boolean conjunction(Boolean x, Boolean y) {
        return x && y;
    }

    @Override
    public Boolean disjunction(Boolean x, Boolean y) {
        return x || y;
    }

    @Override
    public Boolean negation(Boolean x) {
        return !x;
    }

    @Override
    public boolean equalTo(Boolean x, Boolean y) {
        return Boolean.compare(x, y) == 0;
    }

    @Override
    public Boolean min() {
        return false;
    }

    @Override
    public Boolean max() {
        return true;
    }

	@Override
	public Boolean valueOf(boolean b) {
		return b;
	}

	@Override
	public Boolean valueOf(double v) {
		return (v >= 0);
	}

	@Override
	public Boolean computeLessThan(double v1, double v2) {
		return (v1<v2);
	}

	@Override
	public Boolean computeLessOrEqualThan(double v1, double v2) {
		return (v1<=v2);
	}

	@Override
	public Boolean computeEqualTo(double v1, double v2) {
		return (v1==v2);
	}

	@Override
	public Boolean computeGreaterThan(double v1, double v2) {
		return (v1>v2);
	}

	@Override
	public Boolean computeGreaterOrEqualThan(double v1, double v2) {
		return (v1>=v2);
	}

	@Override
	public DataHandler<Boolean> getDataHandler() {
		return DataHandler.BOOLEAN;
	}

}
