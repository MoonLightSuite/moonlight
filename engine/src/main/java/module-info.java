module moonlight.engine {
    requires org.jetbrains.annotations;
    requires com.google.gson;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    exports io.github.moonlightsuite.moonlight;
    exports io.github.moonlightsuite.moonlight.domain;
    exports io.github.moonlightsuite.moonlight.core.base;
    exports io.github.moonlightsuite.moonlight.core.formula;
    exports io.github.moonlightsuite.moonlight.core.signal;
    exports io.github.moonlightsuite.moonlight.core.space;
    exports io.github.moonlightsuite.moonlight.core.io;
    exports io.github.moonlightsuite.moonlight.offline.signal;
    exports io.github.moonlightsuite.moonlight.space;
    exports io.github.moonlightsuite.moonlight.io;
}
