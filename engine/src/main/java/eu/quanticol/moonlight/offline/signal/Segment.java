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

package eu.quanticol.moonlight.offline.signal;

import eu.quanticol.moonlight.online.signal.Sample;

/**
 * @author loreti
 *
 */
public class Segment<T> implements Sample<Double, T> {
	
	private double time;
	private final T value;
	private Segment<T> next;
	private Segment<T> previous;
	private double end = Double.NaN;
	
	public Segment( double time , T value ) {
		this.time = time;
		this.value = value;
		this.previous = null;
	}
	
	private Segment(Segment<T> previous, double time, T value) {
		this.previous = previous;
		this.time = time;
		this.value = value;
		this.end = time;
	}

	public Double getStart() {
		return time;
	}

	public T getValue() {
		return value;
	}
	
	public Segment<T> getNext() {
		return next;
	}
	
	public Segment<T> getPrevious() {
		return previous;
	}
	
	public void setNext( Segment<T> next ) {
		this.next = next;
	}

	public T getValueAt(double t) {
		Segment<T> selected = jump( t );
		return (selected==null?null:selected.value);
	}
	
	public Segment<T> jump(double t ) {
		if (t<time) {
			return backwardTo( this, t );
		} else {
			return forwardTo( this, t );
		}
	}
	
	public static <T> Segment<T> forwardTo(Segment<T> segment, double t) {
		Segment<T> cursor = segment;
		while (cursor != null) {
			if (cursor.contains(t)) {
				return cursor;
			} 
			cursor = cursor.next;
		}
		return null;	
	}

	public static <T> Segment<T> backwardTo(Segment<T> segment, double t) {
		Segment<T> cursor = segment;
		while (cursor != null) {
			if (cursor.contains(t)) {
				return cursor;
			} 
			cursor = cursor.previous;
		}
		return null;	
	}

	public boolean contains(double t) {
		return (time==t)||((time<=t)&&((Double.isFinite(end)&&(t<=end))||(next!=null)&&(t<next.time)));
	}

	public static <T> double getTime(Segment<T> s) {
		return (s == null ? Double.NaN : s.getStart());
	}

	public Segment<T> addAfter(double time, T value) {
		if (this.time >= time) {
			throw new IllegalArgumentException("Trying to add time: " + time +
											   ". Expected: >= " + this.time);
		}
		if (!this.value.equals(value)) {
			this.next = new Segment<>(this, time, value);
			this.end = Double.NaN;
			return this.next;
		} else {
			this.end = time;
			this.next = null;
			return this;
		}
	}

	public Segment<T> addBefore(double time, T value) {
		if (this.time<=time) {
			throw new IllegalArgumentException(); //TODO: Add error message!
		}
		if (!this.value.equals(value)) {
			this.previous = new Segment<>(this, time, value);
			this.previous.next = this;
			return this.previous;
		} else {
			if (isAPoint()) {
				this.end = this.time;
			}
			this.time = time;
			return this;
		}
	}

	public Double getEnd() {
		return getSegmentEnd();
	}

	public double getSegmentEnd() {
		if (next == null) {
			if (Double.isNaN(this.end)) {
				return this.time;
			}
			return this.end;
		}
		return next.getStart();
	}

	public double getPreviousTime() {
		if (previous == null) {
			return Double.NaN;
		}
		return previous.getStart();
	}
	
	@Override
	public String toString() {
		return (previous!=null?"<":"[")+time+":"+value+(next!=null?">":"]");
	}

	public void endAt(double end) {
		if (end<this.time) {
			throw new IllegalArgumentException(); //TODO: Add message!
		}
		this.end = end;
	}

	//TODO: this method mutates the Segment (very dangerous!)
	//		and doesn't do what it says it does!!!!!
	public Segment<T> splitAt(double time) {
		if (this.time>=time) {
			throw new IllegalArgumentException();
		}
		this.time = time;
		return this;
	}

	public boolean isTheEnd(double time) {
		return (this.end == time);
	}

	public boolean doEndAt(double t) {
		return (this.end==t);
	}

	public boolean isRightClosed() {
		return !Double.isNaN(this.end);
	}

	public double nextTimeAfter(double time) {
		if (this.next!=null) {
			return next.getStart();
		} else {
			if (time<end) {
				return end;
			} else {
				return Double.NaN;
			}
		}
	}

	public void setFirst() {
		this.previous = null;
	}

	public boolean isAPoint() {
		return (this.next==null)&&(Double.isNaN(this.end)||(this.time==this.end));
	}
	
}
