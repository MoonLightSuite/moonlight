/**
 * 
 */
package eu.quanticol.moonlight.monitoring.temporal;

import java.util.function.BiFunction;

import eu.quanticol.moonlight.signal.Signal;

/**
 * @author loreti
 *
 */
public class TemporalMonitorBinary<S, T> implements TemporalMonitor<S, T> {

	private final TemporalMonitor<S, T> m1;
	private final BiFunction<T, T, T> op;
	private final TemporalMonitor<S, T> m2;

	public TemporalMonitorBinary(TemporalMonitor<S, T> m1, BiFunction<T, T, T> op, TemporalMonitor<S, T> m2) {
		this.m1 = m1;
		this.op = op;
		this.m2 = m2;
	}

	@Override
	public Signal<T> monitor(Signal<S> signal) {
		return Signal.apply(m1.monitor(signal), op, m2.monitor(signal));
	}

}
