package eu.quanticol.moonlight.monitoring.online;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.formula.AtomicFormula;
import eu.quanticol.moonlight.formula.Formula;
import eu.quanticol.moonlight.formula.FormulaVisitor;
import eu.quanticol.moonlight.formula.Parameters;
import eu.quanticol.moonlight.monitoring.online.strategy.AtomicMonitor;
import eu.quanticol.moonlight.signal.DistanceStructure;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.online.SignalInterface;
import eu.quanticol.moonlight.signal.online.Update;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class OnlineSpaceTimeMonitor<S, V, R extends Comparable<R>>  implements
    FormulaVisitor<Parameters, OnlineMonitor<Double, V, AbstractInterval<R>>>
{
    private final Formula formula;
    private final SignalDomain<R> interpretation;
    private final Map<String, OnlineMonitor<Double, V, AbstractInterval<R>>>
            monitors;
    private final Map<String, Function<V, AbstractInterval<R>>> atoms;

    private final Map<String, Function<SpatialModel<S>,
                                       DistanceStructure<S, ?>>> dist;


    public OnlineSpaceTimeMonitor(
            Formula formula,
            SignalDomain<R> interpretation,
            Map<String, Function<V, AbstractInterval<R>>> atomicPropositions,
            Map<String, Function<SpatialModel<S>,
                                 DistanceStructure<S, ?>>> distanceFunctions)
    {
        this.atoms = atomicPropositions;
        this.formula = formula;
        this.interpretation = interpretation;
        this.monitors = new HashMap<>();
        this.dist = distanceFunctions;
    }

    public SignalInterface<Double, AbstractInterval<R>>
    monitor(Update<Double, V> update)
    {
        OnlineMonitor<Double, V, AbstractInterval<R>> m =
                                    formula.accept(this, null);

        if(update != null)
            m.monitor(update);

        return m.getResult();
    }

    @Override
    public OnlineMonitor<Double, V, AbstractInterval<R>> visit(
            AtomicFormula formula, Parameters parameters)
    {
        Function<V, AbstractInterval<R>> f = fetchAtom(formula);

        return monitors.computeIfAbsent(formula.toString(),
                                  x -> new AtomicMonitor<>(f, interpretation));

    }

    private Function<V, AbstractInterval<R>> fetchAtom(AtomicFormula f)
    {
        Function<V, AbstractInterval<R>> atom = atoms.get(f.getAtomicId());

        if(atom == null) {
            throw new IllegalArgumentException("Unknown atomic ID " +
                                               f.getAtomicId());
        }
        return atom;
    }

}
