/**
 * 
 */
package eu.quanticol.moonlight.monitoring.temporal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BiFunction;
import java.util.function.Function;

import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.io.SignalLoader;
import eu.quanticol.moonlight.io.SignalWriter;
import eu.quanticol.moonlight.signal.Signal;

/**
 * @author loreti
 *
 */
public abstract class TemporalMonitor<S,T> {
	
	private SignalLoader<S> loader;
	private SignalWriter<T> writer;
	
	public abstract Signal<T> monitor( Signal<S> signal ); 
	
	public void setLoader(SignalLoader<S> loader) {
		this.loader = loader;
	}

	public void setWriter(SignalWriter<T> writer) {
		this.writer = writer;
	}

	public Signal<T> monitor( InputStream input ) throws IOException {
		if (loader == null) {
			throw new IllegalStateException("Signal loader is not configured!");
		}
		return monitor( loader.load(input) );
	}
	
	public void monitor( InputStream input, OutputStream output ) throws IOException {
		if (writer == null) {
			throw new IllegalStateException("Signal writer is not configured!");
		}
		writer.write( monitor( loader.load(input) ) , output );
	}
	
	public static <S,T> TemporalMonitor<S,T> atomicMonitor( Function<S,T> atomic ) {
		return new TemporalMonitorAtomic<S,T>( atomic );
	}
	
	public static <S,T> TemporalMonitor<S,T> andMonitor( TemporalMonitor<S,T> m1, BiFunction<T,T,T> andOperator, TemporalMonitor<S,T> m2) {
		return new TemporalMonitorBinary<S,T>( m1 , andOperator , m2 );
	}
	
	public static <S,T> TemporalMonitor<S,T> orMonitor( TemporalMonitor<S,T> m1, BiFunction<T,T,T> orOperator, TemporalMonitor<S,T> m2) {
		return new TemporalMonitorBinary<S,T>( m1 , orOperator , m2 );
	}
	
	public static <S,T> TemporalMonitor<S,T> notMonitor( TemporalMonitor<S,T> m , Function<T,T> negation ) {
		return new TemporalMonitorUnary<S,T>( m , negation );
	}
	
	public static <S,T> TemporalMonitor<S,T> eventuallyMonitor( TemporalMonitor<S,T> m , BiFunction<T,T,T> op, T min ) {
		return new TemporalMonitorFutureOperator<S,T>( m , op, min );
	}

	public static <S,T> TemporalMonitor<S,T> eventuallyMonitor( TemporalMonitor<S,T> m , BiFunction<T,T,T> op, T min , Interval interval ) {
		return new TemporalMonitorFutureOperator<S,T>( m , op, min , interval );
	}

	public static <S,T> TemporalMonitor<S,T> globallyMonitor( TemporalMonitor<S,T> m , BiFunction<T,T,T> op, T max ) {
		return new TemporalMonitorFutureOperator<S,T>( m , op, max );
	}

	public static <S,T> TemporalMonitor<S,T> globallyMonitor( TemporalMonitor<S,T> m , BiFunction<T,T,T> op, T max , Interval interval ) {
		return new TemporalMonitorFutureOperator<S,T>( m , op, max , interval );
	}

	public static <S,T> TemporalMonitor<S,T> untilMonitor( TemporalMonitor<S,T> m1, TemporalMonitor<S,T> m2, SignalDomain<T> domain ) {
		return new TemporalMonitorUntil<S,T>(m1, m2, domain);
	}
	
	public static <S,T> TemporalMonitor<S,T> untilMonitor( TemporalMonitor<S,T> m1, Interval interval, TemporalMonitor<S,T> m2, SignalDomain<T> domain ) {
		return new TemporalMonitorUntil<S,T>(m1, interval, m2, domain);
	}
	
	public static <S,T> TemporalMonitor<S,T> hystoricallyMonitor( TemporalMonitor<S,T> m , BiFunction<T,T,T> op, T max ) {
		return new TemporalMonitorPastOperator<S,T>( m , op, max );
	}

	public static <S,T> TemporalMonitor<S,T> hystoricallyMonitor( TemporalMonitor<S,T> m , BiFunction<T,T,T> op, T max , Interval interval ) {
		return new TemporalMonitorPastOperator<S,T>( m , op, max , interval );
	}

	public static <S,T> TemporalMonitor<S,T> onceMonitor( TemporalMonitor<S,T> m , BiFunction<T,T,T> op, T min ) {
		return new TemporalMonitorPastOperator<S,T>( m , op, min );
	}

	public static <S,T> TemporalMonitor<S,T> onceMonitor( TemporalMonitor<S,T> m , BiFunction<T,T,T> op, T min , Interval interval ) {
		return new TemporalMonitorPastOperator<S,T>( m , op, min , interval );
	}

	public static <S,T> TemporalMonitor<S,T> sinceMonitor( TemporalMonitor<S,T> m1, TemporalMonitor<S,T> m2, SignalDomain<T> domain ) {
		return new TemporalMonitorSince<S,T>(m1, m2, domain);
	}
	
	public static <S,T> TemporalMonitor<S,T> sinceMonitor( TemporalMonitor<S,T> m1, Interval interval, TemporalMonitor<S,T> m2, SignalDomain<T> domain ) {
		return new TemporalMonitorSince<S,T>(m1, interval, m2, domain);
	}

	/*

	@Override
	public Function<Signal<T>, Signal<R>> visit(SinceFormula sinceFormula, Parameters parameters) {
		Function<Signal<T>,Signal<R>> firstMonitoring = sinceFormula.getFirstArgument().accept(this, parameters);
		Function<Signal<T>,Signal<R>> secondMonitoring = sinceFormula.getSecondArgument().accept(this, parameters);
		Function<Signal<T>,Signal<R>> unboundedSinceMonitoring = s -> TemporalMonitoring.unboundedSinceMonitoring( firstMonitoring.apply(s) , secondMonitoring.apply(s) , module);
		if (sinceFormula.isUnbounded()) {
			return unboundedSinceMonitoring;
		} else {
			return s -> boundedSinceMonitoring(firstMonitoring.apply(s), sinceFormula.getInterval(), secondMonitoring.apply(s), module);
//			return s -> Signal.apply(unboundedSinceMonitoring.apply(s), module::conjunction, TemporalMonitoring.temporalMonitoring(secondMonitoring.apply(s), module::disjunction, sinceFormula.getInterval(), true));
		}
	}



	public static <R> Signal<R>  unboundedSinceMonitoring(Signal<R> s1, Signal<R> s2, SignalDomain<R> module) {
		Signal<R> result = new Signal<R>();
		SignalCursor<R> c1 = s1.getIterator(true);
		SignalCursor<R> c2 = s2.getIterator(true);
		double start = Math.max( c1.time() , c2.time() );
		double end = Math.min(s1.end(), s2.end());
		double time = start;
		R current = module.min();
		c1.move(time);
		c2.move(time);
		while (!c1.completed()&&!c2.completed()) {
			result.add(time, module.disjunction(c2.value(), module.conjunction(c1.value(), current)));
			time = Math.min(c1.nextTime(), c2.nextTime());
			c1.move(time);
			c2.move(time);
		} 
		result.endAt(end);
		return result;
	}

	public static <R> Signal<R>  boundedSinceMonitoring(Signal<R> s1, Interval i, Signal<R> s2, SignalDomain<R> module) {
		Signal<R> unboundedResult = unboundedSinceMonitoring(s1, s2, module);
		Signal<R> onceMonitoring = TemporalMonitoring.temporalMonitoring(s2, module::disjunction, i, false);
		return Signal.apply(unboundedResult, module::conjunction, onceMonitoring);
	}

	

	public static <R> Signal<R> temporalMonitoring( Signal<R> signal , BiFunction<R, R, R> aggregator , Interval i , boolean future) {
		SlidingWindow<R> sw = new SlidingWindow<>(i.getStart(), i.getEnd(), aggregator,future);
		return sw.apply(signal);
	}

	public Function<Signal<T>, Signal<R>> monitor(Formula f, Parameters parameters) {
		return f.accept(this, parameters);
	}

	@Override
	public Function<Signal<T>, Signal<R>> visit(SinceFormula sinceFormula, Parameters parameters) {
		Function<Signal<T>,Signal<R>> firstMonitoring = sinceFormula.getFirstArgument().accept(this, parameters);
		Function<Signal<T>,Signal<R>> secondMonitoring = sinceFormula.getSecondArgument().accept(this, parameters);
		Function<Signal<T>,Signal<R>> unboundedSinceMonitoring = s -> TemporalMonitoring.unboundedSinceMonitoring( firstMonitoring.apply(s) , secondMonitoring.apply(s) , module);
		if (sinceFormula.isUnbounded()) {
			return unboundedSinceMonitoring;
		} else {
			return s -> boundedSinceMonitoring(firstMonitoring.apply(s), sinceFormula.getInterval(), secondMonitoring.apply(s), module);
//			return s -> Signal.apply(unboundedSinceMonitoring.apply(s), module::conjunction, TemporalMonitoring.temporalMonitoring(secondMonitoring.apply(s), module::disjunction, sinceFormula.getInterval(), true));
		}
	}


	 */
	
	
}
