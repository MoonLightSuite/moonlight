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

/**
 * Immutable data type that represents an interval over
 * the set of real numbers (here limited to doubles).
 *
 * @see AbstractInterval
 */
public class Interval extends AbstractInterval<Double> {

    /**
     * Constructs an empty Interval
     */
    public Interval() {
        super(0.0, 0.0, true, true);
    }

    /**
     * Constructs a degenerated interval of the kind [value, value]
     * @param value the only element of the interval
     */
    public Interval(Number value) {
        super(value.doubleValue(), value.doubleValue(),
                false, false);
    }

    /**
     * Constructs an interval of the kind [start, end]
     * @param start left bound of the interval
     * @param end right bound of the interval
     */
    public Interval(Number start, Number end) {
        super(start.doubleValue(), end.doubleValue(), false, false);
    }

    /**
     * Constructs an interval of the kind [start, end) or [start, end]
     * @param start left bound of the interval
     * @param end right bound of the interval
     * @param openOnRight marks whether the right bound is included or not
     */
    public Interval(Number start, Number end, boolean openOnRight) {
        super(start.doubleValue(), end.doubleValue(), false, openOnRight);
    }

    /**
     * Constructs an interval of any kind between start and end
     * @param start left bound of the interval
     * @param end right bound of the interval
     * @param openOnLeft marks whether the left bound is included or not
     * @param openOnRight marks whether the right bound is included or not
     */
    public Interval(Number start, Number end,
                    boolean openOnLeft, boolean openOnRight)
    {
        super(start.doubleValue(), end.doubleValue(), openOnLeft, openOnRight);
    }

    /**
     * @param offset numerical value to translate the interval
     * @return a new Interval translated on the right by {@code offset}
     */
    public Interval translate(Double offset) {
        return new Interval(getStart() + offset, getEnd() + offset,
                             isOpenOnLeft(), isOpenOnRight());
    }

    /**
     * Generates a new interval by combining two of them
     * @param i1 first interval (a1, b1)
     * @param i2 second interval (a2, b2)
     * @return a new interval of the kind (a1 + a2, b1 + b2)
     * @throws NullPointerException when any of the two is null
     */
    public static Interval combine(Interval i1, Interval i2) {
        if(i1.isEmpty())
            return i2;

        if(i2.isEmpty())
            return i1;

        return new Interval(i1.getStart() + i2.getStart(),
                i1.getEnd() + i2.getEnd());
    }

    /**
     * @return the widest possible Interval
     */
    public static Interval any() {
        return new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }
}
