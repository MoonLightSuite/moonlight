package eu.quanticol.moonlight.monitoring.temporal.online;

import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.formula.Parameters;

public class HorizonParameter extends Parameters {
    private final Interval horizon;

    public HorizonParameter(Interval horizon) {
        this.horizon = horizon;
    }

    public Interval getHorizon() {
        if(horizon != null)
            return horizon;

        throw new IllegalArgumentException("The Horizon parameter has not " +
                                           "been initialized!");
    }
}
