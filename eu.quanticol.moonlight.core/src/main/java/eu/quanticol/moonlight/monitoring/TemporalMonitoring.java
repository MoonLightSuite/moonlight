/**
 * 
 */
package eu.quanticol.moonlight.monitoring;

import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import eu.quanticol.moonlight.formula.AndFormula;
import eu.quanticol.moonlight.formula.AtomicFormula;
import eu.quanticol.moonlight.formula.DomainModule;
import eu.quanticol.moonlight.formula.EventuallyFormula;
import eu.quanticol.moonlight.formula.Formula;
import eu.quanticol.moonlight.formula.FormulaVisitor;
import eu.quanticol.moonlight.formula.GloballyFormula;
import eu.quanticol.moonlight.formula.HystoricallyFormula;
import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.formula.NegationFormula;
import eu.quanticol.moonlight.formula.OnceFormula;
import eu.quanticol.moonlight.formula.OrFormula;
import eu.quanticol.moonlight.formula.Parameters;
import eu.quanticol.moonlight.formula.SinceFormula;
import eu.quanticol.moonlight.formula.SlidingWindow;
import eu.quanticol.moonlight.formula.UntilFormula;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCursor;

/**
 *
 */
public class TemporalMonitoring<T,R> implements 
		FormulaVisitor<Parameters, Function<Signal<T>,Signal<R>>> {
	
	private final HashMap<String,Function<Parameters,Function<T,R>>> atomicPropositions;
	private final DomainModule<R> module;
	
	public TemporalMonitoring( DomainModule<R> module ) {
		this( new HashMap<>(), module );
	}
	
	public TemporalMonitoring( HashMap<String,Function<Parameters,Function<T,R>>> atomicPropositions  , DomainModule<R> module ) {
		this.atomicPropositions = atomicPropositions;
		this.module = module;
	}

	@Override
	public Function<Signal<T>, Signal<R>> visit(AtomicFormula atomicFormula, Parameters parameters) {
		Function<Parameters,Function<T,R>> f = atomicPropositions.get(atomicFormula.getAtomicId());
		if (f == null) {
			throw new IllegalArgumentException("Unkown atomic ID "+atomicFormula.getAtomicId());
		}
		Function<T,R> atomic = f.apply(parameters);
		return s -> s.apply(atomic);
	}

	@Override
	public Function<Signal<T>, Signal<R>> visit(AndFormula andFormula, Parameters parameters) {
		Function<Signal<T>,Signal<R>> leftMonitoring = andFormula.getFirstArgument().accept(this, parameters);
		Function<Signal<T>,Signal<R>> rightMonitoring = andFormula.getSecondArgument().accept(this, parameters);
		return s -> Signal.apply(leftMonitoring.apply(s), module::conjunction, rightMonitoring.apply(s));
	}

	@Override
	public Function<Signal<T>, Signal<R>> visit(NegationFormula negationFormula, Parameters parameters) {
		Function<Signal<T>,Signal<R>> argumentMonitoring = negationFormula.getArgument().accept(this, parameters);
		return s -> argumentMonitoring.apply(s).apply(module::negation);
	}

	@Override
	public Function<Signal<T>, Signal<R>> visit(OrFormula orFormula, Parameters parameters) {
		Function<Signal<T>,Signal<R>> leftMonitoring = orFormula.getFirstArgument().accept(this, parameters);
		Function<Signal<T>,Signal<R>> rightMonitoring = orFormula.getSecondArgument().accept(this, parameters);
		return s -> Signal.apply(leftMonitoring.apply(s), module::disjunction, rightMonitoring.apply(s));
	}

	@Override
	public Function<Signal<T>, Signal<R>> visit(EventuallyFormula eventuallyFormula, Parameters parameters) {
		Function<Signal<T>,Signal<R>> argumentMonitoring = eventuallyFormula.getArgument().accept(this, parameters);
		if (eventuallyFormula.isUnbounded()) {
			return s -> argumentMonitoring.apply(s).iterateBackward( module::disjunction , module.min() ); 
		} else {
			Interval interval = eventuallyFormula.getInterval();
			return s -> TemporalMonitoring.temporalMonitoring(argumentMonitoring.apply(s), module::disjunction, interval,true);
		}
	}

	@Override
	public Function<Signal<T>, Signal<R>> visit(GloballyFormula globallyFormula, Parameters parameters) {
		Function<Signal<T>,Signal<R>> argumentMonitoring = globallyFormula.getArgument().accept(this, parameters);
		if (globallyFormula.isUnbounded()) {
			return s -> argumentMonitoring.apply(s).iterateBackward( module::conjunction , module.max() ); 
		} else {
			Interval interval = globallyFormula.getInterval();
			return s -> TemporalMonitoring.temporalMonitoring(argumentMonitoring.apply(s), module::conjunction, interval,true);
		}
	}

	@Override
	public Function<Signal<T>, Signal<R>> visit(UntilFormula untilFormula, Parameters parameters) {
		Function<Signal<T>,Signal<R>> firstMonitoring = untilFormula.getFirstArgument().accept(this, parameters);
		Function<Signal<T>,Signal<R>> secondMonitoring = untilFormula.getSecondArgument().accept(this, parameters);
		Function<Signal<T>,Signal<R>> unboundedMonitoring = s -> TemporalMonitoring.unboundedUntilMonitoring( firstMonitoring.apply(s) , secondMonitoring.apply(s) , module);
		if (untilFormula.isUnbounded()) {
			return unboundedMonitoring;
		} else {
//			return s -> Signal.apply(unboundedMonitoring.apply(s), module::conjunction, TemporalMonitoring.temporalMonitoring(secondMonitoring.apply(s), module::disjunction, untilFormula.getInterval(), true));
			return s -> boundedSinceMonitoring(firstMonitoring.apply(s), untilFormula.getInterval(), secondMonitoring.apply(s), module);
		}
	}


	public static <R> Signal<R>  unboundedUntilMonitoring(Signal<R> s1, Signal<R> s2, DomainModule<R> module) {
		Signal<R> result = new Signal<R>();
		SignalCursor<R> c1 = s1.getIterator(false);
		SignalCursor<R> c2 = s2.getIterator(false);
		double end = Math.min( c1.time() , c2.time() );
		double time = end;
		R current = module.min();
		c1.move(time);
		c2.move(time);
		while (!c1.completed()&&!c2.completed()) {
			result.add(time, module.disjunction(c2.value(), module.conjunction(c1.value(), current)));
			time = Math.max(c1.previousTime(), c2.previousTime());
			c1.move(time);
			c2.move(time);
		} 
		result.endAt(end);
		return result;
	}

	public static <R> Signal<R>  boundedUntilMonitoring(Signal<R> s1, Interval i, Signal<R> s2, DomainModule<R> module) {
		Signal<R> unboundedMonitoring = unboundedUntilMonitoring(s1, s2, module);
		Signal<R> eventuallyMonitoring = TemporalMonitoring.temporalMonitoring(s2, module::disjunction, i, true);
		return Signal.apply(unboundedMonitoring,module::conjunction,eventuallyMonitoring);
	}


	public static <R> Signal<R>  unboundedSinceMonitoring(Signal<R> s1, Signal<R> s2, DomainModule<R> module) {
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

	public static <R> Signal<R>  boundedSinceMonitoring(Signal<R> s1, Interval i, Signal<R> s2, DomainModule<R> module) {
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

	@Override
	public Function<Signal<T>, Signal<R>> visit(HystoricallyFormula hystoricallyFormula, Parameters parameters) {
		Function<Signal<T>,Signal<R>> argumentMonitoring = hystoricallyFormula.getArgument().accept(this, parameters);
		if (hystoricallyFormula.isUnbounded()) {
			return s -> argumentMonitoring.apply(s).iterateForward( module::conjunction , module.min() ); 
		} else {
			Interval interval = hystoricallyFormula.getInterval();
			return s -> TemporalMonitoring.temporalMonitoring(argumentMonitoring.apply(s), module::conjunction, interval,false);
		}
	}

	@Override
	public Function<Signal<T>, Signal<R>> visit(OnceFormula onceFormula, Parameters parameters) {
		Function<Signal<T>,Signal<R>> argumentMonitoring = onceFormula.getArgument().accept(this, parameters);
		if (onceFormula.isUnbounded()) {
			return s -> argumentMonitoring.apply(s).iterateForward( module::disjunction , module.min() ); 
		} else {
			Interval interval = onceFormula.getInterval();
			return s -> TemporalMonitoring.temporalMonitoring(argumentMonitoring.apply(s), module::disjunction, interval,false);
		}
	}

	public void addProperty(String name, Function<Parameters,Function<T,R>> atomic ) {
		atomicPropositions.put(name, atomic);
	}

	

}
