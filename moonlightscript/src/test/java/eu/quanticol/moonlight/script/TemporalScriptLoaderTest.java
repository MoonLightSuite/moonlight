package eu.quanticol.moonlight.script;

import eu.quanticol.moonlight.MoonLightScript;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TemporalScriptLoaderTest {

    public final static String CODE = "\n" +
            "signal { real x; real y; }\n" +
            "domain boolean;\n" +
            "formula future = globally [0, 0.2]  (x <= y);";

    @Test
    void shouldLoadAScriptFromCode() throws IOException {
        ScriptLoader loader = ScriptLoader.loaderFromCode(CODE);
        assertNotNull( loader.getScript() );
    }

    @Test
    void shouldLoadAScriptFromCodeAndItsSpatial() throws IOException {
        ScriptLoader loader = ScriptLoader.loaderFromCode(CODE);
        MoonLightScript script = loader.getScript();
        assertTrue(script.isTemporal());
        assertFalse(script.isSpatialTemporal());
    }

    @Test
    void shouldLoadAScriptFromCodeAndItsSpatialWithTheRightMonitors() throws IOException {
        ScriptLoader loader = ScriptLoader.loaderFromCode(CODE);
        MoonLightScript script = loader.getScript();
        assertEquals( 1, script.getMonitors().length);
        assertEquals("future", script.getMonitors()[0]);
    }
}