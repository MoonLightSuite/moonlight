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

package io.github.moonlightsuite.moonlight.offline.monitoring.spatialtemporal;

import io.github.moonlightsuite.moonlight.core.algorithms.EscapeAlgorithm;
import io.github.moonlightsuite.moonlight.core.signal.SignalDomain;
import io.github.moonlightsuite.moonlight.core.space.DistanceStructure;
import io.github.moonlightsuite.moonlight.core.space.LocationService;
import io.github.moonlightsuite.moonlight.core.space.SpatialModel;
import io.github.moonlightsuite.moonlight.offline.algorithms.SpatialOp;
import io.github.moonlightsuite.moonlight.offline.signal.SpatialTemporalSignal;

import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Strategy to interpret the Escape spatial logic operator.
 *
 * @param <S> Spatial Graph Edge Type
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 * @see SpatialTemporalMonitor
 */
public class SpatialTemporalMonitorEscape<S, T, R>
        implements SpatialTemporalMonitor<S, T, R> {
    private final SpatialTemporalMonitor<S, T, R> m;
    private final Function<SpatialModel<S>, DistanceStructure<S, ?>> distance;
    private final SignalDomain<R> domain;

    public SpatialTemporalMonitorEscape(SpatialTemporalMonitor<S, T, R> m,
                                        Function<SpatialModel<S>,
                                                DistanceStructure<S, ?>> distance,
                                        SignalDomain<R> domain) {
        this.m = m;
        this.distance = distance;
        this.domain = domain;
    }

    @Override
    public SpatialTemporalSignal<R> monitor(LocationService<Double, S> locationService,
                                            SpatialTemporalSignal<T> signal) {
        SpatialOp<S, R> sc = new SpatialOp<>(locationService,
                distance,
                this::escapeOp);
        return sc.computeUnary(m.monitor(locationService, signal));
    }

    private IntFunction<R> escapeOp(IntFunction<R> spatialSignal,
                                    DistanceStructure<S, ?> ds) {
        return new EscapeAlgorithm<>(ds, domain, spatialSignal).compute();
    }
}
