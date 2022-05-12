package eu.quanticol.moonlight.formula.mfr;

import eu.quanticol.moonlight.core.formula.Formula;

public record BindToFormula<T>(T bind, Formula toBind, Formula argument) {

    public T getBind() {
        return bind;
    }

    public Formula getToBind() {
        return toBind;
    }

    public Formula getArgument() {
        return argument;
    }
}
