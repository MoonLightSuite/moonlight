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
public class TemporalMonitorPastOperator<S, T> implements TemporalMonitor<S, T> {

	private TemporalMonitor<S, T> m;
	private BiFunction<T, T, T> op;
	private T init;
	private Interval interval;

	public TemporalMonitorPastOperator(TemporalMonitor<S, T> m, BiFunction<T, T, T> op, T init, Interval interval) {
		this.m = m;
		this.op = op;
		this.init = init;
		this.interval = interval;
	}

	public TemporalMonitorPastOperator(TemporalMonitor<S, T> m, BiFunction<T, T, T> op, T min) {
		this(m,op,min,null);
	}

	@Override
	public Signal<T> monitor(Signal<S> signal) {
		return computeSignal( m.monitor(signal) , interval , op, init );
	}

	public static <T> Signal<T> computeSignal( Signal<T> signal , Interval interval , BiFunction<T,T,T> op , T init ) {
		if (interval == null) {
			return signal.iterateForward( op , init );
		} else {
			SlidingWindow<T> sw = new SlidingWindow<>(interval.getStart(), interval.getEnd(), op,false);
			return sw.apply(signal);
		}
	}
	
}
