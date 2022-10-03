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

package eu.quanticol.moonlight.core.space;

import java.util.function.Function;

/**
 * This interface abstracts the computation of the distance
 * for spatial algorithms.
 *
 * @param <E> Type of edge labels of the spatial model.
 * @param <M> Type of the distance metric
 */
public interface DistanceStructure<E, M> {

    /**
     * Method to retrieve the distance between the two locations.
     * The operation can be commutative or not, depending on the implementation.
     *
     * @param from source form which the computation starts
     * @param to   destination to which to look for computing the distance
     * @return the aggregated distance to reach <code>to</code>
     * starting at <code>from</code>.
     */
    M getDistance(int from, int to);

    /**
     * Helper method, might be preferable to the combination of
     * <code>isWithinBounds(getDistance(from, to))</code>,
     * if it makes sense for the current distance structure.
     *
     * @param from source form which the computation starts
     * @param to   destination to which to look for the analysis
     * @return <code>true</code> when <code>to</code> can be reached.
     * <code>false</code> otherwise
     */
    boolean areWithinBounds(int from, int to);

    /**
     * Method to assess whether one location is within the bounds of the
     * spatial structure
     *
     * @param d distance
     * @return <code>true</code> when <code>d</code> is within bounds.
     * <code>false</code> otherwise
     */
    boolean isWithinBounds(M d);

    /**
     * @return the distance function used to compute the distance.
     */
    Function<E, M> getDistanceFunction();

    /**
     * @return the distance domain used to compute the distance.
     */
    DistanceDomain<M> getDistanceDomain();

    /**
     * @return an approximation of the locations that are close
     * enough to the current one (to optimize analysis).
     */
    default int[] getBoundingBox(int i) {
        return new int[]{0, getModel().size()};
    }

    /**
     * @return the spatial model on which the distance structure is defined.
     */
    SpatialModel<E> getModel();

    /**
     * @return an approximation of the locations that are close
     * enough to the current one (to optimize analysis).
     */
    default int[] getNeighbourhood(int i) {
        throw new UnsupportedOperationException("Not supported by current " +
                "distance structure");
    }
}
