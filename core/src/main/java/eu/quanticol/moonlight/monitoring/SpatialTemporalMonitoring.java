/**
 *
 */
package eu.quanticol.moonlight.monitoring;

import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.SpatialModel;

import java.util.Map;
import java.util.function.Function;

/**
 *
 */
public class SpatialTemporalMonitoring<V, T, R> implements
        FormulaVisitor<Parameters, SpatialTemporalMonitor<V,T,R>> {

    private final Map<String, Function<Parameters, Function<T, R>>> atomicPropositions;

    private final Map<String, Function<SpatialModel<V>, DistanceStructure<V, ?>>> distanceFunctions;

    private final SignalDomain<R> module;

    private final boolean staticSpace;


    public SpatialTemporalMonitor<V,T,R> monitor(Formula f, Parameters parameters) {
        return f.accept(this, parameters);
    }

    /**
     * @param atomicPropositions
     * @param module
     */
    public SpatialTemporalMonitoring(
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
    public SpatialTemporalMonitor<V,T,R> visit(
            AtomicFormula atomicFormula, Parameters parameters) {
        Function<Parameters, Function<T, R>> f = atomicPropositions.get(atomicFormula.getAtomicId());
        if (f == null) {
            throw new IllegalArgumentException("Unkown atomic ID " + atomicFormula.getAtomicId());
        }
        Function<T, R> atomic = f.apply(parameters);
        return SpatialTemporalMonitor.atomicMonitor(atomic);
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.AndFormula, java.lang.Object)
     */
    @Override
    public SpatialTemporalMonitor<V,T,R> visit(AndFormula andFormula, Parameters parameters) {
        SpatialTemporalMonitor<V,T,R> leftMonitoring = andFormula.getFirstArgument().accept(this, parameters);
        SpatialTemporalMonitor<V,T,R> rightMonitoring = andFormula.getSecondArgument().accept(this, parameters);
        return SpatialTemporalMonitor.andMonitor(leftMonitoring, module, rightMonitoring);
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.NegationFormula, java.lang.Object)
     */
    @Override
    public SpatialTemporalMonitor<V,T,R> visit(
            NegationFormula negationFormula, Parameters parameters) {
        SpatialTemporalMonitor<V,T,R> m = negationFormula.getArgument().accept(this, parameters);
        return SpatialTemporalMonitor.notMonitor(m, module);
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.OrFormula, java.lang.Object)
     */
    @Override
    public SpatialTemporalMonitor<V,T,R> visit(OrFormula orFormula, Parameters parameters) {
        SpatialTemporalMonitor<V,T,R> leftMonitoring = orFormula.getFirstArgument().accept(this, parameters);
        SpatialTemporalMonitor<V,T,R> rightMonitoring = orFormula.getSecondArgument().accept(this, parameters);
        return SpatialTemporalMonitor.orMonitor(leftMonitoring, module, rightMonitoring);
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.EventuallyFormula, java.lang.Object)
     */
    @Override
    public SpatialTemporalMonitor<V,T,R> visit(
            EventuallyFormula eventuallyFormula, Parameters parameters) {
        SpatialTemporalMonitor<V,T,R> m = eventuallyFormula.getArgument().accept(this, parameters);
        return SpatialTemporalMonitor.eventuallyMonitor(m,eventuallyFormula.getInterval(),module);
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.GloballyFormula, java.lang.Object)
     */
    @Override
    public SpatialTemporalMonitor<V,T,R> visit(
            GloballyFormula globallyFormula, Parameters parameters) {
        SpatialTemporalMonitor<V,T,R> m = globallyFormula.getArgument().accept(this, parameters);
        return SpatialTemporalMonitor.globallyMonitor(m, globallyFormula.getInterval(),module);
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.UntilFormula, java.lang.Object)
     */
    @Override
    public SpatialTemporalMonitor<V,T,R> visit(
            UntilFormula untilFormula, Parameters parameters) {
        SpatialTemporalMonitor<V,T,R> firstMonitoring = untilFormula.getFirstArgument().accept(this, parameters);
        SpatialTemporalMonitor<V,T,R> secondMonitoring = untilFormula.getSecondArgument().accept(this, parameters);
        return SpatialTemporalMonitor.untilMonitor(firstMonitoring, untilFormula.getInterval(), secondMonitoring, module);
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.SinceFormula, java.lang.Object)
     */
    @Override
    public SpatialTemporalMonitor<V,T,R> visit(
            SinceFormula sinceFormula, Parameters parameters) {
        SpatialTemporalMonitor<V,T,R> firstMonitoring = sinceFormula.getFirstArgument().accept(this, parameters);
        SpatialTemporalMonitor<V,T,R> secondMonitoring = sinceFormula.getSecondArgument().accept(this, parameters);
        return SpatialTemporalMonitor.sinceMonitor(firstMonitoring, sinceFormula.getInterval(), secondMonitoring, module);
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.HistoricallyFormula, java.lang.Object)
     */
    @Override
    public SpatialTemporalMonitor<V,T,R> visit(
            HistoricallyFormula historicallyFormula, Parameters parameters) {
        SpatialTemporalMonitor<V,T,R> argumentMonitoring = historicallyFormula.getArgument().accept(this, parameters);
        return SpatialTemporalMonitor.historicallyMonitor(argumentMonitoring, historicallyFormula.getInterval(),module);
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.OnceFormula, java.lang.Object)
     */
    @Override
    public SpatialTemporalMonitor<V,T,R> visit(
            OnceFormula onceFormula, Parameters parameters) {
        SpatialTemporalMonitor<V,T,R> argumentMonitoring = onceFormula.getArgument().accept(this, parameters);
        return SpatialTemporalMonitor.onceMonitor(argumentMonitoring, onceFormula.getInterval(), module);
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.SomewhereFormula, java.lang.Object)
     */
    @Override
    public SpatialTemporalMonitor<V,T,R> visit(
            SomewhereFormula somewhereFormula, Parameters parameters) {
        Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction = distanceFunctions.get(somewhereFormula.getDistanceFunctionId());
        SpatialTemporalMonitor<V,T,R> argumentMonitor = somewhereFormula.getArgument().accept(this, parameters);
        return SpatialTemporalMonitor.somewhereMonitor(argumentMonitor, distanceFunction, module);
    }

    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.EverywhereFormula, java.lang.Object)
     */
    @Override
    public SpatialTemporalMonitor<V,T,R> visit(
            EverywhereFormula everywhereFormula, Parameters parameters) {
        Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction = distanceFunctions.get(everywhereFormula.getDistanceFunctionId());
        SpatialTemporalMonitor<V,T,R> argumentMonitor = everywhereFormula.getArgument().accept(this, parameters);
        return SpatialTemporalMonitor.everywhereMonitor(argumentMonitor, distanceFunction, module);
    }


    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.ReachFormula, java.lang.Object)
     */
    @Override
    public SpatialTemporalMonitor<V,T,R> visit(
            ReachFormula reachFormula, Parameters parameters) {
        Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction = distanceFunctions.get(reachFormula.getDistanceFunctionId());
        SpatialTemporalMonitor<V,T,R> m1 = reachFormula.getFirstArgument().accept(this, parameters);
        SpatialTemporalMonitor<V,T,R> m2 = reachFormula.getSecondArgument().accept(this, parameters);
        return SpatialTemporalMonitor.reachMonitor(m1, distanceFunction, m2, module);
    }


    /* (non-Javadoc)
     * @see eu.quanticol.moonlight.formula.FormulaVisitor#visit(eu.quanticol.moonlight.formula.EscapeFormula, java.lang.Object)
     */
    @Override
    public SpatialTemporalMonitor<V,T,R> visit(
            EscapeFormula escapeFormula, Parameters parameters) {
        Function<SpatialModel<V>, DistanceStructure<V, ?>> distanceFunction = distanceFunctions.get(escapeFormula.getDistanceFunctionId());
        SpatialTemporalMonitor<V,T,R> argumentMonitor = escapeFormula.getArgument().accept(this, parameters);
        return SpatialTemporalMonitor.escapeMonitor(argumentMonitor, distanceFunction, module);
    }




}
