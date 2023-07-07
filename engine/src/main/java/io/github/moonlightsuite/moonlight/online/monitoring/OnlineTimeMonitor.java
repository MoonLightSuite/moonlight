package io.github.moonlightsuite.moonlight.online.monitoring;

import io.github.moonlightsuite.moonlight.core.base.Box;
import io.github.moonlightsuite.moonlight.core.formula.Formula;
import io.github.moonlightsuite.moonlight.core.formula.FormulaVisitor;
import io.github.moonlightsuite.moonlight.core.signal.SignalDomain;
import io.github.moonlightsuite.moonlight.formula.*;
import io.github.moonlightsuite.moonlight.formula.classic.AndFormula;
import io.github.moonlightsuite.moonlight.formula.classic.NegationFormula;
import io.github.moonlightsuite.moonlight.formula.classic.OrFormula;
import io.github.moonlightsuite.moonlight.formula.temporal.*;
import io.github.moonlightsuite.moonlight.online.monitoring.temporal.*;
import io.github.moonlightsuite.moonlight.online.signal.TimeChain;
import io.github.moonlightsuite.moonlight.core.signal.TimeSignal;
import io.github.moonlightsuite.moonlight.online.signal.Update;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Primary entry point to perform online monitoring.
 * Based on a visitor design pattern over the formula tree.
 *
 * @param <V> Signal Trace Type
 * @param <R> Semantic Interpretation Semiring Type
 *
 */
public class OnlineTimeMonitor<V, R extends Comparable<R>> {

    private final Formula formula;
    private final SignalDomain<R> interpretation;
    private final Map<String, OnlineMonitor<Double, V, Box<R>>>
                                                                       monitors;
    private final Map<String, Function<V, Box<R>>> atoms;

    public OnlineTimeMonitor(
        Formula formula,
        SignalDomain<R> interpretation,
        Map<String, Function<V, Box<R>>> atomicPropositions)
    {
        this.atoms = atomicPropositions;
        this.formula = formula;
        this.interpretation = interpretation;
        this.monitors = new HashMap<>();
    }

    /**
     * Entry point of the monitoring program:
     * it launches the monitoring process over the formula f.
     *
     * @param f the formula to monitor
     * @return the result of the monitoring process.
     */
    private OnlineMonitor<Double, V, Box<R>> monitor(Formula f) {
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
            default -> illegalFormula(f);
        };
    }

    private  OnlineMonitor<Double, V, Box<R>> illegalFormula(Formula f) {
        throw new IllegalArgumentException("Unsupported formula: " + f);
    }

    public TimeSignal<Double, Box<R>> monitor(@NotNull Update<Double, V> update)
    {
        var m = monitor(formula);
        m.monitor(update);
        return m.getResult();
    }

    public TimeSignal<Double, Box<R>> monitor(@NotNull
                                              TimeChain<Double, V> updates)
    {
        var m = monitor(formula);
        m.monitor(updates);
        return m.getResult();
    }

    private OnlineMonitor<Double, V, Box<R>> generateAtomicMonitor(
            AtomicFormula f)
    {
        var func = fetchAtom(f);

        return monitors.computeIfAbsent(f.toString(),
                                x -> new AtomicMonitor<>(func, interpretation));

    }

    private OnlineMonitor<Double, V, Box<R>> generateNegationMonitor(
            NegationFormula formula)
    {
        var argumentMonitor = monitor(formula.getArgument());

        return monitors.computeIfAbsent(formula.toString(),
                x -> new UnaryMonitor<>(argumentMonitor,
                        v -> negation(v, interpretation), interpretation));
    }

    private OnlineMonitor<Double, V, Box<R>> generateAndMonitor(
            AndFormula formula)
    {
        var firstArgMonitor = monitor(formula.getFirstArgument());

        var secondArgMonitor = monitor(formula.getSecondArgument());

        return monitors.computeIfAbsent(formula.toString(),
                x -> new BinaryMonitor<>(firstArgMonitor, secondArgMonitor,
                        (v1, v2) -> conjunction(v1, v2, interpretation),
                                                        interpretation));
    }

    private OnlineMonitor<Double, V, Box<R>> generateOrMonitor(
            OrFormula formula)
    {
        var firstArgMonitor = monitor(formula.getFirstArgument());

        var secondArgMonitor = monitor(formula.getSecondArgument());

        return monitors.computeIfAbsent(formula.toString(),
                x ->  new BinaryMonitor<>(firstArgMonitor, secondArgMonitor,
                        (v1, v2) -> disjunction(v1, v2, interpretation),
                        interpretation));
    }

    private OnlineMonitor<Double, V, Box<R>> generateEventuallyMonitor(
            EventuallyFormula formula)
    {
        var argumentMonitor = monitor(formula.getArgument());

        return monitors.computeIfAbsent(formula.toString(),
                x ->  new TemporalOpMonitor<>(argumentMonitor,
                        (v1, v2) -> disjunction(v1, v2, interpretation),
                                                        formula.getInterval(),
                                                        interpretation));
    }

    private OnlineMonitor<Double, V, Box<R>> generateGloballyMonitor(
            GloballyFormula formula)
    {
        var argumentMonitor = monitor(formula.getArgument());

        return monitors.computeIfAbsent(formula.toString(),
                x ->  new TemporalOpMonitor<>(argumentMonitor,
                        (v1, v2) -> conjunction(v1, v2, interpretation),
                                                        formula.getInterval(),
                                                        interpretation));
    }

    private Box<R> negation(Box<R> value,
                            SignalDomain<R> domain)
    {
        return new Box<>(domain.negation(value.getEnd()),
                                      domain.negation(value.getStart()));
    }

    private Box<R> conjunction(Box<R> fstVal,
                               Box<R> sndVal,
                               SignalDomain<R> domain)
    {
        return new Box<>(
                domain.conjunction(fstVal.getStart(), sndVal.getStart()),
                domain.conjunction(fstVal.getEnd(), sndVal.getEnd()));
    }

    private Box<R> disjunction(Box<R> fstVal,
                               Box<R> sndVal,
                               SignalDomain<R> domain)
    {
        return new Box<>(
                domain.disjunction(fstVal.getStart(), sndVal.getStart()),
                domain.disjunction(fstVal.getEnd(), sndVal.getEnd()));
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
