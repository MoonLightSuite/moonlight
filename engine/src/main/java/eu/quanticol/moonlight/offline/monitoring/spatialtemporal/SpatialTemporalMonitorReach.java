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

package eu.quanticol.moonlight.offline.monitoring.spatialtemporal;

import eu.quanticol.moonlight.core.algorithms.SpatialAlgorithms;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.offline.algorithms.SpatialOp;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;

import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Strategy to interpret the Reach spatial logic operator.
 *
 * @param <S> Spatial Graph Edge Type
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 * @see SpatialTemporalMonitor
 */
public class SpatialTemporalMonitorReach<S, T, R>
        implements SpatialTemporalMonitor<S, T, R> {
    private final SpatialTemporalMonitor<S, T, R> m1;
    private final Function<SpatialModel<S>, DistanceStructure<S, ?>> distance;
    private final SpatialTemporalMonitor<S, T, R> m2;
    private final SignalDomain<R> domain;

    public SpatialTemporalMonitorReach(SpatialTemporalMonitor<S, T, R> m1,
                                       Function<SpatialModel<S>,
                                               DistanceStructure<S, ?>> distance,
                                       SpatialTemporalMonitor<S, T, R> m2,
                                       SignalDomain<R> domain) {
        this.m1 = m1;
        this.distance = distance;
        this.m2 = m2;
        this.domain = domain;
    }

    @Override
    public SpatialTemporalSignal<R> monitor(LocationService<Double, S> locationService,
                                            SpatialTemporalSignal<T> signal) {
        SpatialOp<S, R> sp = new SpatialOp<>(locationService, distance, null);
        return sp.computeDynamic(domain,
                m1.monitor(locationService, signal),
                m2.monitor(locationService, signal));
    }

    private IntFunction<R> reachOp(IntFunction<R> leftSignal,
                                   IntFunction<R> rightSignal,
                                   DistanceStructure<S, ?> ds) {
        return SpatialAlgorithms.reach(domain, leftSignal, rightSignal, ds);
    }
}
