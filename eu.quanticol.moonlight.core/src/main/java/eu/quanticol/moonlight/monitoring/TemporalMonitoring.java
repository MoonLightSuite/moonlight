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
import eu.quanticol.moonlight.signal.Sample;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SignalCursor;

/**
 *
 */
public class TemporalMonitoring<T,R> implements 
		FormulaVisitor<Parameters, Function<Signal<T>,Signal<R>>> {
	
	private final HashMap<String,Function<Parameters,Function<T,R>>> atomicPropositions;
	private final DomainModule<R> module;
	
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
		Interval interval = eventuallyFormula.getInterval();
		Function<Signal<T>,Signal<R>> argumentMonitoring = eventuallyFormula.getArgument().accept(this, parameters);
		return s -> TemporalMonitoring.temporalMonitoring(argumentMonitoring.apply(s), module::disjunction, interval,true);
	}

	@Override
	public Function<Signal<T>, Signal<R>> visit(GloballyFormula globallyFormula, Parameters parameters) {
		Interval interval = globallyFormula.getInterval();
		Function<Signal<T>,Signal<R>> argumentMonitoring = globallyFormula.getArgument().accept(this, parameters);
		return s -> TemporalMonitoring.temporalMonitoring(argumentMonitoring.apply(s), module::conjunction, interval,true);
	}

	@Override
	public Function<Signal<T>, Signal<R>> visit(UntilFormula untilFormula, Parameters parameters) {
		// TODO Auto-generated method stub
		return null;
	}


	public static <R> Signal<R> temporalMonitoring( Signal<R> signal , BiFunction<R, R, R> aggregator , Interval i , boolean future) {
		SlidingWindow<R> sw = new SlidingWindow<>(i.getStart(), i.getEnd(), aggregator);
		return sw.apply(signal,future);
	}

	public Function<Signal<T>, Signal<R>> monitor(Formula f, Parameters parameters) {
		return f.accept(this, parameters);
	}

	@Override
	public Function<Signal<T>, Signal<R>> visit(SinceFormula sinceFormula, Parameters parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Function<Signal<T>, Signal<R>> visit(HystoricallyFormula hystoricallyFormula, Parameters parameters) {
		Function<Signal<T>,Signal<R>> argumentMonitoring = hystoricallyFormula.getArgument().accept(this, parameters);
		if (hystoricallyFormula.isUnbounded()) {
			return s -> TemporalMonitoring.pastMonitoring( argumentMonitoring.apply(s) , module::disjunction ); 
		} else {
			Interval interval = hystoricallyFormula.getInterval();
			return s -> TemporalMonitoring.temporalMonitoring(argumentMonitoring.apply(s), module::disjunction, interval,false);
		}
	}

	private static <R> Signal<R>  pastMonitoring(Signal<R> s, BiFunction<R, R, R> aggregator ) {
		Signal<R> result = new Signal<R>();
//		SignalCursor<R> iterator = s.getIterator();
//		R current = null;
//		while (iterator.hasNext()) {
//			Sample<R> next = iterator.next();
//			current = (current==null?next.getValue():aggregator.apply(current,next.getValue()));
//			result.add(next.getTime(), current);
//		}
		return result;
	}

	@Override
	public Function<Signal<T>, Signal<R>> visit(OnceFormula onceFormula, Parameters parameters) {
		Function<Signal<T>,Signal<R>> argumentMonitoring = onceFormula.getArgument().accept(this, parameters);
		if (onceFormula.isUnbounded()) {
			return s -> TemporalMonitoring.pastMonitoring( argumentMonitoring.apply(s) , module::conjunction ); 
		} else {
			Interval interval = onceFormula.getInterval();
			return s -> TemporalMonitoring.temporalMonitoring(argumentMonitoring.apply(s), module::conjunction, interval,false);
		}
	}

	

}
