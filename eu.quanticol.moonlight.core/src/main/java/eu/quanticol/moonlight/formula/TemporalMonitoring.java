/**
 * 
 */
package eu.quanticol.moonlight.formula;

import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import eu.quanticol.moonlight.signal.Signal;

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
		Function<Signal<T>,Signal<R>> argumentMonitoring = negationFormula.accept(this, parameters);
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
		Interval interval = eventuallyFormula.getInterval(parameters);
		Function<Signal<T>,Signal<R>> argumentMonitoring = eventuallyFormula.accept(this, parameters);
		return s -> TemporalMonitoring.temporalMonitoring(argumentMonitoring.apply(s), module::disjunction, interval);
	}

	@Override
	public Function<Signal<T>, Signal<R>> visit(GloballyFormula globallyFormula, Parameters parameters) {
		Interval interval = globallyFormula.getInterval(parameters);
		Function<Signal<T>,Signal<R>> argumentMonitoring = globallyFormula.accept(this, parameters);
		return s -> TemporalMonitoring.temporalMonitoring(argumentMonitoring.apply(s), module::disjunction, interval);
	}

	@Override
	public Function<Signal<T>, Signal<R>> visit(UntilFormula untilFormula, Parameters parameters) {
		// TODO Auto-generated method stub
		return null;
	}


	public static <R,T> Signal<R> temporalMonitoring( Signal<R> signal , BiFunction<R, R, R> aggregator , Interval i ) {
		SlidingWindow<R> sw = new SlidingWindow<>(i.getStart(), i.getEnd(), aggregator);
		return sw.apply(signal);
	}

	@Override
	public Function<Signal<T>, Signal<R>> monitor(Formula f, Parameters parameters) {
		return f.accept(this, parameters);
	}

	

}
