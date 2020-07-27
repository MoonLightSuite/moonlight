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

package eu.quanticol.moonlight.monitoring.temporal.online;

import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.formula.OnlineSlidingWindow;
import eu.quanticol.moonlight.formula.SlidingWindow;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.Signal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;

/**
 *
 * @param <T>
 * @param <R>
 */
public class OnlineMonitorFutureOperator<T, R>
        implements OnlineTemporalMonitor<T, R>
{
    private final TemporalMonitor<T, R> m;
    private final BinaryOperator<R> op;
    private final R min;
    private final R neutral;
    private final Interval interval;
    private final Interval horizon;
    private final List<Signal<R>> worklist;
    private double signalEnd;

    public OnlineMonitorFutureOperator(TemporalMonitor<T, R> m,
                                       BinaryOperator<R> op, R min, R neutral,
                                       Interval definitionInterval,
                                       Interval parentHorizon,
                                       double end)
    {
        this.m = m;
        this.op = op;
        this.min = min;
        this.neutral = neutral;
        this.interval = definitionInterval;
        horizon = parentHorizon;
        signalEnd = end;
        worklist = new ArrayList<>();
    }

    public OnlineMonitorFutureOperator(TemporalMonitor<T, R> m,
                                       BinaryOperator<R> op, R min, R neutral,
                                       Interval parentHorizon,
                                       double end)
    {
        this(m, op, min, neutral, null, parentHorizon, end);
    }

    @Override
    public Signal<R> monitor(Signal<T> signal) {
        //if(horizon.contains(signalEnd) || worklist.isEmpty()) {
            //update result
            worklist.add(computeSignal(m.monitor(signal), interval, op, min));
        //}

        signalEnd =  signal.getEnd();
        //System.out.println("FutureOperator Result Signal@maxT= " + signalEnd + " : " + worklist.get(worklist.size() - 1).toString());
        return worklist.get(worklist.size() - 1); //return last computed value
    }

    protected Signal<R> computeSignal(Signal<R> signal,
                                      Interval interval,
                                      BinaryOperator<R> op,
                                      R init)
    {
        if (interval ==  null || interval.isEmpty()) {
            throw new UnsupportedOperationException("Not Implemented Yet!");
            //return signal.iterateBackward(op, init);
        } else {
            SlidingWindow<R> sw = new OnlineSlidingWindow<>(interval.getStart(),
                                                            interval.getEnd(),
                                                            op, true,
                                                            neutral,
                                                            horizon.getEnd());
            return sw.apply(signal);
        }
    }

    /**
     * [Debugging method] Retrieved the list of monitored results
     */
    @Override
    public List<R> getWorklist() {
        List<R> lastValues = new ArrayList<>();
        for(Signal<R> item: worklist) {
            lastValues.add(item.valueAt(0));
        }

        return lastValues;
    }
}
