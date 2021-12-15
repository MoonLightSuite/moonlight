/*******************************************************************************
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
 *******************************************************************************/
package eu.quanticol.moonlight.signal;

import eu.quanticol.moonlight.util.Pair;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 */
public class Signal<T> {

    private Segment<T> first;
    private Segment<T> last;
    private int size = 0;


    private double end = 0.0;

    public Signal() {
        this.first = null;
        this.last = null;
        this.size = 0;
        this.end = Double.NaN;
    }

    public double getEnd() {
        return end;
    }

    /**
     * @return the start time of the signal
     */
    public double start() {
        return Segment.getTime(first);
    }

    /**
     * @return the end time of the signal
     */
    public double end() {
        return end;
    }

    /**
     * @returns true if the signal is empty
     */
    public boolean isEmpty() {
        return (size == 0);
    }

    /**
     * Add (t,value) to the sample set
     *
     * @param t
     * @param value
     * @return last time step
     */
    public void add(double t, T value) {
        Segment<T> oldLast = last;
        if (first == null) {
            startWith(t, value);
        } else {
            if (this.end > t) {
                throw new IllegalArgumentException("Time: " + t + " Expected: >" + this.end);//TODO: Add Message!
            }
            last = last.addAfter(t, value);
            end = t;
        }
        if (oldLast != last) {
            size++;
        }
    }

    private void startWith(double t, T value) {
        first = new Segment<>(t, value);
        last = first;
        end = t;
    }


    /**
     * Add (t,value) to the sample set
     *
     * @param t
     * @param value
     * @return last time step
     */
    public void addBefore(double t, T value) {
        Segment<T> oldFirst = first;
        if (first == null) {
            startWith(t, value);
        } else {
            first = first.addBefore(t, value);
        }
        if (first != oldFirst) {
            size++;
        }
    }

    /**
     * return a signal, given a signal and a function
     */
    public <R> Signal<R> apply(Function<T, R> f) {
        Signal<R> newSignal = new Signal<R>();
        SignalCursor<T> cursor = getIterator(true);
        while (!cursor.completed()) {
            newSignal.add(cursor.time(), f.apply(cursor.value()));
            cursor.forward();
        }
        newSignal.endAt(end);
        return newSignal;
    }

    /**
     *
     */
    public static <T, R> Signal<R> apply(Signal<T> s, Function<T, R> f) {
        return s.apply(f);
    }

    /**
     * return a signal, given two signals and a bifunction
     */
    public static <T, R> Signal<R> apply(Signal<T> s1, BiFunction<T, T, R> f, Signal<T> s2) {
        Signal<R> newSignal = new Signal<>();
        if (!s1.isEmpty() && !s2.isEmpty()) {
            SignalCursor<T> c1 = s1.getIterator(true);
            SignalCursor<T> c2 = s2.getIterator(true);
            double time = Math.max(s1.start(), s2.start());
            c1.move(time);
            c2.move(time);
            while (!c1.completed() && !c2.completed()) {
                newSignal.add(time, f.apply(c1.value(), c2.value()));
                time = Math.min(c1.nextTime(), c2.nextTime());
                c1.move(time);
                c2.move(time);
            }
            if (!newSignal.isEmpty()) {
                newSignal.endAt(Math.min(s1.end, s2.end));
            }
        }
        return newSignal;
    }

    /**
     * @param f
     * @param init
     * @return
     */
    //TODO: Add comments!
    public <R> Signal<R> iterateForward(BiFunction<T, R, R> f, R init) {
        Signal<R> newSignal = new Signal<>();
        SignalCursor<T> cursor = getIterator(true);
        R value = init;
        while (!cursor.completed()) {
            value = f.apply(cursor.value(), value);
            newSignal.add(cursor.time(), value);
            cursor.forward();
        }
        newSignal.end = end;
        return newSignal;
    }


    /**
     * @param f
     * @param init
     * @return
     */
    //TODO: Add comments!
    public <R> Signal<R> iterateBackward(BiFunction<T, R, R> f, R init) {
        Signal<R> newSignal = new Signal<>();
        SignalCursor<T> cursor = getIterator(false);
        R value = init;
        while (!cursor.completed()) {
            T sValue = cursor.value();
            double t = cursor.time();
            value = f.apply(sValue, value);
            newSignal.addBefore(t, value);
            cursor.backward();
        }
        newSignal.end = end;
        return newSignal;
    }

    public void forEach(BiConsumer<Double,T> consumer) {
        SignalCursor<T> cursor = getIterator(true);
        while (!cursor.completed()) {
            consumer.accept(cursor.time(),cursor.value());
            cursor.forward();
        }
    }

    public <R> R reduce(BiFunction<Pair<Double,T>,R,R> reducer, R init) {
        R toReturn = init;
        SignalCursor<T> cursor = getIterator(true);
        while (!cursor.completed()) {
            toReturn = reducer.apply(new Pair<>(cursor.time(),cursor.value()),toReturn);
            cursor.forward();
        }
        return toReturn;
    }

    public <R> void fill( double[] timePoints, R[] data, Function<T,R> f) {
        if (size == 0) {
            throw new IllegalStateException("No array can be generated from an empty signal is empty!");
        }
        Segment<T> current = first;
        for (int i = 0; i < timePoints.length; i++) {
            current = current.jump(timePoints[i]);
            data[i] = f.apply(current.getValue());
        }
    }

    /**
     * @return
     */
    public SignalCursor<T> getIterator(boolean forward) {
        return new SignalCursor<T>() {

            private Segment<T> current = (forward ? first : last);
            private double time = (current != null ? (forward ? current.getTime() : current.getSegmentEnd()) : Double.NaN);

            @Override
            public double time() {
                return time;
            }

            @Override
            public T value() {
                return (current != null ? current.getValue() : null);
            }

            @Override
            public void forward() {
                if (current != null) {
                    if ((!current.isRightClosed()) || (current.doEndAt(time))) {
                        current = current.getNext();
                        time = (current != null ? current.getTime() : Double.NaN);
                    } else {
                        time = current.getSegmentEnd();
                    }
                }
            }

            @Override
            public void backward() {
                if (current != null) {
                    if (time>current.getTime()) {
                        time = current.getTime();
                    } else {
                        current = current.getPrevious();
                        time = (current != null ? current.getTime() : Double.NaN);
                    }
                }
            }

            @Override
            public void move(double t) {
                if (current != null) {
                    current = current.jump(t);
                    time = t;
                }
            }

            @Override
            public double nextTime() {
                if (current != null) {
                    return current.nextTimeAfter(time);
                }
                return Double.NaN;
            }

            @Override
            public double previousTime() {
                if (current != null) {
                    if (current.getTime() < time) {
                        return current.getTime();
                    } else {
                        return current.getPreviousTime();
                    }
                }
                return Double.NaN;
            }

            @Override
            public boolean hasNext() {
                return (current != null) && (current.getNext() != null);
            }

            @Override
            public boolean hasPrevious() {
                return (current != null) && (current.getPrevious() != null);
            }

            @Override
            public boolean completed() {
                return (current == null);//||(current.isTheEnd(time)));
                //return ((current == null));//||(current.isTheEnd(time)));
            }

            @Override
            public String toString() {
                return Signal.this.toString() + (current == null ? "!" : ("@(" + current.getTime() + ")"));
            }

        };
    }

    public int size() {
        return size;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "Signal [ ]";
        } else {
            return "Signal [start=" + start() + ", end=" + end() + ", size=" + size() + "]";
        }
    }

    public void endAt(double end) {
        if ((this.end > end) || (last == null)) {
            throw new IllegalArgumentException();//TODO: Add message!
        }
        this.end = end;
        this.last.endAt(end);
    }


    public T valueAt(double t) {
        return (first == null ? null : first.getValueAt(t));
    }

    public double[][] arrayOf(FunctionToDouble<T> f) {
        if (size == 0) {
            return new double[][] {};
            //throw new IllegalStateException("No array can be generated from an empty signal is empty!");
        }
        int arraySize = size;
        if (!last.isAPoint()) {
            arraySize++;
        }
        double[][] toReturn = new double[arraySize][2];
        Segment<T> current = first;
        int counter = 0;
        while (current != null) {
            toReturn[counter][0] = current.getTime();
            toReturn[counter][1] = f.apply( current.getValue() );
            current = current.getNext();
            counter++;
        }
        if (!last.isAPoint()) {
            toReturn[size][0] = end;
            toReturn[size][1] = f.apply( last.getValue() );
        }
        return toReturn;
    }

    public double[][] arrayOf(double[] timePoints, FunctionToDouble<T> f) {
        if (size == 0) {
            return new double[][] {};
            //throw new IllegalStateException("No array can be generated from an empty signal is empty!");
        }
        double[][] toReturn = new double[timePoints.length][2];
        Segment<T> current = first;
        double value = Double.NaN;
        for (int i = 0; i < timePoints.length; i++) {
            if (current != null) {
                current = current.jump(timePoints[i]);
            }
            if (current != null) {
                value = f.apply(current.getValue());
            }
            toReturn[i][0] = timePoints[i];
            toReturn[i][1] = value;
        }
        return toReturn;
    }



    public Set<Double> getTimeSet() {
        HashSet<Double> timeSet = new HashSet<>();
        Segment<T> current = first;
        while (current != null) {
            timeSet.add(current.getTime());
            current = current.getNext();
        }
        timeSet.add(end);
        return timeSet;
    }

}
