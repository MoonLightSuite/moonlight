package eu.quanticol.moonlight.formula.mfr;

import eu.quanticol.moonlight.core.formula.Formula;

import java.util.function.BinaryOperator;

public record BinaryFormula<T>(Formula leftArgument,
                               Formula rightArgument,
                               BinaryOperator<T> operator) implements Formula {

    public BinaryOperator<T> getOperator() {
        return operator;
    }

    public Formula getRightArgument() {
        return rightArgument;
    }

    public Formula getLeftArgument() {
        return leftArgument;
    }
}
