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

package eu.quanticol.moonlight.formula;

/**
 * Immutable data type that represents an interval over
 * the set of real numbers (here limited to doubles).
 */
public class Interval {
    private final double start;
    private final double end;
    private final boolean openOnRight;
    private final boolean openOnLeft;

    /**
     * Constructs an interval defined of the kind [start, end]
     * @param start left bound of the interval
     * @param end right bound of the interval
     */
    public Interval(double start, double end) {
        this(start, end, false, false);
    }

    /**
     * Constructs an interval defined of the kind [start, end) or [start, end]
     * @param start left bound of the interval
     * @param end right bound of the interval
     * @param openOnRight marks whether the right bound is included or not
     */
    public Interval(double start, double end, boolean openOnRight) {
        this(start, end, false, openOnRight);
    }

    /**
     * Constructs an interval of any kind between start and end
     * @param start left bound of the interval
     * @param end right bound of the interval
     * @param openOnLeft marks whether the left bound is included or not
     * @param openOnRight marks whether the right bound is included or not
     */
    public Interval(double start, double end, boolean openOnLeft, boolean openOnRight) {
        this.start = start;
        this.end = end;
        this.openOnLeft = openOnLeft;
        this.openOnRight = openOnRight;
    }

    /**
     * Constructs a singleton interval that contains only the provided number
     * @param number the only number included in the interval
     * @return an interval of the kind [number, number]
     */
    public static Interval fromDouble(Double number) {
        return new Interval(number, number);
    }

    /**
     * @return an empty Interval
     */
    public static Interval empty() {
        return new Interval(0, 0, true);
    }

    /**
     * @return the left bound of the interval
     */
    public double getStart() {
        return start;
    }

    /**
     * @return the right bound of the interval
     */
    public double getEnd() {
        return end;
    }

    /**
     * Checks whether the passed value belongs to the interval
     * @param value the value to be checked
     * @return true if the value belongs to the interval, false otherwise.
     */
    public boolean contains(double value) {
        return  (value > start && value < end)  ||
                (value == start && !openOnLeft) ||
                (value == end && !openOnRight);
    }

    /**
     * @return tells whether the interval is empty or not
     */
    public boolean isEmpty() {
        return start == end && (openOnLeft || openOnRight);
    }

    /**
     * @return tells whether the right bound is included in the interval or not
     */
    public boolean isOpenOnRight() {
        return openOnRight;
    }

    /**
     * @return tells whether the left bound is included in the interval or not
     */
    public boolean isOpenOnLeft() {
        return openOnLeft;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(end);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + (openOnRight ? 1231 : 1237);
        temp = Double.doubleToLongBits(start);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Interval other = (Interval) obj;
        if (Double.doubleToLongBits(end) != Double.doubleToLongBits(other.end))
            return false;
        if (openOnRight != other.openOnRight)
            return false;
        if (Double.doubleToLongBits(start) != Double.doubleToLongBits(other.start))
            return false;
        return true;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String output = "Interval: ";

        if(openOnLeft)
            output += "(";
        else
            output += "[";

        output += start + "," + end;

        if(openOnRight)
            output += ")";
        else
            output += "]";

        return output;
    }
}
