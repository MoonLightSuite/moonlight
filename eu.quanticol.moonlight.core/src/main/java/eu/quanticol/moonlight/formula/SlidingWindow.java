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
package eu.quanticol.moonlight.formula;

import java.util.LinkedList;
import java.util.function.BiFunction;

import eu.quanticol.moonlight.signal.Sample;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalIterator;

/**
 *
 */
public class SlidingWindow<R> {

	private final double a;
	private final double size;
	private BiFunction<R, R, R> aggregator;

	public SlidingWindow(double a, double b, BiFunction<R, R, R> aggregator) {
		this.a = a;
		this.size = b-a;
		this.aggregator = aggregator;
	}	

	public Signal<R> apply(Signal<R> s) {
		Signal<R> result = new Signal<>();
		SignalIterator<R> iterator = s.getIterator();
		LinkedList<Sample<R>> window = new LinkedList<>();
		iterator.jump(s.start()+a);
		double windowEnd = 0.0;
		while (iterator.hasNext()) {
			Sample<R> next = iterator.next();
			while (!window.isEmpty()&&(next.getTime()>window.getFirst().getTime()+size)) {
				Sample<R> created = removeFirstAndAddToSignal( result, window);
				if (!window.isEmpty()) {
					Sample<R> second = window.getFirst();
					if (second.getTime()+size>=next.getTime()) {
						window.addFirst(new Sample<R>(next.getTime()-size, created.getValue()));
					}
				} else {
					window.addFirst(new Sample<R>(next.getTime()-size, created.getValue()));
				}
			}
			addElement(window,next.getTime(),next.getValue());
			windowEnd = next.getTime();
		}
		if ((window.size()>0)&&(window.getFirst().getTime()+size)<=windowEnd) {
			removeFirstAndAddToSignal( result, window);
		}
		result.complete();
		return result;
	}


	private Sample<R> removeFirstAndAddToSignal(Signal<R> result, LinkedList<Sample<R>> window) {
		Sample<R> first = window.removeFirst();
		result.add(first.getTime()-a, first.getValue());
		return first;
	}

	private void addElement(LinkedList<Sample<R>> window, double t,R v) {
		R currentValue = v;
		double currentTime = t;
		while (!window.isEmpty()) {
			Sample<R> sample = window.peekLast();
			R lastValue = sample.getValue();
			R newValue = this.aggregator.apply(lastValue,currentValue);
			if (newValue.equals(lastValue)) {
				window.add(new Sample<R>(currentTime,currentValue));
				return ;
			} else {
				currentValue = newValue;
				currentTime = sample.getTime();
				window.pollLast();
			}
		}
		window.add(new Sample<R>(currentTime,currentValue));
	}

	public double size() {
		return size;
	}


}
