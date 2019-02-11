/**
 * 
 */
package eu.quanticol.moonlight.monitoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import eu.quanticol.moonlight.formula.AndFormula;
import eu.quanticol.moonlight.formula.AtomicFormula;
import eu.quanticol.moonlight.formula.SignalDomain;
import eu.quanticol.moonlight.formula.EscapeFormula;
import eu.quanticol.moonlight.formula.EventuallyFormula;
import eu.quanticol.moonlight.formula.EverywhereFormula;
import eu.quanticol.moonlight.formula.FormulaVisitor;
import eu.quanticol.moonlight.formula.GloballyFormula;
import eu.quanticol.moonlight.formula.HystoricallyFormula;
import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.formula.NegationFormula;
import eu.quanticol.moonlight.formula.OnceFormula;
import eu.quanticol.moonlight.formula.OrFormula;
import eu.quanticol.moonlight.formula.Parameters;
import eu.quanticol.moonlight.formula.ReachFormula;
import eu.quanticol.moonlight.formula.SinceFormula;
import eu.quanticol.moonlight.formula.SomewhereFormula;
import eu.quanticol.moonlight.formula.UntilFormula;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatialSignal;

/**
 *
 */
public class SpatioTemporalMonitoring<V,T,R> implements 
		FormulaVisitor<Parameters, BiFunction<
			Function<Double,SpatialModel<V>>,
			SpatialSignal<T>,
			SpatialSignal<R>>> {

	private final HashMap<String,Function<Parameters,Function<T,R>>> atomicPropositions;

	private final HashMap<String,Function<SpatialModel<V>,DistanceStructure<V, ? extends Object>>> distanceFunctions;
	
	private final SignalDomain<R> module;
	
	private final boolean staticSpace;
		
	/**
	 * @param atomicPropositions
	 * @param module
	 */
	public SpatioTemporalMonitoring(
			HashMap<String, Function<Parameters, Function<T, R>>> atomicPropositions,
			HashMap<String,Function<SpatialModel<V>,DistanceStructure<V, ? extends Object>>> distanceFunctions,
			SignalDomain<R> module,
			boolean staticSpace) {
		super();
		this.atomicPropositions = atomicPropositions;
		this.module = module;
		this.distanceFunctions = distanceFunctions;
		this.staticSpace = staticSpace;
	}	

	/* (non-Javadoc)
	 * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.AtomicFormula, java.lang.Object)
	 */
	@Override
	public BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> visit(
			AtomicFormula atomicFormula, Parameters parameters) {
		Function<Parameters,Function<T,R>> f = atomicPropositions.get(atomicFormula.getAtomicId());
		if (f == null) {
			throw new IllegalArgumentException("Unkown atomic ID "+atomicFormula.getAtomicId());
		}
		Function<T,R> atomic = f.apply(parameters);
		return (l,s) -> s.apply(atomic);
	}

	/* (non-Javadoc)
	 * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.AndFormula, java.lang.Object)
	 */
	@Override
	public BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> visit(AndFormula andFormula, Parameters parameters) {
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> leftMonitoring = andFormula.getFirstArgument().accept(this, parameters);
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> rightMonitoring = andFormula.getSecondArgument().accept(this, parameters);
		return (l,s) -> SpatialSignal.apply(leftMonitoring.apply(l,s), module::conjunction, rightMonitoring.apply(l,s));
	}

	/* (non-Javadoc)
	 * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.NegationFormula, java.lang.Object)
	 */
	@Override
	public BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> visit(
			NegationFormula negationFormula, Parameters parameters) {
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> m = negationFormula.getArgument().accept(this, parameters);
		return (l,s) -> m.apply(l,s).apply(module::negation);
	}

	/* (non-Javadoc)
	 * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.OrFormula, java.lang.Object)
	 */
	@Override
	public BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> visit(OrFormula orFormula,
			Parameters parameters) {
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> leftMonitoring = orFormula.getFirstArgument().accept(this, parameters);
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> rightMonitoring = orFormula.getSecondArgument().accept(this, parameters);
		return (l,s) -> SpatialSignal.apply(leftMonitoring.apply(l,s), module::disjunction, rightMonitoring.apply(l,s));
	}

	/* (non-Javadoc)
	 * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.EventuallyFormula, java.lang.Object)
	 */
	@Override
	public BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> visit(
			EventuallyFormula eventuallyFormula, Parameters parameters) {
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> m = eventuallyFormula.getArgument().accept(this, parameters);
		if (eventuallyFormula.isUnbounded()) {
			return (l,s) -> m.apply(l, s).applyToSignal(x -> x.iterateBackward( module::disjunction , module.min() ));
		} else {
			Interval interval = eventuallyFormula.getInterval();
			return (l,s) -> m.apply(l, s).applyToSignal(x -> TemporalMonitoring.temporalMonitoring(x, module::disjunction, interval,true));
		}
	}

	/* (non-Javadoc)
	 * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.GloballyFormula, java.lang.Object)
	 */
	@Override
	public BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> visit(
			GloballyFormula globallyFormula, Parameters parameters) {
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> m = globallyFormula.getArgument().accept(this, parameters);
		if (globallyFormula.isUnbounded()) {
			return (l,s) -> m.apply(l, s).applyToSignal(x -> x.iterateBackward( module::conjunction , module.max() ));
		} else {
			Interval interval = globallyFormula.getInterval();
			return (l,s) -> m.apply(l, s).applyToSignal(x -> TemporalMonitoring.temporalMonitoring(x, module::conjunction, interval,true));
		}
	}

	/* (non-Javadoc)
	 * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.UntilFormula, java.lang.Object)
	 */
	@Override
	public BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> visit(
			UntilFormula untilFormula, Parameters parameters) {
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> firstMonitoring = untilFormula.getFirstArgument().accept(this, parameters);
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> secondMonitoring = untilFormula.getSecondArgument().accept(this, parameters);
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> unboundedMonitoring = 
				(l,s) -> SpatialSignal.applyToSignal(
							firstMonitoring.apply(l, s), 
							(s1,s2) -> TemporalMonitoring.unboundedUntilMonitoring( s1 , s2 , module), 
							secondMonitoring.apply(l, s));
		if (untilFormula.isUnbounded()) {
			return unboundedMonitoring;
		} else {
			return (l,s) -> 
				SpatialSignal.applyToSignal(firstMonitoring.apply(l, s), 
					(s1,s2) -> TemporalMonitoring.boundedUntilMonitoring(s1,untilFormula.getInterval(),s2,module), 						
					secondMonitoring.apply(l, s));
		}
	}

	/* (non-Javadoc)
	 * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.SinceFormula, java.lang.Object)
	 */
	@Override
	public BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> visit(
			SinceFormula sinceFormula, Parameters parameters) {
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> firstMonitoring = sinceFormula.getFirstArgument().accept(this, parameters);
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> secondMonitoring = sinceFormula.getSecondArgument().accept(this, parameters);
		if (sinceFormula.isUnbounded()) {
			return (l,s) -> SpatialSignal.applyToSignal(
					firstMonitoring.apply(l, s), 
					(s1,s2) -> TemporalMonitoring.unboundedSinceMonitoring(s1, s2, module), 
					secondMonitoring.apply(l, s));
		} else {
			return (l,s) -> SpatialSignal.applyToSignal(
					firstMonitoring.apply(l, s), 
					(s1,s2) -> TemporalMonitoring.boundedSinceMonitoring(s1, sinceFormula.getInterval(),s2, module), 
					secondMonitoring.apply(l, s));
		}
	}

	/* (non-Javadoc)
	 * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.HystoricallyFormula, java.lang.Object)
	 */
	@Override
	public BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> visit(
			HystoricallyFormula hystoricallyFormula, Parameters parameters) {
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>>  argumentMonitoring = hystoricallyFormula.getArgument().accept(this, parameters);
		if (hystoricallyFormula.isUnbounded()) {
			return (l,s) -> argumentMonitoring.apply(l,s).applyToSignal(x -> x.iterateForward( module::conjunction , module.min() )); 
		} else {
			Interval interval = hystoricallyFormula.getInterval();
			return (l,s) -> argumentMonitoring.apply(l, s).applyToSignal(x -> TemporalMonitoring.temporalMonitoring(x, module::conjunction, interval,false));
		}
	}

	/* (non-Javadoc)
	 * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.OnceFormula, java.lang.Object)
	 */
	@Override
	public BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> visit(
			OnceFormula onceFormula, Parameters parameters) {
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>>  argumentMonitoring = onceFormula.getArgument().accept(this, parameters);
		if (onceFormula.isUnbounded()) {
			return (l,s) -> argumentMonitoring.apply(l,s).applyToSignal(x -> x.iterateForward( module::conjunction , module.min() )); 
		} else {
			Interval interval = onceFormula.getInterval();
			return (l,s) -> argumentMonitoring.apply(l, s).applyToSignal(x -> TemporalMonitoring.temporalMonitoring(x, module::disjunction, interval,false));
		}
	}

	/* (non-Javadoc)
	 * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.SomewhereFormula, java.lang.Object)
	 */
	@Override
	public BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> visit(
			SomewhereFormula somewhereFormula, Parameters parameters) {
		Function<SpatialModel<V>, DistanceStructure<V, ? extends Object>> distanceFunction = distanceFunctions.get(somewhereFormula.getDistanceFunctionId());
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> argumentMonitor = somewhereFormula.getArgument().accept(this, parameters);
		return (l,s) -> computeSomewhere( l, distanceFunction , argumentMonitor.apply(l, s));
	}

	public SpatialSignal<R> computeSomewhere(
			Function<Double, SpatialModel<V>> l, Function<SpatialModel<V>, DistanceStructure<V, ? extends Object>> distanceFunction,
			SpatialSignal<R> s) {
		if (staticSpace) {
			return computeSomewhereStatic( l, distanceFunction, s );
		} else {
			return computeSomewhereDynamic( l, distanceFunction, s );
		}
	}

	private SpatialSignal<R> computeSomewhereDynamic(
			Function<Double, SpatialModel<V>> l, Function<SpatialModel<V>, DistanceStructure<V, ? extends Object>> distanceFunction, SpatialSignal<R> s) {
		SpatialSignal<R> toReturn = new SpatialSignal<R>(s.getNumberOfLocations());
		ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
		double time = cursor.getTime();
		while (!cursor.completed()) {
			SpatialModel<V> sm = l.apply(time);
			DistanceStructure<V,? extends Object> f = distanceFunction.apply(sm);
			toReturn.add(time, f.somewhere( module, cursor.getValue() ));
			time = cursor.forward();
		}
		//TODO: Manage end of signal!
		return toReturn;
	}

	private SpatialSignal<R> computeSomewhereStatic(
			Function<Double, SpatialModel<V>> l, Function<SpatialModel<V>, DistanceStructure<V, ? extends Object>> distanceFunction, SpatialSignal<R> s) {
		SpatialSignal<R> toReturn = new SpatialSignal<R>(s.getNumberOfLocations());
		ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
		double time = cursor.getTime();
		SpatialModel<V> sm = l.apply(time);
		DistanceStructure<V,? extends Object> f = distanceFunction.apply(sm);
		while (!cursor.completed()) {
			toReturn.add(time, f.somewhere( module, cursor.getValue() ));
			time = cursor.forward();
		}
		//TODO: Manage end of signal!
		return toReturn;
	}

	/* (non-Javadoc)
	 * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.EverywhereFormula, java.lang.Object)
	 */
	@Override
	public BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> visit(
			EverywhereFormula everywhereFormula, Parameters parameters) {
		Function<SpatialModel<V>, DistanceStructure<V, ? extends Object>> distanceFunction = distanceFunctions.get(everywhereFormula.getDistanceFunctionId());
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> argumentMonitor = everywhereFormula.getArgument().accept(this, parameters);
		return (l,s) -> computeEverywhere( l, distanceFunction , argumentMonitor.apply(l, s));
	}

	public SpatialSignal<R> computeEverywhere(
			Function<Double, SpatialModel<V>> l, Function<SpatialModel<V>, DistanceStructure<V, ? extends Object>> distanceFunction,
			SpatialSignal<R> s) {
		if (staticSpace) {
			return computeEverywhereStatic( l, distanceFunction, s );
		} else {
			return computeEverywhereDynamic( l, distanceFunction, s );
		}
	}

	private SpatialSignal<R> computeEverywhereDynamic(
			Function<Double, SpatialModel<V>> l, Function<SpatialModel<V>, DistanceStructure<V, ? extends Object>> distanceFunction, SpatialSignal<R> s) {
		SpatialSignal<R> toReturn = new SpatialSignal<R>(s.getNumberOfLocations());
		ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
		double time = cursor.getTime();
		while (!cursor.completed()) {
			SpatialModel<V> sm = l.apply(time);
			DistanceStructure<V,? extends Object> f = distanceFunction.apply(sm);
			toReturn.add(time, f.everywhere( module, cursor.getValue() ));
			time = cursor.forward();
		}
		//TODO: Manage end of signal!
		return toReturn;
	}

	private SpatialSignal<R> computeEverywhereStatic(
			Function<Double, SpatialModel<V>> l, Function<SpatialModel<V>, DistanceStructure<V, ? extends Object>> distanceFunction, SpatialSignal<R> s) {
		SpatialSignal<R> toReturn = new SpatialSignal<R>(s.getNumberOfLocations());
		ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
		double time = cursor.getTime();
		SpatialModel<V> sm = l.apply(time);
		DistanceStructure<V,? extends Object> f = distanceFunction.apply(sm);
		while (!cursor.completed()) {
			toReturn.add(time, f.everywhere( module, cursor.getValue() ));
			time = cursor.forward();
		}
		//TODO: Manage end of signal!
		return toReturn;
	}	
	
	/* (non-Javadoc)
	 * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.ReachFormula, java.lang.Object)
	 */
	@Override
	public BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> visit(
			ReachFormula reachFormula, Parameters parameters) {
		Function<SpatialModel<V>, DistanceStructure<V, ? extends Object>> distanceFunction = distanceFunctions.get(reachFormula.getDistanceFunctionId());
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> m1 = reachFormula.getFirstArgument().accept(this, parameters);
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> m2 = reachFormula.getSecondArgument().accept(this, parameters);
		if (staticSpace) {
			return (l,s) -> computeReachStatic( l, m1.apply(l, s), distanceFunction , m2.apply(l, s));			
		} else {
			return (l,s) -> computeReachDynamic( l, m1.apply(l, s), distanceFunction , m2.apply(l, s));						
		}
	}

	private  SpatialSignal<R> computeReachDynamic(Function<Double, SpatialModel<V>> l, SpatialSignal<R> s1,
			Function<SpatialModel<V>, DistanceStructure<V, ? extends Object>> distanceFunction,
			SpatialSignal<R> s2) {
		SpatialSignal<R> toReturn = new SpatialSignal<R>(s1.getNumberOfLocations());
		ParallelSignalCursor<R> c1 = s1.getSignalCursor(true);
		ParallelSignalCursor<R> c2 = s2.getSignalCursor(true);
		double time = Math.max(s1.start(),s2.start());
		c1.move(time);
		c2.move(time);
		while (!c1.completed()&&!c2.completed()) {
			SpatialModel<V> sm = l.apply(c1.getTime());
			DistanceStructure<V,? extends Object> f = distanceFunction.apply(sm);
			ArrayList<R> values = f.reach(module, c1.getValue(), c2.getValue());
			toReturn.add(time, (i -> values.get(i)));
			time = Math.min(c1.nextTime(), c2.nextTime());
			c1.move(time);
			c2.move(time);
		}
		return toReturn;
	}

	private  SpatialSignal<R> computeReachStatic(Function<Double, SpatialModel<V>> l, SpatialSignal<R> s1,
			Function<SpatialModel<V>, DistanceStructure<V, ? extends Object>> distanceFunction,
			SpatialSignal<R> s2) {
		SpatialSignal<R> toReturn = new SpatialSignal<R>(s1.getNumberOfLocations());
		ParallelSignalCursor<R> c1 = s1.getSignalCursor(true);
		ParallelSignalCursor<R> c2 = s2.getSignalCursor(true);
		double time = Math.max(s1.start(),s2.start());
		c1.move(time);
		c2.move(time);
		SpatialModel<V> sm = l.apply(c1.getTime());
		DistanceStructure<V,? extends Object> f = distanceFunction.apply(sm);
		while (!c1.completed()&&!c2.completed()) {
			ArrayList<R> values = f.reach(module, c1.getValue(), c2.getValue());
			toReturn.add(time, (i -> values.get(i)));
			time = Math.min(c1.nextTime(), c2.nextTime());
			c1.move(time);
			c2.move(time);
		}
		return toReturn;
	}

	/* (non-Javadoc)
	 * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.EscapeFormula, java.lang.Object)
	 */
	@Override
	public BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> visit(
			EscapeFormula escapeFormula, Parameters parameters) {
		Function<SpatialModel<V>, DistanceStructure<V, ? extends Object>> distanceFunction = distanceFunctions.get(escapeFormula.getDistanceFunctionId());
		BiFunction<Function<Double, SpatialModel<V>>, SpatialSignal<T>, SpatialSignal<R>> argumentMonitor = escapeFormula.getArgument().accept(this, parameters);
		if (staticSpace) {
			return (l,s) -> computeEscapeStatic( l, distanceFunction , argumentMonitor.apply(l, s));			
		} else {
			return (l,s) -> computeEscapeDynamic( l, distanceFunction , argumentMonitor.apply(l, s));						
		}
	}

	private SpatialSignal<R> computeEscapeStatic(Function<Double, SpatialModel<V>> l,
			Function<SpatialModel<V>, DistanceStructure<V, ? extends Object>> distanceFunction,
			SpatialSignal<R> s) {
		SpatialSignal<R> toReturn = new SpatialSignal<R>(s.getNumberOfLocations());
		ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
		double time = cursor.getTime();
		SpatialModel<V> sm = l.apply(time);
		DistanceStructure<V,? extends Object> f = distanceFunction.apply(sm);
		while (!cursor.completed()) {
			toReturn.add(time, f.escape(module, cursor.getValue() ));
			time = cursor.forward();
		}
		//TODO: Manage end of signal!
		return toReturn;
	}
	
	private SpatialSignal<R> computeEscapeDynamic(Function<Double, SpatialModel<V>> l,
			Function<SpatialModel<V>, DistanceStructure<V, ? extends Object>> distanceFunction,
			SpatialSignal<R> s) {
		SpatialSignal<R> toReturn = new SpatialSignal<R>(s.getNumberOfLocations());
		ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
		double time = cursor.getTime();
		while (!cursor.completed()) {
			SpatialModel<V> sm = l.apply(time);
			DistanceStructure<V,? extends Object> f = distanceFunction.apply(sm);
			toReturn.add(time, f.everywhere( module, cursor.getValue() ));
			time = cursor.forward();
		}
		//TODO: Manage end of signal!
		return toReturn;
	}
	

}
