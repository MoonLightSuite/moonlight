/**
 *
 */
package eu.quanticol.moonlight.monitoring;

import eu.quanticol.moonlight.algorithms.SpaceUtilities;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.space.DistanceStructure;
import eu.quanticol.moonlight.space.LocationService;
import eu.quanticol.moonlight.signal.ParallelSignalCursor;
import eu.quanticol.moonlight.space.SpatialModel;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * @deprecated use {@link SpatialTemporalMonitoring} instead.
 * TODO: this class doesn't seem to be used anymore. Can we remove it?
 * 		 After all it can always be restored from git history.
 */
@Deprecated
public class SpatioTemporalMonitoringOld<V, T, R> implements
        FormulaVisitor<Parameters, BiFunction<
                LocationService<Double, V>,
                SpatialTemporalSignal<T>,
                SpatialTemporalSignal<R>>> {

    private final Map<String, Function<Parameters, Function<T, R>>> atomicPropositions;

    private final Map<String, Function<SpatialModel<V>, DistanceStructure<V, ?>>> distanceFunctions;

    private final SignalDomain<R> module;

    private final boolean staticSpace;


    public BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> monitor(Formula f, Parameters parameters) {
        return f.accept(this, parameters);
    }

    /**
     * @param atomicPropositions
     * @param module
     */
    public SpatioTemporalMonitoringOld(
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
    public BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> visit(
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
    public BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> visit(AndFormula andFormula, Parameters parameters) {
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> leftMonitoring = andFormula.getFirstArgument().accept(this, parameters);
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> rightMonitoring = andFormula.getSecondArgument().accept(this, parameters);
        return (l, s) -> SpatialTemporalSignal.apply(leftMonitoring.apply(l, s), module::conjunction, rightMonitoring.apply(l, s));
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.NegationFormula, java.lang.Object)
     */
    @Override
    public BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> visit(
            NegationFormula negationFormula, Parameters parameters) {
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> m = negationFormula.getArgument().accept(this, parameters);
        return (l, s) -> m.apply(l, s).apply(module::negation);
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.OrFormula, java.lang.Object)
     */
    @Override
    public BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> visit(OrFormula orFormula,
                                                                                                    Parameters parameters) {
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> leftMonitoring = orFormula.getFirstArgument().accept(this, parameters);
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> rightMonitoring = orFormula.getSecondArgument().accept(this, parameters);
        return (l, s) -> SpatialTemporalSignal.apply(leftMonitoring.apply(l, s), module::disjunction, rightMonitoring.apply(l, s));
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.EventuallyFormula, java.lang.Object)
     */
    @Override
    public BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> visit(
            EventuallyFormula eventuallyFormula, Parameters parameters) {
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> m = eventuallyFormula.getArgument().accept(this, parameters);
        if (eventuallyFormula.isUnbounded()) {
            return (l, s) -> m.apply(l, s).applyToSignal(x -> x.iterateBackward(module::disjunction, module.min()));
        } else {
            Interval interval = eventuallyFormula.getInterval();
            return (l, s) -> m.apply(l, s).applyToSignal(x -> TemporalMonitoringOld.temporalMonitoring(x, module::disjunction, interval, true));
        }
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.GloballyFormula, java.lang.Object)
     */
    @Override
    public BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> visit(
            GloballyFormula globallyFormula, Parameters parameters) {
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> m = globallyFormula.getArgument().accept(this, parameters);
        if (globallyFormula.isUnbounded()) {
            return (l, s) -> m.apply(l, s).applyToSignal(x -> x.iterateBackward(module::conjunction, module.max()));
        } else {
            Interval interval = globallyFormula.getInterval();
            return (l, s) -> m.apply(l, s).applyToSignal(x -> TemporalMonitoringOld.temporalMonitoring(x, module::conjunction, interval, true));
        }
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.UntilFormula, java.lang.Object)
     */
    @Override
    public BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> visit(
            UntilFormula untilFormula, Parameters parameters) {
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> firstMonitoring = untilFormula.getFirstArgument().accept(this, parameters);
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> secondMonitoring = untilFormula.getSecondArgument().accept(this, parameters);
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> unboundedMonitoring =
                (l, s) -> SpatialTemporalSignal.applyToSignal(
                        firstMonitoring.apply(l, s),
                        (s1, s2) -> TemporalMonitoringOld.unboundedUntilMonitoring(s1, s2, module),
                        secondMonitoring.apply(l, s));
        if (untilFormula.isUnbounded()) {
            return unboundedMonitoring;
        } else {
            return (l, s) ->
                    SpatialTemporalSignal.applyToSignal(firstMonitoring.apply(l, s),
                            (s1, s2) -> TemporalMonitoringOld.boundedUntilMonitoring(s1, untilFormula.getInterval(), s2, module),
                            secondMonitoring.apply(l, s));
        }
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.SinceFormula, java.lang.Object)
     */
    @Override
    public BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> visit(
            SinceFormula sinceFormula, Parameters parameters) {
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> firstMonitoring = sinceFormula.getFirstArgument().accept(this, parameters);
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> secondMonitoring = sinceFormula.getSecondArgument().accept(this, parameters);
        if (sinceFormula.isUnbounded()) {
            return (l, s) -> SpatialTemporalSignal.applyToSignal(
                    firstMonitoring.apply(l, s),
                    (s1, s2) -> TemporalMonitoringOld.unboundedSinceMonitoring(s1, s2, module),
                    secondMonitoring.apply(l, s));
        } else {
            return (l, s) -> SpatialTemporalSignal.applyToSignal(
                    firstMonitoring.apply(l, s),
                    (s1, s2) -> TemporalMonitoringOld.boundedSinceMonitoring(s1, sinceFormula.getInterval(), s2, module),
                    secondMonitoring.apply(l, s));
        }
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.HistoricallyFormula, java.lang.Object)
     */
    @Override
    public BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> visit(
            HistoricallyFormula historicallyFormula, Parameters parameters) {
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> argumentMonitoring = historicallyFormula.getArgument().accept(this, parameters);
        if (historicallyFormula.isUnbounded()) {
            return (l, s) -> argumentMonitoring.apply(l, s).applyToSignal(x -> x.iterateForward(module::conjunction, module.min()));
        } else {
            Interval interval = historicallyFormula.getInterval();
            return (l, s) -> argumentMonitoring.apply(l, s).applyToSignal(x -> TemporalMonitoringOld.temporalMonitoring(x, module::conjunction, interval, false));
        }
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.OnceFormula, java.lang.Object)
     */
    @Override
    public BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> visit(
            OnceFormula onceFormula, Parameters parameters) {
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> argumentMonitoring = onceFormula.getArgument().accept(this, parameters);
        if (onceFormula.isUnbounded()) {
            return (l, s) -> argumentMonitoring.apply(l, s).applyToSignal(x -> x.iterateForward(module::conjunction, module.min()));
        } else {
            Interval interval = onceFormula.getInterval();
            return (l, s) -> argumentMonitoring.apply(l, s).applyToSignal(x -> TemporalMonitoringOld.temporalMonitoring(x, module::disjunction, interval, false));
        }
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.SomewhereFormula, java.lang.Object)
     */
    @Override
    public BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> visit(
            SomewhereFormula somewhereFormula, Parameters parameters) {
        Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction = distanceFunctions.get(somewhereFormula.getDistanceFunctionId());
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> argumentMonitor = somewhereFormula.getArgument().accept(this, parameters);
        return (l, s) -> computeSomewhere(l, distanceFunction, argumentMonitor.apply(l, s));
    }

    private SpatialTemporalSignal<R> computeSomewhere(
            LocationService<Double, V> l, Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction,
            SpatialTemporalSignal<R> s) {
        if (staticSpace) {
            return computeSomewhereStatic(l, distanceFunction, s);
        } else {
            return computeSomewhereDynamic(l, distanceFunction, s);
        }
    }

    private SpatialTemporalSignal<R> computeSomewhereDynamic(
            LocationService<Double, V> l, Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction, SpatialTemporalSignal<R> s) {
        SpatialTemporalSignal<R> toReturn = new SpatialTemporalSignal<R>(s.getNumberOfLocations());
        if (l.isEmpty()) {
            return toReturn;
        }
        ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
        Iterator<Pair<Double, SpatialModel<V>>> locationServiceIterator = l.times();
        Pair<Double, SpatialModel<V>> current = locationServiceIterator.next();
        Pair<Double, SpatialModel<V>> next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
        double time = cursor.getTime();
        while ((next != null)&&(next.getFirst()<=time)) {
            current = next;
            next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
        }
        //Loop invariant: (current.getFirst()<=time)&&((next==null)||(time<next.getFirst()))
        while (!cursor.completed() && !Double.isNaN(time)) {
            IntFunction<R> spatialSignal = cursor.getValue();
            SpatialModel<V> sm = current.getSecond();
            DistanceStructure<V, ?> f = distanceFunction.apply(sm);
            toReturn.add(time, SpaceUtilities.somewhere(module, spatialSignal, f));
            double nextTime = cursor.forward();
            while ((next != null)&&(next.getFirst()<nextTime)) {
                current = next;
                time = current.getFirst();
                next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
                f = distanceFunction.apply(current.getSecond());
                toReturn.add(time, SpaceUtilities.somewhere(module, spatialSignal, f));
            }
            time = nextTime;
        }

        //TODO: Manage end of signal!
        return toReturn;
    }

    private SpatialTemporalSignal<R> computeSomewhereStatic(
            LocationService<Double, V> l, Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction, SpatialTemporalSignal<R> s) {
        SpatialTemporalSignal<R> toReturn = new SpatialTemporalSignal<R>(s.getNumberOfLocations());
        ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
        double time = cursor.getTime();
        SpatialModel<V> sm = l.get(time);
        DistanceStructure<V, ?> f = distanceFunction.apply(sm);
        while (!cursor.completed() && !Double.isNaN(time)) {
            toReturn.add(time, SpaceUtilities.somewhere(module, cursor.getValue(), f));
            time = cursor.forward();
        }
        //TODO: Manage end of signal!
        return toReturn;
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.EverywhereFormula, java.lang.Object)
     */
    @Override
    public BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> visit(
            EverywhereFormula everywhereFormula, Parameters parameters) {
        Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction = distanceFunctions.get(everywhereFormula.getDistanceFunctionId());
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> argumentMonitor = everywhereFormula.getArgument().accept(this, parameters);
        return (l, s) -> computeEverywhere(l, distanceFunction, argumentMonitor.apply(l, s));
    }

    public SpatialTemporalSignal<R> computeEverywhere(
            LocationService<Double, V> l, Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction,
            SpatialTemporalSignal<R> s) {
        if (staticSpace) {
            return computeEverywhereStatic(l, distanceFunction, s);
        } else {
            return computeEverywhereDynamic(l, distanceFunction, s);
        }
    }

    private SpatialTemporalSignal<R> computeEverywhereDynamic(
            LocationService<Double, V> l, Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction, SpatialTemporalSignal<R> s) {
        SpatialTemporalSignal<R> toReturn = new SpatialTemporalSignal<R>(s.getNumberOfLocations());
        if (l.isEmpty()) {
            return toReturn;
        }
        ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
        Iterator<Pair<Double, SpatialModel<V>>> locationServiceIterator = l.times();
        Pair<Double, SpatialModel<V>> current = locationServiceIterator.next();
        Pair<Double, SpatialModel<V>> next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
        double time = cursor.getTime();
        while ((next != null)&&(next.getFirst()<=time)) {
            current = next;
            next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
        }
        //Loop invariant: (current.getFirst()<=time)&&((next==null)||(time<next.getFirst()))
        while (!cursor.completed() && !Double.isNaN(time)) {
            IntFunction<R> spatialSignal = cursor.getValue();
            SpatialModel<V> sm = current.getSecond();
            DistanceStructure<V, ?> f = distanceFunction.apply(sm);
            toReturn.add(time, SpaceUtilities.everywhere(module, spatialSignal, f));
            double nextTime = cursor.forward();
            while ((next != null)&&(next.getFirst()<nextTime)) {
                current = next;
                time = current.getFirst();
                next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
                f = distanceFunction.apply(current.getSecond());
                toReturn.add(time, SpaceUtilities.everywhere(module, spatialSignal, f));
            }
            time = nextTime;
        }
        //TODO: Manage end of signal!
        return toReturn;
    }

    private SpatialTemporalSignal<R> computeEverywhereStatic(
            LocationService<Double, V> l, Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction, SpatialTemporalSignal<R> s) {
        SpatialTemporalSignal<R> toReturn = new SpatialTemporalSignal<R>(s.getNumberOfLocations());
        ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
        double time = cursor.getTime();
        SpatialModel<V> sm = l.get(time);
        DistanceStructure<V, ?> f = distanceFunction.apply(sm);
        while (!cursor.completed() && !Double.isNaN(time)) {
            toReturn.add(time, SpaceUtilities.everywhere(module, cursor.getValue(), f));
            time = cursor.forward();
        }
        //TODO: Manage end of signal!
        return toReturn;
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.ReachFormula, java.lang.Object)
     */
    @Override
    public BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> visit(
            ReachFormula reachFormula, Parameters parameters) {
        Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction = distanceFunctions.get(reachFormula.getDistanceFunctionId());
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> m1 = reachFormula.getFirstArgument().accept(this, parameters);
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> m2 = reachFormula.getSecondArgument().accept(this, parameters);
        if (staticSpace) {
            return (l, s) -> computeReachStatic(l, m1.apply(l, s), distanceFunction, m2.apply(l, s));
        } else {
            return (l, s) -> computeReachDynamic(l, m1.apply(l, s), distanceFunction, m2.apply(l, s));
        }
    }

    private SpatialTemporalSignal<R> computeReachDynamic(LocationService<Double, V> l, SpatialTemporalSignal<R> s1,
                                                         Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction,
                                                         SpatialTemporalSignal<R> s2) {
        SpatialTemporalSignal<R> toReturn = new SpatialTemporalSignal<R>(s1.getNumberOfLocations());
        if (l.isEmpty()) {
            return toReturn;
        }
        ParallelSignalCursor<R> c1 = s1.getSignalCursor(true);
        ParallelSignalCursor<R> c2 = s2.getSignalCursor(true);
        Iterator<Pair<Double, SpatialModel<V>>> locationServiceIterator = l.times();
        Pair<Double, SpatialModel<V>> current = locationServiceIterator.next();
        Pair<Double, SpatialModel<V>> next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
        double time = Math.max(s1.start(), s2.start());
        while ((next != null)&&(next.getFirst()<=time)) {
            current = next;
            next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
        }
        //Loop invariant: (current.getFirst()<=time)&&((next==null)||(time<next.getFirst()))
        c1.move(time);
        c2.move(time);
        while (!c1.completed() && !c2.completed() && !Double.isNaN(time)) {
            IntFunction<R> spatialSignal1 = c1.getValue();
            IntFunction<R> spatialSignal2 = c2.getValue();
            SpatialModel<V> sm = current.getSecond();
            DistanceStructure<V, ?> f = distanceFunction.apply(sm);
            List<R> values =  f.reach(module, spatialSignal1, spatialSignal2);
            toReturn.add(time, (values::get));
            double nextTime = Math.min(c1.nextTime(), c2.nextTime());
            c1.move(time);
            c2.move(time);
            while ((next != null)&&(next.getFirst()<nextTime)) {
                current = next;
                time = current.getFirst();
                next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
                f = distanceFunction.apply(current.getSecond());
                values =  f.reach(module, spatialSignal1, spatialSignal2);
                toReturn.add(time, f.escape(module,(values::get)));
            }
            time = nextTime;
        }
        return toReturn;
    }

    private SpatialTemporalSignal<R> computeReachStatic(LocationService<Double, V> l, SpatialTemporalSignal<R> s1,
                                                        Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction,
                                                        SpatialTemporalSignal<R> s2) {
        SpatialTemporalSignal<R> toReturn = new SpatialTemporalSignal<R>(s1.getNumberOfLocations());
        ParallelSignalCursor<R> c1 = s1.getSignalCursor(true);
        ParallelSignalCursor<R> c2 = s2.getSignalCursor(true);
        double time = Math.max(s1.start(), s2.start());
        c1.move(time);
        c2.move(time);
        SpatialModel<V> sm = l.get(c1.getTime());
        DistanceStructure<V, ?> f = distanceFunction.apply(sm);
        while (!c1.completed() && !c2.completed() && !Double.isNaN(time)) {
            List<R> values = f.reach(module, c1.getValue(), c2.getValue());
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
    public BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> visit(
            EscapeFormula escapeFormula, Parameters parameters) {
        Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction = distanceFunctions.get(escapeFormula.getDistanceFunctionId());
        BiFunction<LocationService<Double, V>, SpatialTemporalSignal<T>, SpatialTemporalSignal<R>> argumentMonitor = escapeFormula.getArgument().accept(this, parameters);
        if (staticSpace) {
            return (l, s) -> computeEscapeStatic(l, distanceFunction, argumentMonitor.apply(l, s));
        } else {
            return (l, s) -> computeEscapeDynamic(l, distanceFunction, argumentMonitor.apply(l, s));
        }
    }

    private SpatialTemporalSignal<R> computeEscapeStatic(LocationService<Double, V> l,
                                                         Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction,
                                                         SpatialTemporalSignal<R> s) {
        SpatialTemporalSignal<R> toReturn = new SpatialTemporalSignal<R>(s.getNumberOfLocations());
        ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
        double time = cursor.getTime();
        SpatialModel<V> sm = l.get(time);
        DistanceStructure<V, ?> f = distanceFunction.apply(sm);
        while (!cursor.completed() && !Double.isNaN(time)) {
            toReturn.add(time, f.escape(module, cursor.getValue()));
            time = cursor.forward();
        }
        //TODO: Manage end of signal!
        return toReturn;
    }

    private SpatialTemporalSignal<R> computeEscapeDynamic(LocationService<Double, V> l,
                                                          Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction,
                                                          SpatialTemporalSignal<R> s) {
        SpatialTemporalSignal<R> toReturn = new SpatialTemporalSignal<R>(s.getNumberOfLocations());
        if (l.isEmpty()) {
            return toReturn;
        }
        ParallelSignalCursor<R> cursor = s.getSignalCursor(true);
        Iterator<Pair<Double, SpatialModel<V>>> locationServiceIterator = l.times();
        Pair<Double, SpatialModel<V>> current = locationServiceIterator.next();
        Pair<Double, SpatialModel<V>> next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
        double time = cursor.getTime();
        while ((next != null)&&(next.getFirst()<=time)) {
            current = next;
            next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
        }
        //Loop invariant: (current.getFirst()<=time)&&((next==null)||(time<next.getFirst()))
        while (!cursor.completed() && !Double.isNaN(time)) {
            IntFunction<R> spatialSignal = cursor.getValue();
            SpatialModel<V> sm = current.getSecond();
            DistanceStructure<V, ?> f = distanceFunction.apply(sm);
            toReturn.add(time, f.escape(module, spatialSignal));
            double nextTime = cursor.forward();
            while ((next != null)&&(next.getFirst()<nextTime)) {
                current = next;
                time = current.getFirst();
                next = (locationServiceIterator.hasNext()?locationServiceIterator.next():null);
                f = distanceFunction.apply(current.getSecond());
                toReturn.add(time, f.escape(module, spatialSignal));
            }
            time = nextTime;
        }
        //TODO: Manage end of signal!
        return toReturn;
    }



}
