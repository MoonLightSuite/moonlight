package eu.quanticol.moonlight.online.monitoring;

import eu.quanticol.moonlight.formula.Parameters;
import eu.quanticol.moonlight.online.signal.Update;

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
