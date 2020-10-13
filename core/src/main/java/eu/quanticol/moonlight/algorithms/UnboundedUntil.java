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

package eu.quanticol.moonlight.algorithms;

import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCursor;

/**
 * Algorithm for Unbounded Since Computation
 */
public class UnboundedUntil {

    private UnboundedUntil() {} // Hidden constructor

    public static <T> Signal<T> compute(SignalDomain<T> domain,
                                        Signal<T> s1,
                                        Signal<T> s2)
    {
        Signal<T> result = new Signal<>();
        SignalCursor<T> c1 = s1.getIterator(false);
        SignalCursor<T> c2 = s2.getIterator(false);
        double t1 = c1.time();
        double t2 = c2.time();
        double end = Math.min(t1 , t2);
        double time = end;
        T current = domain.min();
        c1.move(time);
        c2.move(time);
        while (!c1.completed() && !c2.completed()) {
            //TODO: instead of c2.value(),
            // 		should be c2.value() AND domain.conjunction(c1.value())
            //		in order to comply with paper's until definition
            current = domain.disjunction(c2.value(),
                    domain.conjunction(c1.value(), current));
            result.addBefore(time, current);
            time = Math.max(c1.previousTime(), c2.previousTime());
            c1.move(time);
            c2.move(time);
        }
        result.endAt(end);
        System.out.println("FutureOperator Result Signal@maxT= " +
                "<" + s1.end() + "," + s2.end() + "> : " +
                result.toString());
        return result;
    }
}
