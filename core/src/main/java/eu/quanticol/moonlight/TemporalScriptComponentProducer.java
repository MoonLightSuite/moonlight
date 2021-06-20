package eu.quanticol.moonlight;

import eu.quanticol.moonlight.formula.SignalDomain;

public interface TemporalScriptComponentProducer {

    <S> TemporalScriptComponent<S> apply(SignalDomain<S> domain);

}
