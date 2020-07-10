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

package eu.quanticol.moonlight.monitoring.spatialtemporal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Function;

import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.util.Pair;

/**
 * Strategy to interpret the Reach spatial logic operator.
 *
 * @param <S> Spatial Graph Edge Type
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see SpatialTemporalMonitor
 */
public class SpatialTemporalMonitorReach<S, T, R>
        implements SpatialTemporalMonitor<S, T, R>
{
	private final SpatialTemporalMonitor<S, T, R> m1;
	private final Function<SpatialModel<S>, DistanceStructure<S, ?>> distance;
	private final SpatialTemporalMonitor<S, T, R> m2;
	private final SignalDomain<R> domain;

	public SpatialTemporalMonitorReach(SpatialTemporalMonitor<S, T, R> m1,
                                       Function<SpatialModel<S>,
                                       DistanceStructure<S, ?>> distance,
                                       SpatialTemporalMonitor<S, T, R> m2,
                                       SignalDomain<R> domain)
    {
		this.m1 = m1;
		this.distance = distance;
		this.m2 = m2;
		this.domain = domain;
	}

	@Override
	public SpatialTemporalSignal<R> monitor(LocationService<S> locationService,
                                            SpatialTemporalSignal<T> signal)
    {
		return computeReachDynamic(locationService,
                                   m1.monitor(locationService, signal),
                                   m2.monitor(locationService, signal));
	}
	
	private SpatialTemporalSignal<R> computeReachDynamic(
	        LocationService<S> locationService,
            SpatialTemporalSignal<R> s1,
            SpatialTemporalSignal<R> s2)
    {
        SpatialTemporalSignal<R> toReturn = new SpatialTemporalSignal<R>(s1.getNumberOfLocations());
        if (locationService.isEmpty()) {
            return toReturn;
        }
        ParallelSignalCursor<R> c1 = s1.getSignalCursor(true);
        ParallelSignalCursor<R> c2 = s2.getSignalCursor(true);
        Iterator<Pair<Double, SpatialModel<S>>> locationServiceIterator = locationService.times();
        Pair<Double, SpatialModel<S>> current = locationServiceIterator.next();
        Pair<Double, SpatialModel<S>> next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
        double time = Math.max(s1.start(), s2.start());
        while ((next != null)&&(next.getFirst()<=time)) {
            current = next;
            next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
        }
        //Loop invariant: (current.getFirst()<=time)&&((next==null)||(time<next.getFirst()))
        c1.move(time);
        c2.move(time);
        while (!c1.completed() && !c2.completed() && !Double.isNaN(time)) {
            Function<Integer, R> spatialSignal1 = c1.getValue();
            Function<Integer, R> spatialSignal2 = c2.getValue();
            SpatialModel<S> sm = current.getSecond();
            DistanceStructure<S, ?> f = distance.apply(sm);
            ArrayList<R> values =  f.reach(domain, spatialSignal1, spatialSignal2);
            toReturn.add(time, (values::get));
            double nextTime = Math.min(c1.nextTime(), c2.nextTime());
            c1.move(nextTime);
            c2.move(nextTime);
            while ((next != null)&&(next.getFirst()<nextTime)) {
                current = next;
                time = current.getFirst();
                next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
                f = distance.apply(current.getSecond());
                values =  f.reach(domain, spatialSignal1, spatialSignal2);
                toReturn.add(time, f.escape(domain,(values::get)));
            }
            time = nextTime;
            current = (next!=null?next:current);
            next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
        }
        return toReturn;
	}

	
}
