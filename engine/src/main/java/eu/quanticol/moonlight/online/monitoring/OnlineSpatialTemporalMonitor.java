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

import eu.quanticol.moonlight.core.base.Box;
import eu.quanticol.moonlight.core.formula.Formula;
import eu.quanticol.moonlight.core.formula.SpatialFormula;
import eu.quanticol.moonlight.core.formula.UnaryFormula;
import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.core.signal.SpaceTimeSignal;
import eu.quanticol.moonlight.core.space.LocationService;
import eu.quanticol.moonlight.core.space.SpatialModel;

import eu.quanticol.moonlight.formula.classic.AndFormula;
import eu.quanticol.moonlight.formula.classic.NegationFormula;
import eu.quanticol.moonlight.formula.classic.OrFormula;
import eu.quanticol.moonlight.formula.spatial.EscapeFormula;
import eu.quanticol.moonlight.formula.spatial.EverywhereFormula;
import eu.quanticol.moonlight.formula.spatial.ReachFormula;
import eu.quanticol.moonlight.formula.spatial.SomewhereFormula;
import eu.quanticol.moonlight.formula.temporal.*;
import eu.quanticol.moonlight.formula.*;

import eu.quanticol.moonlight.domain.AbsIntervalDomain;
import eu.quanticol.moonlight.domain.ListDomain;

import eu.quanticol.moonlight.online.algorithms.SpatialComputation;
import eu.quanticol.moonlight.online.monitoring.spatialtemporal.*;
import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.online.signal.Update;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

import static eu.quanticol.moonlight.core.algorithms.SpatialAlgorithms.*;

public class OnlineSpatialTemporalMonitor<S, V, R extends Comparable<R>> {
    private final Formula formula;
    private final SignalDomain<R> interpretation;
    private final ListDomain<R> listInterpretation;
    private final Map<String,
                      OnlineMonitor<Double, List<V>, List<Box<R>>>>
                                                     monitors = new HashMap<>();
    private final Map<String, Function<V, Box<R>>> atoms;

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
            Map<String, Function<V, Box<R>>> atomicPropositions,
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
            Map<String, Function<V, Box<R>>> atomicPropositions,
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

    public SpaceTimeSignal<Double, Box<R>>
    monitor(@NotNull Update<Double, List<V>> update)
    {
        var m = monitor(formula);

        if(update.getValue().size() != size)
            throw new IllegalArgumentException("The update doesn't match the " +
                                               "expected size of the signal");

        m.monitor(update);

        return (SpaceTimeSignal<Double, Box<R>>) m.getResult();
    }

    public SpaceTimeSignal<Double, Box<R>>
    monitor(@NotNull TimeChain<Double, List<V>> updates)
    {
        var m = monitor(formula);
        m.monitor(updates);
        return (SpaceTimeSignal<Double, Box<R>>) m.getResult();
    }

    private OnlineMonitor<Double, List<V>, List<Box<R>>> monitor(Formula f) {
        return switch(f) {
            // Classic operators
            case AtomicFormula atomic -> generateAtomicMonitor(atomic);
            case NegationFormula negation -> generateNegationMonitor(negation);
            case AndFormula and -> generateAndMonitor(and);
            case OrFormula or -> generateOrMonitor(or);
            // Temporal Future Operators
            case EventuallyFormula ev -> generateEventuallyMonitor(ev);
            case GloballyFormula globally -> generateGloballyMonitor(globally);
//            case UntilFormula until -> generateUntilMonitor(until);
            // Temporal Past Operators
//            case OnceFormula once -> generateOnceMonitor(once);
//            case HistoricallyFormula hs -> generateHistoricallyMonitor(hs);
//            case SinceFormula since -> generateSinceMonitor(since);
            // Spatial Operators
            case SomewhereFormula some -> unarySpace(some, this::somewhereOp);
            case EverywhereFormula every -> unarySpace(every, this::everywhereOp);
            case EscapeFormula escape -> unarySpace(escape, this::escapeOp);
//            case ReachFormula reach -> generateReachMonitor(reach);
            default -> illegalFormula(f);
        };
    }

    private  OnlineMonitor<Double, List<V>, List<Box<R>>> illegalFormula(Formula f) {
        throw new IllegalArgumentException("Unsupported formula: " + f);
    }

    public OnlineMonitor<Double, List<V>, List<Box<R>>> generateAtomicMonitor(
            AtomicFormula formula)
    {
        Function<V, Box<R>> f = fetchAtom(formula);

        return monitors.computeIfAbsent(formula.toString(),
                             x -> new AtomicMonitor<>(f, size, interpretation));
    }

    public OnlineMonitor<Double, List<V>, List<Box<R>>> generateNegationMonitor(
            NegationFormula formula)
    {
        var argMonitor = monitor(formula.getArgument());

        return monitors.computeIfAbsent(formula.toString(),
                x -> new UnaryMonitor<>(argMonitor,
                        listInterpretation::negation, interpretation, size));
    }

    public OnlineMonitor<Double, List<V>, List<Box<R>>> generateAndMonitor(
            AndFormula formula)
    {
        var firstArg = monitor(formula.getFirstArgument());

        var secondArg = monitor(formula.getSecondArgument());

        return monitors.computeIfAbsent(formula.toString(),
                x -> new BinaryMonitor<>(firstArg, secondArg,
                        listInterpretation::conjunction,
                        interpretation, size));
    }

    public OnlineMonitor<Double, List<V>, List<Box<R>>> generateOrMonitor(
            OrFormula formula)
    {
        var firstArg = monitor(formula.getFirstArgument());

        var secondArg = monitor(formula.getSecondArgument());

        return monitors.computeIfAbsent(formula.toString(),
                x -> new BinaryMonitor<>(firstArg, secondArg,
                        listInterpretation::disjunction,
                        interpretation, size));
    }

    public OnlineMonitor<Double, List<V>, List<Box<R>>> generateEventuallyMonitor(
            EventuallyFormula formula)
    {
        var argMonitor = monitor(formula.getArgument());

        return monitors.computeIfAbsent(formula.toString(),
                x ->  new UnaryTimeOpMonitor<>(argMonitor,
                        listInterpretation::disjunction,
                        formula.getInterval(),
                        interpretation, size));
    }

    public OnlineMonitor<Double, List<V>, List<Box<R>>> generateGloballyMonitor(
            GloballyFormula formula)
    {
        var argMonitor = monitor(formula.getArgument());

        return monitors.computeIfAbsent(formula.toString(),
                x ->  new UnaryTimeOpMonitor<>(argMonitor,
                        listInterpretation::conjunction,
                        formula.getInterval(),
                        interpretation, size));
    }

    public OnlineMonitor<Double, List<V>, List<Box<R>>> generateReachMonitor(
            ReachFormula formula)
    {
        var firstArg = monitor(formula.getFirstArgument());
        var secondArg = monitor(formula.getSecondArgument());

        return monitors.computeIfAbsent(formula.toString(),
                x -> new BinarySpaceOpMonitor<>(firstArg, secondArg,
                        null,
                        interpretation, size));
    }

    private OnlineMonitor<Double, List<V>, List<Box<R>>>
    unarySpace(UnaryFormula f,
               BiFunction<IntFunction<Box<R>>,
                       DistanceStructure<S, ?>, List<Box<R>>> op)
    {
        var argMonitor = monitor(f.getArgument());

        String distF = ((SpatialFormula) f).getDistanceFunctionId();

        Function<SpatialModel<S>, DistanceStructure<S, ?>> d = dist.get(distF);

        SpatialComputation<Double, S, Box<R>> sc =
                new SpatialComputation<>(locSvc, d, op);

        return monitors.computeIfAbsent(formula.toString(),
                x -> new UnarySpaceOpMonitor<>(argMonitor,
                        sc, interpretation, size));

    }

    private List<Box<R>> everywhereOp(
            IntFunction<Box<R>> spatialSignal,
            DistanceStructure<S, ?> ds)
    {
        if(parallel)
            return everywhereParallel(new AbsIntervalDomain<>(interpretation),
                                      spatialSignal, ds);
        return everywhere(new AbsIntervalDomain<>(interpretation),
                          spatialSignal, ds);
    }

    private List<Box<R>> somewhereOp(
            IntFunction<Box<R>> spatialSignal,
            DistanceStructure<S, ?> ds)
    {
        if(parallel)
            return somewhereParallel(new AbsIntervalDomain<>(interpretation),
                                     spatialSignal, ds);

        return somewhere(new AbsIntervalDomain<>(interpretation),
                         spatialSignal, ds);

    }

    private List<Box<R>> escapeOp(
            IntFunction<Box<R>> spatialSignal,
            DistanceStructure<S, ?> f)
    {
        return escape(new AbsIntervalDomain<>(interpretation),
                      spatialSignal, f);
    }

    private Function<V, Box<R>> fetchAtom(AtomicFormula f)
    {
        Function<V, Box<R>> atom = atoms.get(f.getAtomicId());

        if(atom == null) {
            throw new IllegalArgumentException("Unknown atomic ID " +
                    f.getAtomicId());
        }
        return atom;
    }

}
