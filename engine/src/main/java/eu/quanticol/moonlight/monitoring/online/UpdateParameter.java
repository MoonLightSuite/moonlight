package eu.quanticol.moonlight.monitoring.online;

import eu.quanticol.moonlight.formula.Parameters;
import eu.quanticol.moonlight.signal.online.Update;

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
