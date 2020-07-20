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

package eu.quanticol.moonlight.monitoring.temporal.online;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.monitoring.temporal.*;
import eu.quanticol.moonlight.signal.Signal;

import java.util.List;

/**
 * Primary Monitoring interface
 * It is based on a strategy design pattern, where each kind of operators
 * has a specific strategy implementation.
 *
 * Implementors must implement the {@link TemporalMonitor} methods, that are
 * general to all kinds of monitors, and the
 * {@link #setHorizon(AbstractInterval)} which is needed to understand whether
 * the formula value must be updated or not.
 *
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * For the more general monitor interface:
 * @see TemporalMonitor
 *
 * For specific implementation of the different logic operators:
 * @see OnlineMonitorAtomic
 * @see OnlineMonitorUnaryOperator
 * @see OnlineMonitorBinaryOperator
 * @see OnlineMonitorFutureOperator
 * @see OnlineMonitorUntil
 * @see OnlineMonitorPastOperator
 * @see OnlineMonitorSince
 */
public interface OnlineTemporalMonitor<T, R> extends TemporalMonitor<T, R> {

    /**
     * This method
     * @param parentHorizon
     */
    //void setHorizon(AbstractInterval<?> parentHorizon);

    List<R> getWorklist();
}
