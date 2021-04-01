package eu.quanticol.moonlight.monitoring.online;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.ListDomain;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.online.strategy.spacetime.AtomicMonitor;
import eu.quanticol.moonlight.monitoring.online.strategy.spacetime.EscapeMonitor;
import eu.quanticol.moonlight.monitoring.online.strategy.spacetime.EverywhereMonitor;
import eu.quanticol.moonlight.monitoring.online.strategy.spacetime.SomewhereMonitor;
import eu.quanticol.moonlight.monitoring.online.strategy.time.OnlineMonitor;
import eu.quanticol.moonlight.monitoring.online.strategy.spacetime.TemporalOpMonitor;
import eu.quanticol.moonlight.monitoring.online.strategy.spacetime.UnaryMonitor;
import eu.quanticol.moonlight.signal.online.SpaceTimeSignal;
import eu.quanticol.moonlight.space.DistanceStructure;
import eu.quanticol.moonlight.space.LocationService;
import eu.quanticol.moonlight.space.SpatialModel;
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
    private final ListDomain<R> listInterpretation;
    private final Map<String,
                      OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>>>
                                                     monitors = new HashMap<>();
    private final Map<String, Function<V, AbstractInterval<R>>> atoms;

    private final Map<String, Function<SpatialModel<S>,
                                       DistanceStructure<S, ?>>> dist;

    private final LocationService<Double, S> locSvc;

    //TODO: refactor this in some cleaner way
    private final int size;


    public OnlineSpaceTimeMonitor(
            Formula formula,
            int size,
            SignalDomain<R> interpretation,
            LocationService<Double, S> locationService,
            Map<String, Function<V, AbstractInterval<R>>> atomicPropositions,
            Map<String, Function<SpatialModel<S>,
                                 DistanceStructure<S, ?>>> distanceFunctions)
    {
        this.atoms = atomicPropositions;
        this.formula = formula;
        this.interpretation = interpretation;
        this.dist = distanceFunctions;
        this.locSvc = locationService;
        this.size = size;
        this.listInterpretation = new ListDomain<>(size, interpretation);
    }

    public SpaceTimeSignal<Double, AbstractInterval<R>>
    monitor(Update<Double, List<V>> update)
    {
        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> m =
                                    formula.accept(this, null);

        if(update != null)
            m.monitor(update);

        return (SpaceTimeSignal<Double, AbstractInterval<R>>) m.getResult();
    }

    @Override
    public OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> visit(
            AtomicFormula formula, Parameters parameters)
    {
        Function<V, AbstractInterval<R>> f = fetchAtom(formula);

        return monitors.computeIfAbsent(formula.toString(),
                             x -> new AtomicMonitor<>(f, size, interpretation));
    }

    @Override
    public OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> visit(
            NegationFormula formula, Parameters parameters)
    {
        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> argumentMonitor =
                formula.getArgument().accept(this, parameters);

        return monitors.computeIfAbsent(formula.toString(),
                x -> new UnaryMonitor<>(argumentMonitor,
                        listInterpretation::negation, interpretation, size));
    }


    @Override
    public OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> visit(
            GloballyFormula formula, Parameters parameters)
    {
        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> argMonitor =
                formula.getArgument().accept(this, parameters);

        return monitors.computeIfAbsent(formula.toString(),
                x ->  new TemporalOpMonitor<>(argMonitor,
                        listInterpretation::conjunction,
                        formula.getInterval(),
                        interpretation, size));
    }

    @Override
    public OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> visit(
            SomewhereFormula formula, Parameters parameters)
    {
        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>>
                argumentMonitor = formula.getArgument()
                                         .accept(this, parameters);

        Function<SpatialModel<S>, DistanceStructure<S, ?>> distance =
                                      dist.get(formula.getDistanceFunctionId());

        return monitors.computeIfAbsent(formula.toString(),
                x -> new SomewhereMonitor<>(argumentMonitor, locSvc,
                                            distance, interpretation));
    }

    @Override
    public OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> visit(
            EverywhereFormula formula, Parameters parameters)
    {
        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>>
                argumentMonitor = formula.getArgument()
                                         .accept(this, parameters);

        Function<SpatialModel<S>, DistanceStructure<S, ?>> distance =
                                      dist.get(formula.getDistanceFunctionId());

        return monitors.computeIfAbsent(formula.toString(),
                x -> new EverywhereMonitor<>(argumentMonitor, locSvc,
                                             distance, interpretation));
    }

    @Override
    public OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> visit(
            EscapeFormula formula, Parameters parameters)
    {
        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>>
                argumentMonitor = formula.getArgument()
                .accept(this, parameters);

        Function<SpatialModel<S>, DistanceStructure<S, ?>> distance =
                dist.get(formula.getDistanceFunctionId());

        return monitors.computeIfAbsent(formula.toString(),
                x -> new EscapeMonitor<>(argumentMonitor, locSvc,
                        distance, interpretation));
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
