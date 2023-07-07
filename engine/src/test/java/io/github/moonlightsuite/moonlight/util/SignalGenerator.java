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
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.moonlightsuite.moonlight.util;

import io.github.moonlightsuite.moonlight.offline.signal.Signal;

import java.util.Random;
import java.util.function.DoubleFunction;

/**
 * Utility class used to generate different kind of signals.
 *
 */
public class SignalGenerator {

    public static <T> double fillArray(
            double[] timeArray,
            T[] dataArray,
            DoubleFunction<T> generator,
            double start,
            DoubleFunction<Double> timeStep) {
        double time = start;
        for( int i=0 ; i<timeArray.length ; i++ ) {
            timeArray[i] = time;
            dataArray[i] = generator.apply(time);
            double dt = timeStep.apply(time);
            time += dt;
        }
        return time;
    }

    public static DoubleFunction<Boolean> booleanGenerator(Random r , double pTrue ) {
        return d -> (r.nextDouble()<pTrue);
    }

    public static DoubleFunction<Integer> integerGenerator(Random r, int min, int max) {
        return d -> min+r.nextInt(max-min);
    }

    public static DoubleFunction<Double> realGenerator(Random r, double min, double max) {
        return d -> min+(max-min)*r.nextDouble();
    }

    public static DoubleFunction<String> stringGenerator(Random r, String[] values) {
        return d -> values[r.nextInt(values.length)];
    }

    public static <T> Signal<T> createSignal(double[] time, T[] values) {
        Signal<T> signal =  new Signal<>();
        for( int i=0 ; i<time.length ; i++ ) {
            signal.add(time[i],values[i]);
        }
        return signal;
    }

    public static <T> Signal<T> createSignal(double[] time, DoubleFunction<T> f) {
        Signal<T> signal =  new Signal<>();
        for( int i=0 ; i<time.length ; i++ ) {
            signal.add(time[i],f.apply(time[i]));
        }
        return signal;
    }
}
