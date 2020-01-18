/**
 * 
 */
package eu.quanticol.moonlight.monitoring;

import java.util.HashMap;
import java.util.function.Function;

import eu.quanticol.moonlight.formula.AndFormula;
import eu.quanticol.moonlight.formula.AtomicFormula;
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
import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.formula.SinceFormula;
import eu.quanticol.moonlight.formula.UntilFormula;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;

/**
 *
 */
public class TemporalMonitoring<T,R> implements 
		FormulaVisitor<Parameters, TemporalMonitor<T,R>> {
	
	private final HashMap<String,Function<Parameters,Function<T,R>>> atomicPropositions;
	private final SignalDomain<R> module;
	
	public TemporalMonitoring( SignalDomain<R> module ) {
		this( new HashMap<>(), module );
	}
	
	public TemporalMonitoring( HashMap<String,Function<Parameters,Function<T,R>>> atomicPropositions  , SignalDomain<R> module ) {
		this.atomicPropositions = atomicPropositions;
		this.module = module;
	}

	@Override
	public TemporalMonitor<T,R> visit(AtomicFormula atomicFormula, Parameters parameters) {
		Function<Parameters,Function<T,R>> f = atomicPropositions.get(atomicFormula.getAtomicId());
		if (f == null) {
			throw new IllegalArgumentException("Unkown atomic ID "+atomicFormula.getAtomicId());
		}
		Function<T,R> atomic = f.apply(parameters);
		return TemporalMonitor.atomicMonitor(atomic);
	}

	@Override
	public TemporalMonitor<T,R> visit(AndFormula andFormula, Parameters parameters) {
		TemporalMonitor<T,R> leftMonitoring = andFormula.getFirstArgument().accept(this, parameters);
		TemporalMonitor<T,R>  rightMonitoring = andFormula.getSecondArgument().accept(this, parameters);
		return TemporalMonitor.andMonitor(leftMonitoring, module , rightMonitoring);
	}

	@Override
	public TemporalMonitor<T,R> visit(NegationFormula negationFormula, Parameters parameters) {
		TemporalMonitor<T,R> argumentMonitoring = negationFormula.getArgument().accept(this, parameters);
		return TemporalMonitor.notMonitor(argumentMonitoring, module );
	}

	@Override
	public TemporalMonitor<T,R> visit(OrFormula orFormula, Parameters parameters) {
		TemporalMonitor<T,R> leftMonitoring = orFormula.getFirstArgument().accept(this, parameters);
		TemporalMonitor<T,R>  rightMonitoring = orFormula.getSecondArgument().accept(this, parameters);
		return TemporalMonitor.andMonitor(leftMonitoring, module , rightMonitoring);
	}

	@Override
	public TemporalMonitor<T,R> visit(EventuallyFormula eventuallyFormula, Parameters parameters) {
		TemporalMonitor<T,R> argumentMonitoring = eventuallyFormula.getArgument().accept(this, parameters);		
		if (eventuallyFormula.isUnbounded()) {
			return TemporalMonitor.eventuallyMonitor(argumentMonitoring, module); 
		} else {
			Interval interval = eventuallyFormula.getInterval();
			return TemporalMonitor.eventuallyMonitor(argumentMonitoring, module, interval); 
		}
	}

	@Override
	public TemporalMonitor<T,R> visit(GloballyFormula globallyFormula, Parameters parameters) {
		TemporalMonitor<T,R> argumentMonitoring = globallyFormula.getArgument().accept(this, parameters);		
		if (globallyFormula.isUnbounded()) {
			return TemporalMonitor.globallyMonitor(argumentMonitoring, module); 
		} else {
			Interval interval = globallyFormula.getInterval();
			return TemporalMonitor.globallyMonitor(argumentMonitoring, module, interval); 
		}
	}

	@Override
	public TemporalMonitor<T,R> visit(UntilFormula untilFormula, Parameters parameters) {
		TemporalMonitor<T,R>  firstMonitoring = untilFormula.getFirstArgument().accept(this, parameters);
		TemporalMonitor<T,R>  secondMonitoring = untilFormula.getSecondArgument().accept(this, parameters);
		if (untilFormula.isUnbounded()) {
			return TemporalMonitor.untilMonitor(firstMonitoring, secondMonitoring, module);
		} else {
			return TemporalMonitor.untilMonitor(firstMonitoring, untilFormula.getInterval(), secondMonitoring, module);
		}
	}

	public TemporalMonitor<T,R> monitor(Formula f, Parameters parameters) {
		return f.accept(this, parameters);
	}

	@Override
	public TemporalMonitor<T,R> visit(SinceFormula sinceFormula, Parameters parameters) {
		TemporalMonitor<T,R> firstMonitoring = sinceFormula.getFirstArgument().accept(this, parameters);
		TemporalMonitor<T,R> secondMonitoring = sinceFormula.getSecondArgument().accept(this, parameters);
		if (sinceFormula.isUnbounded()) {
			return TemporalMonitor.sinceMonitor(firstMonitoring, secondMonitoring, module);
		} else {
			return TemporalMonitor.sinceMonitor(firstMonitoring, sinceFormula.getInterval(), secondMonitoring, module);
		}
	}

	@Override
	public TemporalMonitor<T,R> visit(HystoricallyFormula hystoricallyFormula, Parameters parameters) {
		TemporalMonitor<T,R> argumentMonitoring = hystoricallyFormula.getArgument().accept(this, parameters);
		if (hystoricallyFormula.isUnbounded()) {
			return TemporalMonitor.hystoricallyMonitor(argumentMonitoring, module);
		} else {
			Interval interval = hystoricallyFormula.getInterval();
			return TemporalMonitor.hystoricallyMonitor(argumentMonitoring, module, hystoricallyFormula.getInterval());
		}
	}

	@Override
	public TemporalMonitor<T,R> visit(OnceFormula onceFormula, Parameters parameters) {
		TemporalMonitor<T,R> argumentMonitoring = onceFormula.getArgument().accept(this, parameters);
		if (onceFormula.isUnbounded()) {
			return TemporalMonitor.onceMonitor(argumentMonitoring, module);
		} else {
			Interval interval = onceFormula.getInterval();
			return TemporalMonitor.onceMonitor(argumentMonitoring, module, interval);
		}
	}

	public void addProperty(String name, Function<Parameters,Function<T,R>> atomic ) {
		atomicPropositions.put(name, atomic);
	}

	

}
