package eu.quanticol.moonlight.monitoring.online;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.online.strategy.spacetime.AtomicMonitor;
import eu.quanticol.moonlight.monitoring.online.strategy.spacetime.SomewhereMonitor;
import eu.quanticol.moonlight.monitoring.online.strategy.time.OnlineMonitor;
import eu.quanticol.moonlight.signal.space.DistanceStructure;
import eu.quanticol.moonlight.signal.space.LocationService;
import eu.quanticol.moonlight.signal.space.SpatialModel;
import eu.quanticol.moonlight.signal.online.SignalInterface;
import eu.quanticol.moonlight.signal.online.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class OnlineSpaceTimeMonitor<S, V, R extends Comparable<R>>  implements
    FormulaVisitor<Parameters, OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>>>
{
    private final Formula formula;
    private final SignalDomain<R> interpretation;
    private final Map<String, OnlineMonitor<Double, List<V>,
                                            List<AbstractInterval<R>>>>
                                                                       monitors;
    private final Map<String, Function<V, AbstractInterval<R>>> atoms;

    private final Map<String, Function<SpatialModel<S>,
                                       DistanceStructure<S, ?>>> dist;

    private final LocationService<S> locSvc;

    //TODO: refactor this in some cleaner way
    private final int size;


    public OnlineSpaceTimeMonitor(
            Formula formula,
            int size,
            SignalDomain<R> interpretation,
            LocationService<S> locationService,
            Map<String, Function<V, AbstractInterval<R>>> atomicPropositions,
            Map<String, Function<SpatialModel<S>,
                                 DistanceStructure<S, ?>>> distanceFunctions)
    {
        this.atoms = atomicPropositions;
        this.formula = formula;
        this.interpretation = interpretation;
        this.monitors = new HashMap<>();
        this.dist = distanceFunctions;
        this.locSvc = locationService;
        this.size = size;
    }

    public SignalInterface<Double, List<AbstractInterval<R>>>
    monitor(Update<Double, List<V>> update)
    {
        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> m =
                                    formula.accept(this, null);

        if(update != null)
            m.monitor(update);

        return m.getResult();
    }

    @Override
    public OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> visit(
            AtomicFormula formula, Parameters parameters)
    {
        Function<V, AbstractInterval<R>> f = fetchAtom(formula);

        return monitors.computeIfAbsent(formula.toString(),
                                  x -> new AtomicMonitor<>(f, size, interpretation));

    }


    public OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> visit(
            SomewhereFormula formula, Parameters parameters)
    {
        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> argumentMonitor =
                formula.getArgument().accept(this, parameters);


        Function<SpatialModel<S>, DistanceStructure<S, ?>> distance = dist.get(formula.getDistanceFunctionId());


        return monitors.computeIfAbsent(formula.toString(),
                x -> new SomewhereMonitor<S, V, R>(argumentMonitor, size, locSvc, distance, interpretation));
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
