/**
 * 
 */
package eu.quanticol.moonlight.monitoring.temporal;

import java.util.function.Function;

import eu.quanticol.moonlight.signal.Signal;

/**
 * @author loreti
 *
 */
public class TemporalMonitorUnary<S, T> implements TemporalMonitor<S, T> {

	private TemporalMonitor<S, T> m;
	private Function<T, T> op;

	public TemporalMonitorUnary(TemporalMonitor<S, T> m, Function<T, T> op) {
		this.m = m;
		this.op = op;
	}

	@Override
	public Signal<T> monitor(Signal<S> signal) {
		return this.m.monitor(signal).apply(op);
	}
	
	

}
