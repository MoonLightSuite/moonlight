package io.github.moonlightsuite.moonlight;

import io.github.moonlightsuite.moonlight.core.signal.SignalDomain;

public interface TemporalScriptComponentProducer {

    <S> TemporalScriptComponent<S> apply(SignalDomain<S> domain);

}
