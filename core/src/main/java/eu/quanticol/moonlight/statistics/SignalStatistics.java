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

package eu.quanticol.moonlight.statistics;

import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import static java.util.Collections.synchronizedCollection;

/**
 * {@code SignalStatistics} is an auxiliary class that is used by the
 * {@code StatisticalModelChecker} to record stats about the monitoring
 * process and then return them in some readable way.
 *
 * The stats data is recorded by {@link #record(Supplier)} trace by trace,
 * but actual stats are computed lazily
 * (i.e. only on call of the {@link #analyze()} method).
 *
 * {@link Statistics} is the Data Transfer Object (DTO) devoted to showing
 * the results.
 *
 * TODO: Generalize so that it can accepts both
 *       SpatialTemporalSignals and TemporalSignals
 *
 * @param <T> the Signal Type of interest
 *
 * @see StatisticalModelChecker for usage details
 */
public class SignalStatistics<T extends SpatialTemporalSignal<?>> {
    private final Collection<T> results = synchronizedCollection(new ArrayList<>());
    private final Collection<Float> durations = new ArrayList<>();

    private final long startingTime;

    /**
     * Initializes the internal timer(s) for the statistics
     */
    public SignalStatistics() {
        startingTime = System.currentTimeMillis();
    }

    /**
     * Method to record statistics about a task that has to be run.
     * Note that the actual running is done inside the record method,
     * but the result is passed through so that the whole process
     * is transparent to the caller.
     *
     * @param f the task to run
     * @return the result of the task, so that it can be
     *         passed through to the caller
     */
    public T record(Supplier<T> f) {
        try {
            long start = System.currentTimeMillis();
            T result = f.get(); //Supplier code execution (i.e. f.apply())
            long end = System.currentTimeMillis();

            float duration = (float)((end - start) / 1000.0);

            durations.add(duration);
            results.add(result);
            return result;
        } catch(Exception e) {
            System.out.println("ERROR: Statistics computation failed");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Executes the computations and builds a DTO with the results.
     * Note that this method is pure, and it may also be used to
     * gather statistics of intermediate results
     * (i.e. when not all traces have been processed).
     *
     * @return a DTO containing the final statistics
     */
    public Statistics analyze() {
        float avg = computeAverage();
        float var = computeVariance(avg);
        float stdDev = (float) Math.sqrt(var);
        float execTime = computeAvgExecTime();
        int cnt = results.size();

        long endingTime = System.currentTimeMillis();
        float totalTime = (float) ((endingTime - startingTime) / 1000.0);

        return new Statistics(avg, execTime, stdDev, var, cnt, totalTime);
    }

    /**
     * @return the average result over the analyzed traces.
     */
    private float computeAverage() {
        float value = 0;

        for (SpatialTemporalSignal<?> result : results) {
            Signal<?> s = result.getSignals().get(0);
            if (s.valueAt(0) instanceof Float) {
                value += (Float) s.valueAt(0);
            } else if (s.valueAt(0) instanceof Boolean) {
                value += (Boolean) s.valueAt(0) ? 1 : 0;
            }
            else
                throw new InvalidParameterException("Unknown Signal Output");
        }

        return value / results.size();
    }

    /**
     * @return the average execution time over the analyzed traces.
     */
    private float computeAvgExecTime() {
        float t = 0;

        for (Float duration : durations) {
            t += duration;
        }

        return t / durations.size();
    }

    /**
     * @return the result variance over the analyzed traces.
     */
    private float computeVariance(float avg) {
        float value = 0;

        for (SpatialTemporalSignal<?> result : results) {
            Signal<?> s = result.getSignals().get(0);
            if (s.valueAt(0) instanceof Float) {
                float v = (Float) s.valueAt(0);
                value += Math.pow((v - avg), 2);
            } else if (s.valueAt(0) instanceof Boolean) {
                float v = (Boolean) s.valueAt(0) ? 1 : 0;
                value += Math.pow((v - avg), 2);
            }
            else
                throw new InvalidParameterException("Unknown Signal Output");
        }

        return value / (results.size());
    }


    /**
     * DTO that contains the computed statistics
     */
    public static class Statistics implements Serializable {

        Statistics(float avg, float exec, float std,
                   float var, int cnt, float tot)
        {
            average = avg;
            executionTime = exec;
            stdDeviation = std;
            variance = var;
            count = cnt;
            totalExecutionTime = tot;
        }

        /**
         * Average of the results of the monitoring process
         */
        public final float average;

        /**
         * Standard deviation of the results of the monitoring process
         */
        public final float stdDeviation;

        /**
         * Variance of the results of the monitoring process
         */
        public final float variance;

        /**
         * Average execution time of the monitoring process
         */
        public final float executionTime;

        /**
         * Number of executions of the monitoring process
         */
        public final int count;

        /**
         * Total execution time of the monitoring process
         */
        public final float totalExecutionTime;

        @Override
        public String toString() {
            return  "AVG:" + average + " | STD:" + stdDeviation +
                    " | VAR:" + variance + " | CNT:" + count +
                    " | AVG_EXEC_TIME:" + executionTime + "ms" +
                    " | TOT_EXEC_TIME:" + totalExecutionTime + "ms";
        }
    }
}
