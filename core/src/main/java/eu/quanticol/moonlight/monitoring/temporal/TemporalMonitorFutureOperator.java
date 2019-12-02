/**
 * 
 */
package eu.quanticol.moonlight.monitoring.temporal;

import java.util.function.BiFunction;

import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.formula.SlidingWindow;
import eu.quanticol.moonlight.signal.Signal;

/**
 * @author loreti
 *
 */
public class TemporalMonitorFutureOperator<S, T> extends TemporalMonitor<S, T> {

	private TemporalMonitor<S, T> m;
	private BiFunction<T, T, T> op;
	private T min;
	private Interval interval;

	public TemporalMonitorFutureOperator(TemporalMonitor<S, T> m, BiFunction<T, T, T> op, T min, Interval interval) {
		this.m = m;
		this.op = op;
		this.min = min;
		this.interval = interval;
	}

	public TemporalMonitorFutureOperator(TemporalMonitor<S, T> m, BiFunction<T, T, T> op, T min) {
		this(m,op,min,null);
	}

	@Override
	public Signal<T> monitor(Signal<S> signal) {
		return computeSignal( m.monitor(signal) , interval , op , min);
	}

	public static <T> Signal<T> computeSignal( Signal<T> signal , Interval interval , BiFunction<T,T,T> op , T init ) {
		if (interval == null) {
			return signal.iterateBackward(op, init);
		} else {
			SlidingWindow<T> sw = new SlidingWindow<>(interval.getStart(), interval.getEnd(), op,true);
			return sw.apply(signal);
		}
	}
	
}
