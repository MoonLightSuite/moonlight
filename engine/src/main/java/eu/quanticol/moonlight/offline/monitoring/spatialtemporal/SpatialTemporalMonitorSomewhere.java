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

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

import eu.quanticol.moonlight.offline.algorithms.SpatialComputation;
import eu.quanticol.moonlight.offline.algorithms.SpatialOperators;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.offline.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.util.Pair;

/**
 * Strategy to interpret the Somewhere spatial logic operator.
 *
 * @param <S> Spatial Graph Edge Type
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see SpatialTemporalMonitor
 */
public class SpatialTemporalMonitorSomewhere<S, T, R> implements SpatialTemporalMonitor<S, T, R> {

	private SpatialTemporalMonitor<S, T, R> m;
	private Function<SpatialModel<S>, DistanceStructure<S, ?>> distance;
	private SignalDomain<R> domain;

	public SpatialTemporalMonitorSomewhere(SpatialTemporalMonitor<S, T, R> m,
                                           Function<SpatialModel<S>, DistanceStructure<S, ?>> distance, SignalDomain<R> domain)
    {
		this.m = m;
		this.distance = distance;
		this.domain = domain;
	}

	@Override
	public SpatialTemporalSignal<R> monitor(LocationService<Double, S> locationService, SpatialTemporalSignal<T> signal) {
		//return computeSomewhereDynamic(locationService,m.monitor(locationService, signal));
        return SpatialOperators.computeWhereDynamic(locationService,
                distance,
                this::somewhereOp,
                m.monitor(locationService, signal));
	}

    private List<R> somewhereOp(IntFunction<R> spatialSignal,
                                 DistanceStructure<S, ?> ds)
    {
        return SpatialComputation.somewhere(domain, spatialSignal, ds);
    }

    private SpatialTemporalSignal<R> computeSomewhereDynamic(
            LocationService<Double, S> l, SpatialTemporalSignal<R> s) {
        SpatialTemporalSignal<R> toReturn = new SpatialTemporalSignal<R>(s.getNumberOfLocations());
        if (l.isEmpty()) {
            return toReturn;
        }
        ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
        Iterator<Pair<Double, SpatialModel<S>>> locationServiceIterator = l.times();
        Pair<Double, SpatialModel<S>> current = locationServiceIterator.next();
        Pair<Double, SpatialModel<S>> next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
        double time = cursor.getTime();
        while ((next != null)&&(next.getFirst()<=time)) {
            current = next;
            next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
        }
        //Loop invariant: (current.getFirst()<=time)&&((next==null)||(time<next.getFirst()))
        while (!cursor.completed() && !Double.isNaN(time)) {
            IntFunction<R> spatialSignal = cursor.getValue();
            SpatialModel<S> sm = current.getSecond();
            DistanceStructure<S, ?> f = distance.apply(sm);
            toReturn.add(time, SpatialComputation.somewhere(domain, spatialSignal, f));
            double nextTime = cursor.forward();
            while ((next != null)&&(next.getFirst()<nextTime)) {
                current = next;
                time = current.getFirst();
                next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
                f = distance.apply(current.getSecond());
                toReturn.add(time, SpatialComputation.somewhere(domain, spatialSignal, f));
            }
            time = nextTime;
            current = (next!=null?next:current);
            next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
        }

        //TODO: Manage end of signal!
        return toReturn;

    }

}
