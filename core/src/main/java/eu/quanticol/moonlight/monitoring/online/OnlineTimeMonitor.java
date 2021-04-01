package eu.quanticol.moonlight.monitoring.online;

import eu.quanticol.moonlight.domain.AbstractInterval;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.formula.*;
import eu.quanticol.moonlight.monitoring.TemporalMonitoring;
import eu.quanticol.moonlight.monitoring.online.strategy.time.*;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.online.TimeSignal;
import eu.quanticol.moonlight.signal.online.Update;

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

    private AbstractInterval<R> conjunction(AbstractInterval<R> firstValue,
                                            AbstractInterval<R> secondValue,
                                            SignalDomain<R> domain)
    {
        return new AbstractInterval<>(
                domain.conjunction(firstValue.getStart(), secondValue.getStart()),
                domain.conjunction(firstValue.getEnd(), secondValue.getEnd()));
    }

    private AbstractInterval<R> disjunction(AbstractInterval<R> firstValue,
                                            AbstractInterval<R> secondValue,
                                            SignalDomain<R> domain)
    {
        return new AbstractInterval<>(
                domain.disjunction(firstValue.getStart(), secondValue.getStart()),
                domain.disjunction(firstValue.getEnd(), secondValue.getEnd()));
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
