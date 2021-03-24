/**
 * 
 */
package eu.quanticol.moonlight.signal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author loreti
 *
 */
public class ParallelSignalCursor<T> {

	private ArrayList<SignalCursor<T>> cursors;
	
	public ParallelSignalCursor( int size , Function<Integer,SignalCursor<T>>  f ) {
		this.cursors = new ArrayList<SignalCursor<T>>(size);
		for( int i=0 ; i<size ; i++ ) {
			this.cursors.add(f.apply(i));
		}
	}

	public double nextTime() {
		double time = Double.POSITIVE_INFINITY;
		for (SignalCursor<T> c : cursors) {
			double cursorTime = c.nextTime();
			if (Double.isNaN(cursorTime)) {
				return Double.NaN;
			}
			if (cursorTime<time) {
				time = cursorTime;
			}
		}
		return time;
	}

	public double previousTime() {
		double time = Double.NEGATIVE_INFINITY;
		for (SignalCursor<T> c : cursors) {
			double cursorTime = c.nextTime();
			if (Double.isNaN(cursorTime)) {
				return Double.NaN;
			}
			if (time<cursorTime) {
				time = cursorTime;
			}
		}
		return time;
	}
	
	public double forward() {
		double time = nextTime();
		if (!Double.isNaN(time)) {
			move( time );
		}
		return time;
	}
	
	public double backward() {
		double time = previousTime();
		if (!Double.isNaN(time)) {
			move( time );
		}
		return time;
	}
	
	public void move(double time) {
		for (SignalCursor<T> c : cursors) {
			c.move(time);
		}		
	}

	public boolean hasNext( ) {
		for (SignalCursor<T> c : cursors) {
			if (!c.hasNext()) {
				return false;
			}
		}
		return true;
	}
	
	public boolean hasPrevious() {
		for (SignalCursor<T> c : cursors) {
			if (!c.hasPrevious()) {
				return false;
			}
		}
		return true;
	}
	
	public Function<Integer,T> getValue() {
		ArrayList<T> values = new ArrayList<>(
				cursors.stream().map(c -> c.value()).collect(Collectors.toList()));
		return (i -> values.get(i));
	}
	
	public double getTime() {
		if( cursors.isEmpty() ) {
			return Double.NaN;
		}
		double time = cursors.get(0).time();
		for (SignalCursor<T> c : cursors) {
			if (c.time() != time) {
				return Double.NaN;
			}
		}
		return time;
	}

	public boolean areSynchronized() {
		return !Double.isNaN(getTime());
	}

	public double syncCursors() {
		double time = cursors.get(0).time();
		boolean flag = false;
		for (SignalCursor<T> c : cursors) {
			if (time != c.time()) {
				flag = true;
			}
			time = Math.max(time, c.time());
		}
		if (flag) {
			move(time);
		}
		return time;
	}

	public boolean completed() {
		for (SignalCursor<T> signalCursor : cursors) {
			if (signalCursor.completed()) {
				return true;
			}
		}
		return false;
	}
}
