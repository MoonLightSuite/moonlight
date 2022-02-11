package eu.quanticol.moonlight;

import eu.quanticol.moonlight.domain.SignalDomain;

public interface TemporalScriptComponentProducer {

    <S> TemporalScriptComponent<S> apply(SignalDomain<S> domain);

}
