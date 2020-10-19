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

package eu.quanticol.moonlight.util;

import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

import java.util.ArrayList;
import java.util.List;

/**
 * Spatial-temporal signal that represents a generic trace of the kind
 * (s,t) -> (x1, ..., xn).
 * Note that x1, ..., xn must at least implement Comparable
 *
 * The goal of this class is to minimize the raw usage of the Comparable class,
 * so that user usage can be as type-safe as possible.
 *
 * Moreover, it performs some input checks to make sure the generated signal
 * is correct in terms of the spatial-temporal domain.
 *
 * @see Comparable
 * @see SpatialTemporalSignal
 */
public class MultiValuedTrace extends SpatialTemporalSignal<List<Comparable<?>>> {
    private final int length;
    private int dimensions = 0;
    private final List<Comparable<?>[][]> data;

    /**
     * Fixes the dimensions of the signal.
     *
     * @param size the number of locations of the Spatial model.
     * @param length the time span of the temporal data.
     */
    public MultiValuedTrace(int size, int length) {
        super(size);

        this.length = length;
        data = new ArrayList<>();
    }

    /**
     * Takes the data stored internally so far and performs the actual
     * conversion to Signals.
     * This is a required step before performing any kind of monitoring
     * or analysis over the signal.
     */
    public void initialize() {
        dimensions = data.size();

        if (data.isEmpty())
            throw new IllegalArgumentException("Empty signal passed");

        //System.out.println("Starting Signal Initialization");
        for(int t = 0; t < length; t ++) {
            int time = t;
            add(t, i -> setSignal(i, time));
        }
        //System.out.println("Completed Signal Initialization");

        data.clear();
    }

    /**
     * Given some Comparable data, it performs some checks and prepares it
     * to be later added as the index dimension of the n-dimensional signal.
     *
     * @see Comparable to learn more about the minimum data requirements
     *
     * @param dimData data to be set as the provided dimension
     * @param index ith dimension of the n-dimensional signal.
     * @return the MultiValuedSignal itself, so that the method can be chained.
     */
    public MultiValuedTrace setDimension(
            Comparable<?>[][] dimData,
            int index) {

        if(!data.isEmpty() && dimData.length != size())
            throw new IllegalArgumentException("Mismatching space size ");

        if(!data.isEmpty() && dimData[0].length != length)
            throw new IllegalArgumentException("Mismatching time length");

        // Perhaps we should check if all required values exist
        data.add(index, dimData);

        return this;
    }


    /**
     * It generates an n-dimensional list, for the given space-time element,
     * based on the dimensions data stored, s.t.
     * (t1, l1) |-> (x1, ..., xn)
     *
     * @param l spatial location of interest
     * @param t time instant of interest
     * @return (x1, ..., xn) n-dimensional data list
     */
    private List<Comparable<?>> setSignal(int l, int t) {
        List<Comparable<?>> signal = new ArrayList<>();

        for (Comparable<?>[][] datum : data) {
            signal.add(datum[l][t]);
        }

        return signal;
    }
    /**
     * @return the number of dimensions of the signal
     */
    public int dimensions() {
        return dimensions;
    }

}
