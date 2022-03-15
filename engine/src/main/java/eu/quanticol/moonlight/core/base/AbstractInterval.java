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

package eu.quanticol.moonlight.core.base;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Abstract immutable data type that represents an interval over parameter T.
 *
 * In general, to define an interval over a set, we only need a
 * total ordering relation defined over that set.
 * For this reason, I am requiring Intervals to be defined over a type T
 * that implements a Comparable interface (of any extension of that).
 * The Comparable interface is slightly stricter than the previous interval
 * definition, as it also requires some (integer) metric to assess the distance
 * between the objects compared.
 * However, I believe this seems coherent with the approach adopted
 * by Moonlight, and being implemented by all numeric types, it provides a
 * fairly general support out of the box.
 *
 * For the reason why AbstractInterval implements Comparable itself,
 * see {@link AbstractInterval#compareTo(AbstractInterval)}.
 *
 * @param <T> Type of the interval (currently only numbers make sense)
 */
public class AbstractInterval<T extends Comparable<T>>
        implements Comparable<AbstractInterval<T>>
{
    private final T start;
    private final T end;
    private final boolean openOnRight;
    private final boolean openOnLeft;

    /**
     * Constructs an interval of any kind between start and end
     * @param start left bound of the interval
     * @param end right bound of the interval
     * @param openOnLeft marks whether the left bound is included or not
     * @param openOnRight marks whether the right bound is included or not
     */
    public AbstractInterval(T start, T end, boolean openOnLeft, boolean openOnRight) {
        if(start.compareTo(end) > 0)
            throw new IllegalArgumentException("An Interval must have the " +
                                               "left bound smaller than the " +
                                               "right bound");

        this.start = start;
        this.end = end;
        this.openOnLeft = openOnLeft;
        this.openOnRight = openOnRight;
    }

    public AbstractInterval(T start, T end) {
        this(start, end,false,false);
    }

    /**
     * @return the left bound of the interval
     */
    public T getStart() {
        return start;
    }

    /**
     * @return the right bound of the interval
     */
    public T getEnd() {
        return end;
    }

    /**
     * Checks whether the passed value belongs to the interval
     * @param value the value to be checked
     * @return true if the value belongs to the interval, false otherwise.
     */
    public boolean contains(T value) {
        if (value != null)
            return (value.compareTo(start) > 0 && value.compareTo(end) < 0)
                || (value.equals(start) && !openOnLeft)
                || (value.equals(end) && !openOnRight);

        return false;
    }

    /**
     * Non-strict set containment between intervals
     * @param target interval to be checked
     * @return true if the argument interval is contained, false otherwise
     * @throws ClassCastException when the target is of a different type
     */
    public boolean contains(AbstractInterval<?> target) {
        if (target != null) {
            AbstractInterval<T> interval = (AbstractInterval<T>) target;
            return  // same object
                    this.equals(interval)                                ||
                    // strictly contained
                    (contains(interval.start) && contains(interval.end)) ||
                    // contained on the right
                    (start == interval.start &&
                     openOnLeft == target.openOnLeft &&
                     contains(interval.end))                             ||
                    // contained on the left
                    (end == target.end &&
                     openOnRight == target.openOnRight &&
                     contains(interval.end));
        }
        return false;
    }

    /**
     * @return tells whether the interval is empty or not
     */
    public boolean isEmpty() {
        return start.equals(end) && (openOnLeft || openOnRight);
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
     * Note that in classical interval arithmetic no total ordering
     * relation is defined over intervals.
     * Nevertheless, a strict partial ordering does exist,
     * so I decided to support it, and to make the function total by
     * failing at runtime when a comparison cannot be made.
     *
     * A summary of the logic is the following:
     * - this.sup < o.inf => compare
     * - this.inf > o.sup => compare
     * - o is null || o is contained in this => error
     *
     * @param o target interval of the comparison
     * @return a number corresponding to the result of the comparison
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException if the specified object's type prevents it
     * @throws UnsupportedOperationException if the interval cannot be compared
     */
    @Override
    public int compareTo(@NotNull AbstractInterval<T> o) {
        if(this.equals(o))
            return 0;

        if(getEnd().compareTo(o.getStart()) < 0) {
            return getEnd().compareTo(o.getStart());
        }
        if(getStart().compareTo(o.getEnd()) > 0) {
            return getStart().compareTo(o.getEnd());
        }

        throw new UnsupportedOperationException("Unable to compare interval " +
                                                " with " + o);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = end.hashCode();
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + (openOnRight ? 1231 : 1237);
        temp = start.hashCode();
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("unchecked")
        AbstractInterval<T> other = (AbstractInterval<T>) obj;
        if (!end.equals(other.end))
            return false;
        if (openOnLeft != other.openOnLeft || openOnRight != other.openOnRight)
            return false;
        return start.equals(other.start);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        String output = "Interval: ";

        if(openOnLeft)
            output += "(";
        else
            output += "[";

        output += start + ", " + end;

        if(openOnRight)
            output += ")";
        else
            output += "]";

        return output;
    }

    public <R extends Comparable<R>> AbstractInterval<R> apply(Function<T, R> f)
    {
        return new AbstractInterval<>(f.apply(this.start),
                                      f.apply(this.end),
                                      this.openOnLeft,
                                      this.openOnRight);
    }
}
