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

/**
 * Interface that must be implemented by a distance metric
 * to be used with the spatial operators and models of Moonlight.
 *
 * @param <M> The metric type of the distance
 */
public interface DistanceDomain<M> {

    /**
     * Maximal allowed distance
     *
     * @return the maximal possible distance
     */
    M infinity();

    /**
     * Method to combine the metric `x`, `factor` times
     *
     * @param x      metric
     * @param factor times of the combination
     * @return multiplication of the metric times the factor
     */
    default M multiply(M x, int factor) {
        M accumul = zero();
        for (int i = 0; i < factor; i++) {
            accumul = sum(x, accumul);
        }
        return accumul;
    }

    /**
     * Minimal allowed distance
     *
     * @return the minimal possible distance
     */
    M zero();

    /**
     * Method to combine two distances
     *
     * @param x first distance to combine
     * @param y second distance to combine
     * @return the resulting distance
     */
    M sum(M x, M y);

    /**
     * Tells whether the first distance is smaller than the second
     *
     * @param x the first distance to be analyzed
     * @param y the second distance to be analyzed
     * @return <code>true</code> if <code>x</code> is smaller than
     * <code>y</code>. <code>False</code> otherwise.
     */
    boolean less(M x, M y);

    /**
     * Tells whether the first distance is equivalent to the second
     *
     * @param x the first distance to be analyzed
     * @param y the second distance to be analyzed
     * @return <code>true</code> if <code>x</code> and <code>y</code>
     * are equals. <code>False</code> otherwise.
     */
    boolean equalTo(M x, M y);

    /**
     * Tells whether the first distance is smaller or equal to
     * the second.
     *
     * @param x the first distance to be analyzed
     * @param y the second distance to be analyzed
     * @return <code>true</code> if <code>x</code> is smaller
     * or equal than <code>y</code>.
     * <code>False</code> otherwise.
     */
    boolean lessOrEqual(M x, M y);
}
