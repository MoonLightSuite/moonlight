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

package eu.quanticol.moonlight.util;

import eu.quanticol.moonlight.offline.signal.Signal;
import eu.quanticol.moonlight.offline.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.space.GraphModel;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.space.LocationServiceList;
import eu.quanticol.moonlight.core.space.SpatialModel;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author loreti
 */
public class Utils {

    private Utils() {
        //utility class
    }

    public static <T> Signal<T> createSignal(double start, double end, double dt, Function<Double, T> f) {
        Signal<T> signal = new Signal<>();
        double time = start;
        while (time <= end) {
            signal.add(time, f.apply(time));
            time += dt;
        }
        signal.endAt(end);
        return signal;
    }


    public static <T> SpatialTemporalSignal<T> createSpatioTemporalSignal(int size, double start, double dt, double end, BiFunction<Double, Integer, T> f) {
        SpatialTemporalSignal<T> s = new SpatialTemporalSignal<>(size);
        double time = start;
        while (time < end) {
            double current = time;
            s.add(time, (i -> f.apply(current, i)));
            time += dt;
        }
        s.add(end, (i -> f.apply(end, i)));
        return s;
    }

    public static <T> SpatialTemporalSignal<T> createSpatioTemporalSignalFromGrid(int rowLength, int columnLength, double start, double dt, double end, BiFunction<Double, Pair<Integer,Integer>, T> f) {
        SpatialTemporalSignal<T> s = new SpatialTemporalSignal<>(rowLength*columnLength);
        double time = start;
        while (time < end) {
            double current = time;
            s.add(time, (i -> f.apply(current, gridLocationOf(i,rowLength,columnLength))));
            time += dt;
        }
        s.add(end, (i -> f.apply(end, gridLocationOf(i,rowLength,columnLength))));
        return s;
    }


    public static <T> SpatialModel<T> createSpatialModel(int size, Map<Pair<Integer, Integer>, T> edges) {
        return createSpatialModel(size, (i, j) -> edges.get(new Pair<>(i, j)));
    }

    public static <T> SpatialModel<T> createSpatialModel(int size, BiFunction<Integer, Integer, T> edges) {
        GraphModel<T> model = new GraphModel<>(size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                T value = edges.apply(i, j);
                if (value != null) {
                    model.add(i, value, j);
                }
            }
        }
        return model;
    }

    public static <T> SpatialModel<T> createGridModel(int rows, int columns, boolean directed, T w) {
        int size = rows * columns;
        GraphModel<T> model = new GraphModel<>(size);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (i + 1 < rows) {
                    model.add(gridIndexOf(i, j, columns), w, gridIndexOf(i + 1, j, columns));
                    if (!directed) {
                        model.add(gridIndexOf(i + 1, j, columns), w, gridIndexOf(i, j, columns));
                    }
                }
                if (j + 1 < columns) {
                    model.add(gridIndexOf(i, j, columns), w, gridIndexOf(i, j + 1, columns));
                    if (!directed) {
                        model.add(gridIndexOf(i, j + 1, columns), w, gridIndexOf(i, j, columns));
                    }
                }
            }
        }
        return model;
    }


    public static int gridIndexOf(int r, int c, int columns) {
        return r * columns + c;
    }


    public static Pair<Integer, Integer> gridLocationOf(int i, int rows, int columns) {
        int r = i / columns;
        int c = i % columns;
        if ((r >= rows) || (c >= columns)) {
            throw new IllegalArgumentException();
        }
        return new Pair<>(r, c);
    }

    public static<T> GraphModel<T> createGraphFromMatlabData(int nodes,
                                             int[][] edges,
                                             T[] weights)
    {
        final int SOURCE = 0;
        final int DESTINATION = 1;
        GraphModel<T> space = new GraphModel<>(nodes);
        if(edges.length != weights.length)
            throw new IllegalArgumentException("Mismatching edges provided");

        for(int i= 0; i < edges.length; i++) {
            space.add(edges[i][SOURCE] - 1,     //Matlab indices fix
                      weights[i],
                      edges[i][DESTINATION] - 1); //Matlab indices fix
        }

        return space;
    }

    public static<T> LocationService<Double, T>
    createLocationServiceFromTimesAndModels(double[] times,
                                            SpatialModel<T>[] models)
    {
        LocationServiceList<T> locSvc = new LocationServiceList<>();
        if(times.length != models.length)
            throw new IllegalArgumentException("Mismatched arguments provided");

        for(int i = 0; i < times.length; i++) {
            locSvc.add(times[i], models[i]);
        }

        return locSvc;
    }



    public static LocationService<Double, Double> createLocServiceFromSetMatrix(Object[] cgraph1) {
        double[][] matrix;
        LocationServiceList<Double> locService = new LocationServiceList<>();
        for (int k = 0; k < cgraph1.length; k++) {
            double t = ((float) k);
            matrix = (double[][]) cgraph1[(int) Math.floor(t)];
            int size = matrix.length;
            GraphModel<Double> graphModel = new GraphModel<>(size);
            for (int i = 0; i < matrix.length; i++) {
                for (int j = i + 1; j < matrix[i].length; j++) {
                    graphModel.add(i, matrix[i][j], j);
                    graphModel.add(j, matrix[j][i], i);
                }
            }
            locService.add(t, graphModel);
        }
        return locService;
    }

    public static LocationService<Double, Double> createLocServiceStatic(double start, double dt, double end, SpatialModel<Double> graph) {
        LocationServiceList<Double> locService = new LocationServiceList<>();
        double time = start;
        while (time < end) {
            double current = time;
            locService.add(time, graph);
            time += dt;
        }
        locService.add(end,graph);
        return locService;
    }

    public static LocationService<Double, Double>
    createLocServiceStaticFromTimeTraj(double [] time ,
                                       SpatialModel<Double> graph)
    {
        LocationServiceList<Double> locService = new LocationServiceList<>();
        for (double v : time) {
            locService.add(v, graph);
        }
        return locService;
    }

}