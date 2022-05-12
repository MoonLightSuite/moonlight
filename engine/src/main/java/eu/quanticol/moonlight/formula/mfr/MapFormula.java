package eu.quanticol.moonlight.formula.mfr;

import eu.quanticol.moonlight.core.formula.Formula;

import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 *
 * @param mapper
 * @param argument
 * @param <R> interpretation domain
 */
public record MapFormula<R>(UnaryOperator<R> mapper,
                            SetFormula argument) implements SetFormula
{
    public UnaryOperator<R> getMapper() { return mapper; }

    public SetFormula getArgument() { return argument; }
}
