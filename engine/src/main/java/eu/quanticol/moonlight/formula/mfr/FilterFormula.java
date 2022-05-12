package eu.quanticol.moonlight.formula.mfr;

import java.util.function.Predicate;

public record FilterFormula<T>(Predicate<T> predicate, SetFormula argument)
        implements SetFormula {
    public Predicate<T> getPredicate() {
        return predicate;
    }

    public SetFormula getArgument() {
        return argument;
    }
}