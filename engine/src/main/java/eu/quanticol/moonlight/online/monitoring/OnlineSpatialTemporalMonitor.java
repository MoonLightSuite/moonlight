/*
 * MoonLight: a light-weight framework for runtime monitoring
 * Copyright (C) 2018-2021
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.quanticol.moonlight.online.monitoring;

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.online.algorithms.SpatialComputation;
import eu.quanticol.moonlight.domain.AbsIntervalDomain;
import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.ListDomain;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.online.monitoring.strategy.spacetime.*;
import eu.quanticol.moonlight.online.signal.SpaceTimeSignal;
import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;
import eu.quanticol.moonlight.online.signal.Update;
import org.jetbrains.annotations.NotNull;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

import static eu.quanticol.moonlight.algorithms.SpaceUtilities.*;

public class OnlineSpatialTemporalMonitor<S, V, R extends Comparable<R>>  implements
FormulaVisitor<Parameters,
               OnlineMonitor<Double, List<V>,
               List<AbstractInterval<R>>>>
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

    private final boolean parallel;
    private final int size;


    public OnlineSpatialTemporalMonitor(
            Formula formula,
            int size,
            SignalDomain<R> interpretation,
            LocationService<Double, S> locationService,
            Map<String, Function<V, AbstractInterval<R>>> atomicPropositions,
            Map<String, Function<SpatialModel<S>,
                    DistanceStructure<S, ?>>> distanceFunctions)
    {
        this(formula, size, interpretation, locationService,
             atomicPropositions, distanceFunctions, false);
    }

    public OnlineSpatialTemporalMonitor(
            Formula formula,
            int size,
            SignalDomain<R> interpretation,
            LocationService<Double, S> locationService,
            Map<String, Function<V, AbstractInterval<R>>> atomicPropositions,
            Map<String, Function<SpatialModel<S>,
                    DistanceStructure<S, ?>>> distanceFunctions,
            boolean parallel)
    {
        this.atoms = atomicPropositions;
        this.formula = formula;
        this.interpretation = interpretation;
        this.dist = distanceFunctions;
        this.locSvc = locationService;
        this.size = size;
        this.listInterpretation = new ListDomain<>(size, interpretation);
        this.parallel = parallel;
    }

    public SpaceTimeSignal<Double, AbstractInterval<R>>
    monitor(@NotNull Update<Double, List<V>> update)
    {
        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> m =
                                    formula.accept(this, null);

        if(update.getValue().size() != size)
            throw new IllegalArgumentException("The update doesn't match the " +
                                               "expected size of the signal");

        m.monitor(update);

        return (SpaceTimeSignal<Double, AbstractInterval<R>>) m.getResult();
    }

    public SpaceTimeSignal<Double, AbstractInterval<R>>
    monitor(TimeChain<Double, List<V>> updates)
    {
        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> m =
                formula.accept(this, null);

        if(updates != null)
            m.monitor(updates);

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
        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> argMonitor =
                formula.getArgument().accept(this, parameters);

        return monitors.computeIfAbsent(formula.toString(),
                x -> new UnaryMonitor<>(argMonitor,
                        listInterpretation::negation, interpretation, size));
    }

    @Override
    public OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> visit(
            AndFormula formula, Parameters parameters)
    {
        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> firstArg =
                formula.getFirstArgument().accept(this, parameters);

        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> secondArg =
                formula.getSecondArgument().accept(this, parameters);

        return monitors.computeIfAbsent(formula.toString(),
                x -> new BinaryMonitor<>(firstArg, secondArg,
                        listInterpretation::conjunction,
                        interpretation, size));
    }

    @Override
    public OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> visit(
            OrFormula formula, Parameters parameters)
    {
        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> firstArg =
                formula.getFirstArgument().accept(this, parameters);

        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> secondArg =
                formula.getSecondArgument().accept(this, parameters);

        return monitors.computeIfAbsent(formula.toString(),
                x -> new BinaryMonitor<>(firstArg, secondArg,
                        listInterpretation::disjunction,
                        interpretation, size));
    }

    @Override
    public OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> visit(
            EventuallyFormula formula, Parameters parameters)
    {
        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> argMonitor =
                formula.getArgument().accept(this, parameters);

        return monitors.computeIfAbsent(formula.toString(),
                x ->  new UnaryTimeOpMonitor<>(argMonitor,
                        listInterpretation::disjunction,
                        formula.getInterval(),
                        interpretation, size));
    }


    @Override
    public OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> visit(
            GloballyFormula formula, Parameters parameters)
    {
        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> argMonitor =
                formula.getArgument().accept(this, parameters);

        return monitors.computeIfAbsent(formula.toString(),
                x ->  new UnaryTimeOpMonitor<>(argMonitor,
                        listInterpretation::conjunction,
                        formula.getInterval(),
                        interpretation, size));
    }

    @Override
    public OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> visit(
            SomewhereFormula formula, Parameters parameters)
    {
        return unarySpace(formula, parameters, this::somewhereOp);
    }

    @Override
    public OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> visit(
            EverywhereFormula formula, Parameters parameters)
    {
        return unarySpace(formula, parameters, this::everywhereOp);
    }

    @Override
    public OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> visit(
            EscapeFormula formula, Parameters parameters)
    {
        return unarySpace(formula, parameters, this::escapeOp);
    }

    @Override
    public OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> visit(
            ReachFormula formula, Parameters parameters)
    {
        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> firstArg =
                formula.getFirstArgument().accept(this, parameters);

        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> secondArg =
                formula.getSecondArgument().accept(this, parameters);

        return monitors.computeIfAbsent(formula.toString(),
                x -> new BinarySpaceOpMonitor<>(firstArg, secondArg,
                        null,
                        interpretation, size));
    }

    private OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>>
    unarySpace(UnaryFormula f,
               Parameters p,
               BiFunction<IntFunction<AbstractInterval<R>>,
                       DistanceStructure<S, ?>, List<AbstractInterval<R>>> op)
    {
        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>>
                argMonitor = f.getArgument().accept(this, p);

        String distF = ((SpatialFormula) f).getDistanceFunctionId();

        Function<SpatialModel<S>, DistanceStructure<S, ?>> d = dist.get(distF);

        SpatialComputation<Double, S, AbstractInterval<R>> sc =
                new SpatialComputation<>(locSvc, d, op);

        return monitors.computeIfAbsent(formula.toString(),
                x -> new UnarySpaceOpMonitor<>(argMonitor,
                        sc, interpretation, size));

    }

    private List<AbstractInterval<R>> everywhereOp(
            IntFunction<AbstractInterval<R>> spatialSignal,
            DistanceStructure<S, ?> ds)
    {
        if(parallel)
            return everywhereParallel(new AbsIntervalDomain<>(interpretation),
                                      spatialSignal, ds);
        return everywhere(new AbsIntervalDomain<>(interpretation),
                          spatialSignal, ds);
    }

    private List<AbstractInterval<R>> somewhereOp(
            IntFunction<AbstractInterval<R>> spatialSignal,
            DistanceStructure<S, ?> ds)
    {
        if(parallel)
            return somewhereParallel(new AbsIntervalDomain<>(interpretation),
                                     spatialSignal, ds);

        return somewhere(new AbsIntervalDomain<>(interpretation),
                         spatialSignal, ds);

    }

    private List<AbstractInterval<R>> escapeOp(
            IntFunction<AbstractInterval<R>> spatialSignal,
            DistanceStructure<S, ?> f)
    {
        return escape(new AbsIntervalDomain<>(interpretation),
                      spatialSignal, f);
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
