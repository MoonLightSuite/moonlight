package eu.quanticol.moonlight.online.monitoring;

import eu.quanticol.moonlight.core.formula.Formula;
import eu.quanticol.moonlight.core.formula.FormulaVisitor;
import eu.quanticol.moonlight.core.base.AbstractInterval;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.formula.classic.AndFormula;
import eu.quanticol.moonlight.formula.classic.NegationFormula;
import eu.quanticol.moonlight.formula.classic.OrFormula;
import eu.quanticol.moonlight.formula.temporal.EventuallyFormula;
import eu.quanticol.moonlight.formula.temporal.GloballyFormula;
import eu.quanticol.moonlight.offline.monitoring.TemporalMonitoring;
import eu.quanticol.moonlight.online.monitoring.monitoring.temporal.*;
import eu.quanticol.moonlight.offline.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.online.monitoring.temporal.AtomicMonitor;
import eu.quanticol.moonlight.online.monitoring.temporal.BinaryMonitor;
import eu.quanticol.moonlight.online.monitoring.temporal.TemporalOpMonitor;
import eu.quanticol.moonlight.online.monitoring.temporal.UnaryMonitor;
import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.core.signal.TimeSignal;
import eu.quanticol.moonlight.online.signal.Update;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Primary entry point to perform online monitoring.
 * Based on a visitor design pattern over the formula tree,
 * just like {@link TemporalMonitoring}.
 *
 * @param <V> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 * @see TemporalMonitoring
 * @see FormulaVisitor
 * @see TemporalMonitor
 */
public class OnlineTimeMonitor<V, R extends Comparable<R>> implements
    FormulaVisitor<Parameters, OnlineMonitor<Double, V, AbstractInterval<R>>>
{

    private final Formula formula;
    private final SignalDomain<R> interpretation;
    private final Map<String, OnlineMonitor<Double, V, AbstractInterval<R>>>
                                                                       monitors;
    private final Map<String, Function<V, AbstractInterval<R>>> atoms;

    public OnlineTimeMonitor(
        Formula formula,
        SignalDomain<R> interpretation,
        Map<String, Function<V, AbstractInterval<R>>> atomicPropositions)
    {
        this.atoms = atomicPropositions;
        this.formula = formula;
        this.interpretation = interpretation;
        this.monitors = new HashMap<>();
    }

    public TimeSignal<Double, AbstractInterval<R>>
    monitor(Update<Double, V> update)
    {
        UpdateParameter<Double, V> param = new UpdateParameter<>(update);

        OnlineMonitor<Double, V, AbstractInterval<R>> m =
                                        formula.accept(this, param);

        if(update != null)
            m.monitor(update);

        return m.getResult();
    }

    public TimeSignal<Double, AbstractInterval<R>>
    monitor(TimeChain<Double, V> updates)
    {

        OnlineMonitor<Double, V, AbstractInterval<R>> m =
                formula.accept(this, null);

        if(updates != null)
            m.monitor(updates);

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

    @Override
    public OnlineMonitor<Double, V, AbstractInterval<R>> visit(
            NegationFormula formula, Parameters parameters)
    {
        OnlineMonitor<Double, V, AbstractInterval<R>> argumentMonitor =
                formula.getArgument().accept(this, parameters);

        return monitors.computeIfAbsent(formula.toString(),
                x -> new UnaryMonitor<>(argumentMonitor,
                        v -> negation(v, interpretation), interpretation));
    }


    @Override
    public OnlineMonitor<Double, V, AbstractInterval<R>> visit(
            AndFormula formula, Parameters parameters)
    {
        OnlineMonitor<Double, V, AbstractInterval<R>> firstArgMonitor =
                formula.getFirstArgument().accept(this, parameters);

        OnlineMonitor<Double, V, AbstractInterval<R>> secondArgMonitor =
                formula.getSecondArgument().accept(this, parameters);

        return monitors.computeIfAbsent(formula.toString(),
                x -> new BinaryMonitor<>(firstArgMonitor, secondArgMonitor,
                        (v1, v2) -> conjunction(v1, v2, interpretation),
                                                        interpretation));
    }

    @Override
    public OnlineMonitor<Double, V, AbstractInterval<R>> visit(
            OrFormula formula, Parameters parameters)
    {
        OnlineMonitor<Double, V, AbstractInterval<R>> firstArgMonitor =
                formula.getFirstArgument().accept(this, parameters);

        OnlineMonitor<Double, V, AbstractInterval<R>> secondArgMonitor =
                formula.getSecondArgument().accept(this, parameters);

        return monitors.computeIfAbsent(formula.toString(),
                x ->  new BinaryMonitor<>(firstArgMonitor, secondArgMonitor,
                        (v1, v2) -> disjunction(v1, v2, interpretation),
                        interpretation));
    }


    @Override
    public OnlineMonitor<Double, V, AbstractInterval<R>> visit(
            EventuallyFormula formula, Parameters parameters)
    {
        OnlineMonitor<Double, V, AbstractInterval<R>> argumentMonitor =
                formula.getArgument().accept(this, parameters);

        return monitors.computeIfAbsent(formula.toString(),
                x ->  new TemporalOpMonitor<>(argumentMonitor,
                        (v1, v2) -> disjunction(v1, v2, interpretation),
                                                        formula.getInterval(),
                                                        interpretation));
    }

    @Override
    public OnlineMonitor<Double, V, AbstractInterval<R>> visit(
            GloballyFormula formula, Parameters parameters)
    {
        OnlineMonitor<Double, V, AbstractInterval<R>> argumentMonitor =
                formula.getArgument().accept(this, parameters);

        return monitors.computeIfAbsent(formula.toString(),
                x ->  new TemporalOpMonitor<>(argumentMonitor,
                        (v1, v2) -> conjunction(v1, v2, interpretation),
                                                        formula.getInterval(),
                                                        interpretation));
    }

    private AbstractInterval<R> negation(AbstractInterval<R> value,
                                         SignalDomain<R> domain)
    {
        return new AbstractInterval<>(domain.negation(value.getEnd()),
                                      domain.negation(value.getStart()));
    }

    private AbstractInterval<R> conjunction(AbstractInterval<R> fstVal,
                                            AbstractInterval<R> sndVal,
                                            SignalDomain<R> domain)
    {
        return new AbstractInterval<>(
                domain.conjunction(fstVal.getStart(), sndVal.getStart()),
                domain.conjunction(fstVal.getEnd(), sndVal.getEnd()));
    }

    private AbstractInterval<R> disjunction(AbstractInterval<R> fstVal,
                                            AbstractInterval<R> sndVal,
                                            SignalDomain<R> domain)
    {
        return new AbstractInterval<>(
                domain.disjunction(fstVal.getStart(), sndVal.getStart()),
                domain.disjunction(fstVal.getEnd(), sndVal.getEnd()));
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
