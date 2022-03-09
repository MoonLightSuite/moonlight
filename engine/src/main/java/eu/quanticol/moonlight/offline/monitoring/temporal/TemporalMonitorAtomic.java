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

package eu.quanticol.moonlight.offline.monitoring.temporal;

import java.util.function.Function;

import eu.quanticol.moonlight.offline.signal.Signal;

/**
 * Strategy to interpret an atomic predicate on the signal of interest.
 *
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see TemporalMonitor
 */
public class TemporalMonitorAtomic<T, R> implements TemporalMonitor<T, R> {

	private final Function<T, R> atomic;

	public TemporalMonitorAtomic(Function<T, R> atomic) {
		this.atomic = atomic;
	}

	@Override
	public Signal<R> monitor(Signal<T> signal) {
		return signal.apply(atomic);
	}

}
