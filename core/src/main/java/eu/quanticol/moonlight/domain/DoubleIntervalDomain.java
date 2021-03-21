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

package eu.quanticol.moonlight.domain;

import eu.quanticol.moonlight.signal.DataHandler;

/**
 * Signal domain to support intervals.
 * Currently limited to intervals of doubles.
 *
 * @see Interval
 * @see SignalDomain
 */
public class DoubleIntervalDomain implements SignalDomain<AbstractInterval<Double>> {
    private static final AbstractInterval<Double> NEGATIVE_INFINITY =
            new AbstractInterval<>(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    private static final AbstractInterval<Double> POSITIVE_INFINITY =
            new AbstractInterval<>(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    private static final AbstractInterval<Double> TOTAL_INTERVAL =
            new AbstractInterval<>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    @Override
    public AbstractInterval<Double> conjunction(AbstractInterval<Double> x, AbstractInterval<Double> y) {
        return new AbstractInterval<>(Math.min(x.getStart(), y.getStart()),
                Math.min(x.getEnd(), y.getEnd()));
    }

    @Override
    public AbstractInterval<Double> disjunction(AbstractInterval<Double> x, AbstractInterval<Double> y) {
        return new AbstractInterval<>(Math.max(x.getStart(), y.getStart()),
                            Math.max(x.getEnd(), y.getEnd()));
    }

    @Override
    public AbstractInterval<Double> negation(AbstractInterval<Double> x) {
        return new AbstractInterval<>(- x.getEnd(), - x.getStart());
    }

    @Override
    public AbstractInterval<Double> min() {
        return NEGATIVE_INFINITY;
    }

    @Override
    public AbstractInterval<Double> max() {
        return POSITIVE_INFINITY;
    }

    @Override
    public AbstractInterval<Double> any() {
        return TOTAL_INTERVAL;
    }

    /* NOT IMPLEMENTED/USED METHODS */

    @Override
    public DataHandler<AbstractInterval<Double>> getDataHandler() {
        return null;
    }

    @Override
    public boolean equalTo(AbstractInterval<Double> x, AbstractInterval<Double> y) {
        return x.equals(y);
    }

    @Override
    public AbstractInterval<Double> valueOf(boolean b) {
        throw new UnsupportedOperationException("Not implemented as not used.");
    }

    @Override
    public AbstractInterval<Double> valueOf(double v) {
        throw new UnsupportedOperationException("Not implemented as not used.");
    }

    @Override
    public AbstractInterval<Double> computeLessThan(double v1, double v2) {
        throw new UnsupportedOperationException("Not implemented as not in " +
                                                "the original scope of the " +
                                                "class development.");
    }

    @Override
    public AbstractInterval<Double> computeLessOrEqualThan(double v1, double v2) {
        throw new UnsupportedOperationException("Not implemented as not in " +
                                                "the original scope of the " +
                                                "class development.");
    }

    @Override
    public AbstractInterval<Double> computeEqualTo(double v1, double v2) {
        throw new UnsupportedOperationException("Not implemented as not in " +
                                                "the original scope of the " +
                                                "class development.");
    }

    @Override
    public AbstractInterval<Double> computeGreaterThan(double v1, double v2) {
        throw new UnsupportedOperationException("Not implemented as not in " +
                                                "the original scope of the " +
                                                "class development.");
    }

    @Override
    public AbstractInterval<Double> computeGreaterOrEqualThan(double v1, double v2) {
        throw new UnsupportedOperationException("Not implemented as not in " +
                                                "the original scope of the " +
                                                "class development.");
    }
}
