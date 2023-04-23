module moonlight.engine {
    requires org.jetbrains.annotations;
    requires com.google.gson;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    exports eu.quanticol.moonlight;
    exports eu.quanticol.moonlight.domain;
    exports eu.quanticol.moonlight.core.base;
    exports eu.quanticol.moonlight.core.formula;
    exports eu.quanticol.moonlight.core.signal;
    exports eu.quanticol.moonlight.core.space;
    exports eu.quanticol.moonlight.core.io;
    exports eu.quanticol.moonlight.offline.signal;
    exports eu.quanticol.moonlight.space;
}
