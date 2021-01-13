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
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.quanticol.moonlight.signal;

import eu.quanticol.moonlight.util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * This interface is implemented by classes describing a spatial model. This is
 * a labelled graph where nodes are identified by integers, while edges have type <code>T</code>.
 *
 * @param <T> the type of values on edges.
 */
public interface SpatialModel<T> {

    /**
     * Returns the value of the edge connecting <code>src</code> to
     * <code>trg</code>. The value <code>null</code> is returned when
     * there is not any edge connecting the two nodes.
     *
     * @param src source node.
     * @param trg end node.
     * @return the value associated with the edge between src and trg,
     *         or null if it does not exist.
     */
    T get(int src, int trg);

    /**
     * @return the number of locations in the model.
     */
    int size();

    /**
     * Returns the list of exiting edges from <code>l</code>.
     * This is represented as a list of {@link Pair}.
     *
     * @param l a location.
     * @return the list of exiting edges from <code>l</code>.
     */
    List<Pair<Integer, T>> next(int l);

    /**
     * Returns the list of incoming edges in <code>l</code>.
     * This is represented as a list of {@link Pair}.
     *
     * @param l a location.
     * @return the list of incoming edges in <code>l</code>.
     */
    List<Pair<Integer, T>> previous(int l);

    /**
     * @return the set of locations in the model.
     */
    Set<Integer> getLocations();

    /**
     * This is an utility method that builds a SpatialModel from an adjacency list represented as
     * an array of String. Each row in <code>data</code> describes an edge and consists of an array of the
     * form <code>[s0;s1;s2;...;sn]</code> where <code>s0</code> and <code>s1</code> are the
     * (string representation) of the source and target of the edge, while <code>[s2;...;sn]</code> represents
     * the values on the edge that is converted to a {@link MoonLightRecord} via the provided {@link RecordHandler}.
     *
     * @param locations number of locations.
     * @param edgeRecordHandler handler for edges data type.
     * @param data adjacency list.
     * @return a SpatialModel.
     */
    static SpatialModel<MoonLightRecord> buildSpatialModelFromAdjacencyList(int locations, RecordHandler edgeRecordHandler, String[][] data) {
        GraphModel<MoonLightRecord> toReturn = new GraphModel<>(locations);
        for( int i=0 ; i<data.length ; i++ ) {
            String[] row = data[i];
            int src = Integer.parseInt(row[0]);
            int trg = Integer.parseInt(row[1]);
            toReturn.add(src,edgeRecordHandler.fromStringArray(row,2,row.length),trg);
        }
        return toReturn;
    }

    /**
     * This is an utility method that builds a SpatialModel from an adjacency list represented as
     * an array of doubles. Each row in <code>data</code> describes an edge and consists of an array of the
     * form <code>[d0;d1;d2;...;dn]</code> where <code>d0</code> and <code>d1</code> are the
     * (double representation) of the source and target of the edge, while <code>[d2;...;dn]</code> represents
     * the values on the edge that is converted to a {@link MoonLightRecord} via the provided {@link RecordHandler}.
     *
     * @param locations number of locations.
     * @param edgeRecordHandler handler for edges data type.
     * @param data adjacency list.
     * @return a SpatialModel.
     */
    static SpatialModel<MoonLightRecord> buildSpatialModelFromAdjacencyList(int locations, RecordHandler edgeRecordHandler, double[][] data) {
        GraphModel<MoonLightRecord> toReturn = new GraphModel<>(locations);
        for( int i=0 ; i<data.length ; i++ ) {
            double[] row = data[i];
            int src = (int) row[0];
            int trg = (int) row[1];
            toReturn.add(src,edgeRecordHandler.fromDoubleArray(row,2,row.length),trg);
        }
        return toReturn;
    }


    /**
     * This is an utility method that builds a SpatialModel from an adjacency matrix represented as
     * an array of doubles. The element <code>data[i][j]</code> of the matrix contains an array of String
     * <code>[d0;...;dn]</code> representing the values on the edge between i and j, or null if this edge
     * does not exist. This array is converted to a {@link MoonLightRecord} via the provided {@link RecordHandler}.
     *
     * @param locations number of locations.
     * @param edgeRecordHandler handler for edges data type.
     * @param data adjacency matrix.
     * @return a SpatialModel.
     */
    static SpatialModel<MoonLightRecord> buildSpatialModelFromAdjacencyMatrix(int locations, RecordHandler edgeRecordHandler,
                                                                              String[][][] data) {
        GraphModel<MoonLightRecord> toReturn = new GraphModel<>(locations);
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                if (i != j && isFull(data[i][j])) {
                    toReturn.add(i, edgeRecordHandler.fromStringArray(data[i][j]), j);
                }
            }
        }
        return toReturn;
    }

    /**
     * This is an utility method that builds a SpatialModel from an adjacency matrix represented as
     * an array of doubles. The element <code>data[i][j]</code> of the matrix contains an array of String
     * <code>[d0;...;dn]</code> representing the values on the edge between i and j, or null if this edge
     * does not exist. This array is converted to a {@link MoonLightRecord} via the provided {@link RecordHandler}.
     *
     * @param locations number of locations.
     * @param edgeRecordHandler handler for edges data type.
     * @param data adjacency matrix.
     * @return a SpatialModel.
     */
    static SpatialModel<MoonLightRecord> buildSpatialModelFromAdjacencyMatrix(int locations, RecordHandler edgeRecordHandler,
                                                                              double[][][] objects) {
        GraphModel<MoonLightRecord> toReturn = new GraphModel<>(locations);
        for (int i = 0; i < objects.length; i++) {
            for (int j = 0; j < objects[i].length; j++) {
                if (i != j && isFull(objects[i][j])) {
                    toReturn.add(i, edgeRecordHandler.fromDoubleArray(objects[i][j]), j);
                }
            }
        }
        return toReturn;
    }


    static Boolean isFull(String[] array) {
        return !Arrays.stream(array).allMatch(Objects::isNull);
    }

    static Boolean isFull(double[] array) {
        return !Arrays.stream(array).allMatch(Objects::isNull);
    }

}