package io.github.moonlightsuite.moonlight.script;

import java.util.List;
import java.util.stream.Collectors;

public class MoonLightScriptLoaderException extends Exception {
    public MoonLightScriptLoaderException(List<MoonLightParseError> errors) {
        super(errors.stream().map(MoonLightParseError::getMessage).collect(Collectors.joining("\n")));
    }
}
