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

package io.github.moonlightsuite.moonlight.offline.algorithms;

import io.github.moonlightsuite.moonlight.core.formula.Interval;
import io.github.moonlightsuite.moonlight.core.signal.SignalDomain;
import io.github.moonlightsuite.moonlight.offline.signal.Signal;

import java.util.function.BinaryOperator;

/**
 * Algorithm for Unbounded Since Computation
 */
public class TemporalOp {

    private TemporalOp() {} // Hidden constructor

    public static <T> Signal<T> computeSince(SignalDomain<T> domain,
                                             Signal<T> s1, Interval interval,
                                             Signal<T> s2)
    {
        UnboundedOperator<T> operator = new UnboundedOperator<>(domain);

        Signal<T> unboundedMonitoring = operator.computeUnbounded(s1, s2, true);
        if (interval == null) {
            return unboundedMonitoring;
        }
        Signal<T> onceMonitoring = computePastSignal(s2, interval,
                                            domain::disjunction, domain.max());

        BooleanOp<T, T> booleanOp = new BooleanOp<>();

        return booleanOp.applyBinary(unboundedMonitoring, domain::conjunction,
                                                                onceMonitoring);
    }

    public static <T> Signal<T> computeUntil(SignalDomain<T> domain,
                                             Signal<T> s1,
                                             Interval interval,
                                             Signal<T> s2) {
        UnboundedOperator<T> op = new UnboundedOperator<>(domain);
        Signal<T> unboundedMonitoring = op.computeUnbounded(s1, s2, false);
        if (interval == null) {
            return unboundedMonitoring;
        }

        Signal<T> eventuallyMonitoring = computeFutureSignal(s2, interval,
                domain::disjunction, domain.min());

        BooleanOp<T, T> booleanOp = new BooleanOp<>();

        return booleanOp.applyBinary(unboundedMonitoring, domain::conjunction,
                                     eventuallyMonitoring);
    }

    public static <T> Signal<T> computePastSignal(Signal<T> signal,
                                                  Interval interval,
                                                  BinaryOperator<T> op, T init)
    {
        if (interval == null) {
            BooleanOp<T, T> booleanOp = new BooleanOp<>();
            return booleanOp.applyUnary(signal, x -> op.apply(x, init));
//            return signal.iterateForward(op , init);
        } else {
            SlidingWindow<T> sw = new SlidingWindow<>(interval.getStart(),
                                                      interval.getEnd(),
                                                      op, false);
            return sw.apply(signal);
        }
    }

    public static <T> Signal<T> computeFutureSignal(Signal<T> signal,
                                                    Interval interval,
                                                    BinaryOperator<T> op,
                                                    T init)
    {
        if (interval == null) {
            BooleanOp<T, T> booleanOp = new BooleanOp<>(false);
            return booleanOp.applyUnaryWithBound(signal, op, init);
        } else {
            SlidingWindow<T> sw = new SlidingWindow<>(interval.getStart(),
                                                      interval.getEnd(),
                                                      op, true);
            return sw.apply(signal);
        }
    }
}
