package eu.quanticol.moonlight;

import eu.quanticol.moonlight.core.signal.SignalDomain;

public interface TemporalScriptComponentProducer {

    <S> TemporalScriptComponent<S> apply(SignalDomain<S> domain);

}
