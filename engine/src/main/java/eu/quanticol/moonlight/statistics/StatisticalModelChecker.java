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

package eu.quanticol.moonlight.statistics;

import eu.quanticol.moonlight.util.Logger;
import eu.quanticol.moonlight.statistics.SignalStatistics.Statistics;
import eu.quanticol.moonlight.offline.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The StatisticalModelChecker is a class for evaluating a property over
 * multiple trajectories and determining the probability of satisfying it.
 *
 * TODO: write some tests for the whole package
 *
 * @param <S> The Value Type of Spatial Model edges
 * @param <T> The Type of the Domain of the Input Signal
 * @param <R> The Interpretation Type of the monitoring
 *
 * @see SignalStatistics for info about the stats recorded and
 * @see Statistics for info about the result format
 */
public class StatisticalModelChecker<S, T, R> {
    private static final Logger LOG = Logger.getLogger();

    /**
     * Input Parameters
     */
    private final SpatialTemporalMonitor<S, T, R> monitor;
    private final Collection<? extends SpatialTemporalSignal<T>> samples;
    private final LocationService<Double, S> locService;

    /**
     * Output Results
     */
    private final List<SpatialTemporalSignal<R>> results;
    private final SignalStatistics<SpatialTemporalSignal<R>> stats;

    private final Collection<Thread> threads = new ArrayList<>();

    /**
     * Instantiating a Statistical Model Checker requires the following:
     * @param propertyMonitor a monitor for the property of interest
     * @param trajectorySamples a Collection of trajectories to analyze
     * @param locationService a location service for the space model of interest
     */
    public StatisticalModelChecker(
            SpatialTemporalMonitor<S, T, R> propertyMonitor,
            Collection<? extends SpatialTemporalSignal<T>> trajectorySamples,
            LocationService<Double, S> locationService)
    {
        monitor = propertyMonitor;
        samples = trajectorySamples;
        locService = locationService;

        SpatialTemporalSignal<T> s  = trajectorySamples.iterator().next();
        int locations = s.getNumberOfLocations();
        int timePoints = s.getSignals().get(0).getTimeSet().size();

        stats =  new SignalStatistics<>(locations, timePoints);
        results = new ArrayList<>();
    }

    /**
     * This is the actual execution of the statistical model checking,
     * by executing the chosen <code>monitor</code> over the provided
     * <code>samples</code>.
     *
     * This has the side effect of updating <code>results</code>
     * and <code>stats</code>.
     */
    public void compute() {
        int i = 0;
        for (SpatialTemporalSignal<T> s : samples) {

            final int n = i;
            Thread t = new Thread(() -> stats.record(
                () ->
                {
                    SpatialTemporalSignal<R> r = monitor.monitor(locService, s);
                    LOG.info("Monitoring " + n + " finished!");
                    return r;
                }
                ));

            threads.add(t);
            LOG.info("Thread starting.");
            t.start();

            i++;
        }
        try {
            for (Thread thread : threads) {
                thread.join();
            }
            results.addAll(stats.getResults());
        } catch(InterruptedException e) {
            LOG.fatal("Thread crashed! " + e.getMessage());
            Thread.currentThread().interrupt();
        }

    }


    /**
     * @return The results collected, following a <code>compute</code> execution
     */
    public Statistics[][] getStats() {
        return stats.analyze();
    }

    /**
     * @return The results collected, following a <code>compute</code> execution
     */
    public List<SpatialTemporalSignal<R>> getResults() {
        return results;
    }


}
