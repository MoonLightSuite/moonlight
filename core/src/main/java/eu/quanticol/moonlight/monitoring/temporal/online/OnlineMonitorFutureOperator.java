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
 * Strategy to interpret online temporal operators on the future (except Until)
 *
 * @param <T> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see TemporalMonitor
 */
public class OnlineMonitorFutureOperator<T, R>
        implements TemporalMonitor<T, R>
{
    private final TemporalMonitor<T, R> m;
    private final BinaryOperator<R> op;
    private final R init;
    private final R unknown;
    private final Interval interval;
    private final Interval horizon;
    private final List<Signal<R>> worklist;
    private double signalEnd = 0;
    private SlidingWindow<R> sw;

    public OnlineMonitorFutureOperator(TemporalMonitor<T, R> m,
                                       BinaryOperator<R> op, R init, R unknown,
                                       Interval definitionInterval,
                                       Interval parentHorizon)
    {
        this.m = m;
        this.op = op;
        this.init = init;
        this.unknown = unknown;
        this.interval = definitionInterval;
        this.horizon = parentHorizon;
        this.worklist = new ArrayList<>();
    }

    @Override
    public Signal<R> monitor(Signal<T> signal) {
        //if(horizon.contains(signal.getEnd()) || signal.getEnd() > signalEnd) {
            //update result
            worklist.add(computeSignal(m.monitor(signal)));
        //}

        signalEnd =  signal.end();
        //System.out.println("FutureOperator Result Signal@maxT= " + signalEnd +
        //                " : " + worklist.get(worklist.size() - 1).toString());
        return worklist.get(worklist.size() - 1); //return last computed value
    }

    protected Signal<R> computeSignal(Signal<R> signal)
    {
        if (interval ==  null || interval.isEmpty()) {
            throw new UnsupportedOperationException("Not Implemented Yet!");
            //return signal.iterateBackward(op, init);
        } else {
            //TODO: sw should be loaded from state
            //if(sw == null) {
                sw = new OnlineSlidingWindow<>(interval.getStart(),
                        interval.getEnd(),
                        op, true,
                        unknown,
                        horizon.getEnd());
           // }
            return sw.apply(signal);
        }
    }
}
