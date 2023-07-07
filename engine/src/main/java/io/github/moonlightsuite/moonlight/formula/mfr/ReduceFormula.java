package io.github.moonlightsuite.moonlight.formula.mfr;

import io.github.moonlightsuite.moonlight.core.formula.Formula;

import java.util.List;
import java.util.function.Function;

public record ReduceFormula<V, R>(String distanceFunctionId,
                                  Function<List<V>, R> aggregator,
                                  SetFormula argument) implements Formula {
    public String getDistanceFunctionId() {
        return distanceFunctionId;
    }

    public SetFormula getArgument() {
        return argument;
    }

    public Function<List<V>, R> getAggregator() {
        return aggregator;
    }
}
