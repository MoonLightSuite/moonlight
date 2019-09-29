/**
 *
 */
package eu.quanticol.moonlight.monitoring;

import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
import java.util.function.Function;

/**
 *
 */
public class SpatioTemporalMonitoring<V, T, R> implements
        FormulaVisitor<Parameters, BiFunction<
                DoubleFunction<SpatialModel<V>>,
                SpatioTemporalSignal<T>,
                SpatioTemporalSignal<R>>> {

    private final Map<String, Function<Parameters, Function<T, R>>> atomicPropositions;

    private final Map<String, Function<SpatialModel<V>, DistanceStructure<V, ?>>> distanceFunctions;

    private final SignalDomain<R> module;

    private final boolean staticSpace;


    public BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> monitor(Formula f, Parameters parameters) {
        return f.accept(this, parameters);
    }

    /**
     * @param atomicPropositions
     * @param module
     */
    public SpatioTemporalMonitoring(
            Map<String, Function<Parameters, Function<T, R>>> atomicPropositions,
            Map<String, Function<SpatialModel<V>, DistanceStructure<V, ?>>> distanceFunctions,
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
    public BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> visit(
            AtomicFormula atomicFormula, Parameters parameters) {
        Function<Parameters, Function<T, R>> f = atomicPropositions.get(atomicFormula.getAtomicId());
        if (f == null) {
            throw new IllegalArgumentException("Unkown atomic ID " + atomicFormula.getAtomicId());
        }
        Function<T, R> atomic = f.apply(parameters);
        return (l, s) -> s.apply(atomic);
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.AndFormula, java.lang.Object)
     */
    @Override
    public BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> visit(AndFormula andFormula, Parameters parameters) {
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> leftMonitoring = andFormula.getFirstArgument().accept(this, parameters);
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> rightMonitoring = andFormula.getSecondArgument().accept(this, parameters);
        return (l, s) -> SpatioTemporalSignal.apply(leftMonitoring.apply(l, s), module::conjunction, rightMonitoring.apply(l, s));
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.NegationFormula, java.lang.Object)
     */
    @Override
    public BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> visit(
            NegationFormula negationFormula, Parameters parameters) {
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> m = negationFormula.getArgument().accept(this, parameters);
        return (l, s) -> m.apply(l, s).apply(module::negation);
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.OrFormula, java.lang.Object)
     */
    @Override
    public BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> visit(OrFormula orFormula,
                                                                                                                 Parameters parameters) {
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> leftMonitoring = orFormula.getFirstArgument().accept(this, parameters);
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> rightMonitoring = orFormula.getSecondArgument().accept(this, parameters);
        return (l, s) -> SpatioTemporalSignal.apply(leftMonitoring.apply(l, s), module::disjunction, rightMonitoring.apply(l, s));
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.EventuallyFormula, java.lang.Object)
     */
    @Override
    public BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> visit(
            EventuallyFormula eventuallyFormula, Parameters parameters) {
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> m = eventuallyFormula.getArgument().accept(this, parameters);
        if (eventuallyFormula.isUnbounded()) {
            return (l, s) -> m.apply(l, s).applyToSignal(x -> x.iterateBackward(module::disjunction, module.min()));
        } else {
            Interval interval = eventuallyFormula.getInterval();
            return (l, s) -> m.apply(l, s).applyToSignal(x -> TemporalMonitoring.temporalMonitoring(x, module::disjunction, interval, true));
        }
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.GloballyFormula, java.lang.Object)
     */
    @Override
    public BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> visit(
            GloballyFormula globallyFormula, Parameters parameters) {
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> m = globallyFormula.getArgument().accept(this, parameters);
        if (globallyFormula.isUnbounded()) {
            return (l, s) -> m.apply(l, s).applyToSignal(x -> x.iterateBackward(module::conjunction, module.max()));
        } else {
            Interval interval = globallyFormula.getInterval();
            return (l, s) -> m.apply(l, s).applyToSignal(x -> TemporalMonitoring.temporalMonitoring(x, module::conjunction, interval, true));
        }
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.UntilFormula, java.lang.Object)
     */
    @Override
    public BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> visit(
            UntilFormula untilFormula, Parameters parameters) {
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> firstMonitoring = untilFormula.getFirstArgument().accept(this, parameters);
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> secondMonitoring = untilFormula.getSecondArgument().accept(this, parameters);
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> unboundedMonitoring =
                (l, s) -> SpatioTemporalSignal.applyToSignal(
                        firstMonitoring.apply(l, s),
                        (s1, s2) -> TemporalMonitoring.unboundedUntilMonitoring(s1, s2, module),
                        secondMonitoring.apply(l, s));
        if (untilFormula.isUnbounded()) {
            return unboundedMonitoring;
        } else {
            return (l, s) ->
                    SpatioTemporalSignal.applyToSignal(firstMonitoring.apply(l, s),
                            (s1, s2) -> TemporalMonitoring.boundedUntilMonitoring(s1, untilFormula.getInterval(), s2, module),
                            secondMonitoring.apply(l, s));
        }
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.SinceFormula, java.lang.Object)
     */
    @Override
    public BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> visit(
            SinceFormula sinceFormula, Parameters parameters) {
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> firstMonitoring = sinceFormula.getFirstArgument().accept(this, parameters);
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> secondMonitoring = sinceFormula.getSecondArgument().accept(this, parameters);
        if (sinceFormula.isUnbounded()) {
            return (l, s) -> SpatioTemporalSignal.applyToSignal(
                    firstMonitoring.apply(l, s),
                    (s1, s2) -> TemporalMonitoring.unboundedSinceMonitoring(s1, s2, module),
                    secondMonitoring.apply(l, s));
        } else {
            return (l, s) -> SpatioTemporalSignal.applyToSignal(
                    firstMonitoring.apply(l, s),
                    (s1, s2) -> TemporalMonitoring.boundedSinceMonitoring(s1, sinceFormula.getInterval(), s2, module),
                    secondMonitoring.apply(l, s));
        }
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.HystoricallyFormula, java.lang.Object)
     */
    @Override
    public BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> visit(
            HystoricallyFormula hystoricallyFormula, Parameters parameters) {
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> argumentMonitoring = hystoricallyFormula.getArgument().accept(this, parameters);
        if (hystoricallyFormula.isUnbounded()) {
            return (l, s) -> argumentMonitoring.apply(l, s).applyToSignal(x -> x.iterateForward(module::conjunction, module.min()));
        } else {
            Interval interval = hystoricallyFormula.getInterval();
            return (l, s) -> argumentMonitoring.apply(l, s).applyToSignal(x -> TemporalMonitoring.temporalMonitoring(x, module::conjunction, interval, false));
        }
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.OnceFormula, java.lang.Object)
     */
    @Override
    public BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> visit(
            OnceFormula onceFormula, Parameters parameters) {
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> argumentMonitoring = onceFormula.getArgument().accept(this, parameters);
        if (onceFormula.isUnbounded()) {
            return (l, s) -> argumentMonitoring.apply(l, s).applyToSignal(x -> x.iterateForward(module::conjunction, module.min()));
        } else {
            Interval interval = onceFormula.getInterval();
            return (l, s) -> argumentMonitoring.apply(l, s).applyToSignal(x -> TemporalMonitoring.temporalMonitoring(x, module::disjunction, interval, false));
        }
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.SomewhereFormula, java.lang.Object)
     */
    @Override
    public BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> visit(
            SomewhereFormula somewhereFormula, Parameters parameters) {
        Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction = distanceFunctions.get(somewhereFormula.getDistanceFunctionId());
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> argumentMonitor = somewhereFormula.getArgument().accept(this, parameters);
        return (l, s) -> computeSomewhere(l, distanceFunction, argumentMonitor.apply(l, s));
    }

    private SpatioTemporalSignal<R> computeSomewhere(
            DoubleFunction<SpatialModel<V>> l, Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction,
            SpatioTemporalSignal<R> s) {
        if (staticSpace) {
            return computeSomewhereStatic(l, distanceFunction, s);
        } else {
            return computeSomewhereDynamic(l, distanceFunction, s);
        }
    }

    private SpatioTemporalSignal<R> computeSomewhereDynamic(
            DoubleFunction<SpatialModel<V>> l, Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction, SpatioTemporalSignal<R> s) {
        SpatioTemporalSignal<R> toReturn = new SpatioTemporalSignal<R>(s.getNumberOfLocations());
        ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
        double time = cursor.getTime();
        while (!cursor.completed() && !Double.isNaN(time)) {
            SpatialModel<V> sm = l.apply(time);
            DistanceStructure<V, ?> f = distanceFunction.apply(sm);
            toReturn.add(time, f.somewhere(module, cursor.getValue()));
            time = cursor.forward();
        }
        //TODO: Manage end of signal!
        return toReturn;
    }

    private SpatioTemporalSignal<R> computeSomewhereStatic(
			DoubleFunction<SpatialModel<V>> l, Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction, SpatioTemporalSignal<R> s) {
        SpatioTemporalSignal<R> toReturn = new SpatioTemporalSignal<R>(s.getNumberOfLocations());
        ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
        double time = cursor.getTime();
        SpatialModel<V> sm = l.apply(time);
        DistanceStructure<V, ?> f = distanceFunction.apply(sm);
        while (!cursor.completed() && !Double.isNaN(time)) {
            toReturn.add(time, f.somewhere(module, cursor.getValue()));
            time = cursor.forward();
        }
        //TODO: Manage end of signal!
        return toReturn;
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.EverywhereFormula, java.lang.Object)
     */
    @Override
    public BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> visit(
            EverywhereFormula everywhereFormula, Parameters parameters) {
        Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction = distanceFunctions.get(everywhereFormula.getDistanceFunctionId());
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> argumentMonitor = everywhereFormula.getArgument().accept(this, parameters);
        return (l, s) -> computeEverywhere(l, distanceFunction, argumentMonitor.apply(l, s));
    }

    public SpatioTemporalSignal<R> computeEverywhere(
            DoubleFunction<SpatialModel<V>> l, Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction,
            SpatioTemporalSignal<R> s) {
        if (staticSpace) {
            return computeEverywhereStatic(l, distanceFunction, s);
        } else {
            return computeEverywhereDynamic(l, distanceFunction, s);
        }
    }

    private SpatioTemporalSignal<R> computeEverywhereDynamic(
            DoubleFunction<SpatialModel<V>> l, Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction, SpatioTemporalSignal<R> s) {
        SpatioTemporalSignal<R> toReturn = new SpatioTemporalSignal<R>(s.getNumberOfLocations());
        ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
        double time = cursor.getTime();
        while (!cursor.completed() && !Double.isNaN(time)) {
            SpatialModel<V> sm = l.apply(time);
            DistanceStructure<V, ?> f = distanceFunction.apply(sm);
            toReturn.add(time, f.everywhere(module, cursor.getValue()));
            time = cursor.forward();
        }
        //TODO: Manage end of signal!
        return toReturn;
    }

    private SpatioTemporalSignal<R> computeEverywhereStatic(
            DoubleFunction<SpatialModel<V>> l, Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction, SpatioTemporalSignal<R> s) {
        SpatioTemporalSignal<R> toReturn = new SpatioTemporalSignal<R>(s.getNumberOfLocations());
        ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
        double time = cursor.getTime();
        SpatialModel<V> sm = l.apply(time);
        DistanceStructure<V, ?> f = distanceFunction.apply(sm);
        while (!cursor.completed() && !Double.isNaN(time)) {
            toReturn.add(time, f.everywhere(module, cursor.getValue()));
            time = cursor.forward();
        }
        //TODO: Manage end of signal!
        return toReturn;
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.ReachFormula, java.lang.Object)
     */
    @Override
    public BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> visit(
            ReachFormula reachFormula, Parameters parameters) {
        Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction = distanceFunctions.get(reachFormula.getDistanceFunctionId());
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> m1 = reachFormula.getFirstArgument().accept(this, parameters);
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> m2 = reachFormula.getSecondArgument().accept(this, parameters);
        if (staticSpace) {
            return (l, s) -> computeReachStatic(l, m1.apply(l, s), distanceFunction, m2.apply(l, s));
        } else {
            return (l, s) -> computeReachDynamic(l, m1.apply(l, s), distanceFunction, m2.apply(l, s));
        }
    }

    private SpatioTemporalSignal<R> computeReachDynamic(DoubleFunction<SpatialModel<V>> l, SpatioTemporalSignal<R> s1,
                                                        Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction,
                                                        SpatioTemporalSignal<R> s2) {
        SpatioTemporalSignal<R> toReturn = new SpatioTemporalSignal<R>(s1.getNumberOfLocations());
        ParallelSignalCursor<R> c1 = s1.getSignalCursor(true);
        ParallelSignalCursor<R> c2 = s2.getSignalCursor(true);
        double time = Math.max(s1.start(), s2.start());
        c1.move(time);
        c2.move(time);
        while (!c1.completed() && !c2.completed() && !Double.isNaN(time)) {
            SpatialModel<V> sm = l.apply(c1.getTime());
            DistanceStructure<V, ?> f = distanceFunction.apply(sm);
            ArrayList<R> values = f.reach(module, c1.getValue(), c2.getValue());
            toReturn.add(time, (values::get));
            time = Math.min(c1.nextTime(), c2.nextTime());
            c1.move(time);
            c2.move(time);
        }
        return toReturn;
    }

    private SpatioTemporalSignal<R> computeReachStatic(DoubleFunction<SpatialModel<V>> l, SpatioTemporalSignal<R> s1,
                                                       Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction,
                                                       SpatioTemporalSignal<R> s2) {
        SpatioTemporalSignal<R> toReturn = new SpatioTemporalSignal<R>(s1.getNumberOfLocations());
        ParallelSignalCursor<R> c1 = s1.getSignalCursor(true);
        ParallelSignalCursor<R> c2 = s2.getSignalCursor(true);
        double time = Math.max(s1.start(), s2.start());
        c1.move(time);
        c2.move(time);
        SpatialModel<V> sm = l.apply(c1.getTime());
        DistanceStructure<V, ?> f = distanceFunction.apply(sm);
        while (!c1.completed() && !c2.completed() && !Double.isNaN(time)) {
            ArrayList<R> values = f.reach(module, c1.getValue(), c2.getValue());
            toReturn.add(time, (values::get));
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
    public BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> visit(
            EscapeFormula escapeFormula, Parameters parameters) {
        Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction = distanceFunctions.get(escapeFormula.getDistanceFunctionId());
        BiFunction<DoubleFunction<SpatialModel<V>>, SpatioTemporalSignal<T>, SpatioTemporalSignal<R>> argumentMonitor = escapeFormula.getArgument().accept(this, parameters);
        if (staticSpace) {
            return (l, s) -> computeEscapeStatic(l, distanceFunction, argumentMonitor.apply(l, s));
        } else {
            return (l, s) -> computeEscapeDynamic(l, distanceFunction, argumentMonitor.apply(l, s));
        }
    }

    private SpatioTemporalSignal<R> computeEscapeStatic(DoubleFunction<SpatialModel<V>> l,
                                                        Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction,
                                                        SpatioTemporalSignal<R> s) {
        SpatioTemporalSignal<R> toReturn = new SpatioTemporalSignal<R>(s.getNumberOfLocations());
        ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
        double time = cursor.getTime();
        SpatialModel<V> sm = l.apply(time);
        DistanceStructure<V, ?> f = distanceFunction.apply(sm);
        while (!cursor.completed() && !Double.isNaN(time)) {
            toReturn.add(time, f.escape(module, cursor.getValue()));
            time = cursor.forward();
        }
        //TODO: Manage end of signal!
        return toReturn;
    }

    private SpatioTemporalSignal<R> computeEscapeDynamic(DoubleFunction<SpatialModel<V>> l,
                                                         Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction,
                                                         SpatioTemporalSignal<R> s) {
        SpatioTemporalSignal<R> toReturn = new SpatioTemporalSignal<R>(s.getNumberOfLocations());
        ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
        double time = cursor.getTime();
        while (!cursor.completed() && !Double.isNaN(time)) {
            SpatialModel<V> sm = l.apply(time);
            DistanceStructure<V, ?> f = distanceFunction.apply(sm);
            toReturn.add(time, f.everywhere(module, cursor.getValue()));
            time = cursor.forward();
        }
        //TODO: Manage end of signal!
        return toReturn;
    }


}
