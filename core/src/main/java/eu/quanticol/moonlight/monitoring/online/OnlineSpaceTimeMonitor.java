/*
 * MoonLight: a light-weight framework for runtime monitoring
 * Copyright (C) 2018
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

package eu.quanticol.moonlight.monitoring.online;

import eu.quanticol.moonlight.algorithms.online.SpatialComputation;
import eu.quanticol.moonlight.domain.AbsIntervalDomain;
import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.ListDomain;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.online.strategy.spacetime.AtomicMonitor;
import eu.quanticol.moonlight.monitoring.online.strategy.spacetime.UnarySpaceOpMonitor;
import eu.quanticol.moonlight.monitoring.online.strategy.time.OnlineMonitor;
import eu.quanticol.moonlight.monitoring.online.strategy.spacetime.UnaryTimeOpMonitor;
import eu.quanticol.moonlight.monitoring.online.strategy.spacetime.UnaryMonitor;
import eu.quanticol.moonlight.signal.online.SpaceTimeSignal;
import eu.quanticol.moonlight.space.DistanceStructure;
import eu.quanticol.moonlight.space.LocationService;
import eu.quanticol.moonlight.space.SpatialModel;
import eu.quanticol.moonlight.signal.online.Update;


import static eu.quanticol.moonlight.space.DistanceStructure.somewhere;
import static eu.quanticol.moonlight.space.DistanceStructure.everywhere;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class OnlineSpaceTimeMonitor<S, V, R extends Comparable<R>>  implements
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
        OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>> argMonitor =
                formula.getArgument().accept(this, parameters);

        return monitors.computeIfAbsent(formula.toString(),
                x -> new UnaryMonitor<>(argMonitor,
                        listInterpretation::negation, interpretation, size));
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

    private OnlineMonitor<Double, List<V>, List<AbstractInterval<R>>>
    unarySpace(UnaryFormula f,
               Parameters p,
               BiFunction<Function<Integer, AbstractInterval<R>>,
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
            Function<Integer, AbstractInterval<R>> spatialSignal,
            DistanceStructure<S, ?> ds)
    {
        return everywhere(new AbsIntervalDomain<>(interpretation),
                          spatialSignal, ds);
    }

    private List<AbstractInterval<R>> somewhereOp(
            Function<Integer, AbstractInterval<R>> spatialSignal,
            DistanceStructure<S, ?> ds)
    {
        return somewhere(new AbsIntervalDomain<>(interpretation),
                         spatialSignal, ds);
    }

    private List<AbstractInterval<R>> escapeOp(
            Function<Integer, AbstractInterval<R>> spatialSignal,
            DistanceStructure<S, ?> f)
    {
        return f.escape(new AbsIntervalDomain<>(interpretation), spatialSignal);
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
