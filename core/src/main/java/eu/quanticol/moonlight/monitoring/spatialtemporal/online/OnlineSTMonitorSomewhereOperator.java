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

package eu.quanticol.moonlight.monitoring.spatialtemporal.online;

import eu.quanticol.moonlight.algorithms.SpaceOperator;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.space.DistanceStructure;
import eu.quanticol.moonlight.space.LocationService;
import eu.quanticol.moonlight.space.SpatialModel;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Strategy to interpret the Somewhere spatial logic operator.
 *
 * @param <S> Spatial Graph Edge Type
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see SpatialTemporalMonitor
 */
public class OnlineSTMonitorSomewhereOperator<S, T, R>
        implements SpatialTemporalMonitor<S, T, R>
{
    private final SpatialTemporalMonitor<S, T, R> m;
    private final Function<SpatialModel<S>, DistanceStructure<S, ?>> distance;
    private final SignalDomain<R> domain;
    private final Interval horizon;
    private double signalEnd = 0;
    private final List<SpatialTemporalSignal<R>> worklist;

    public OnlineSTMonitorSomewhereOperator(SpatialTemporalMonitor<S, T, R> m,
                                            Function<SpatialModel<S>,
                                             DistanceStructure<S, ?>> distance,
                                            SignalDomain<R> domain,
                                            Interval parentHorizon)
    {
        this.m = m;
        this.distance = distance;
        this.domain = domain;
        this.horizon = parentHorizon;
        this.worklist = new ArrayList<>();
    }

    @Override
    public SpatialTemporalSignal<R> monitor(LocationService<Double, S> locationService,
                                            SpatialTemporalSignal<T> signal)
    {
        //if(horizon.contains(signalEnd) || worklist.isEmpty()) {
        //update result
        worklist.add(SpaceOperator.computeWhereDynamic(locationService,
                                                  distance,
                                                  this::somewhereOp,
                                                  m.monitor(locationService,
                                                            signal)));
        //System.out.println("[DEBUG] Binary Operator worklist:");
        //System.out.println(getWorklist().toString());
        //}

        signalEnd =  signal.end();
        //System.out.println("SpaceOperator Result Signal@maxT= " + signalEnd +
        //                " : " + worklist.get(worklist.size() - 1).toString());
        return worklist.get(worklist.size() - 1); //return last computed value
    }

    private List<R> somewhereOp(Function<Integer, R> spatialSignal,
                                DistanceStructure<S, ?> ds)
    {
        return DistanceStructure.somewhere(domain, spatialSignal, ds);
    }
}
