package io.github.moonlightsuite.moonlight.online.monitoring;

import io.github.moonlightsuite.moonlight.formula.Parameters;
import io.github.moonlightsuite.moonlight.online.signal.Update;

public class UpdateParameter<T extends Comparable<T>, V>
        extends Parameters
{

    private final Update<T, V> update;

    public UpdateParameter(Update<T, V> update) {
        this.update = update;
    }

    public Update<T, V> getUpdate() {
        return update;
    }
}
