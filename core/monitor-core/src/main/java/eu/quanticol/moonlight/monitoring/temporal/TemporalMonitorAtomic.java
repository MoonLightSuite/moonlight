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
public class TemporalMonitorAtomic<S, T> extends TemporalMonitor<S, T> {

	private final Function<S, T> atomic;

	public TemporalMonitorAtomic(Function<S, T> atomic) {
		this.atomic = atomic;
	}

	@Override
	public Signal<T> monitor(Signal<S> signal) {
		return signal.apply(atomic);
	}

}
