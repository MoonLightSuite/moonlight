package eu.quanticol.moonlight.script;

import eu.quanticol.moonlight.MoonLightScript;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SpatialScriptLoaderTest {

    public final static String CITY_CODE = "type poiType = BusStop|Hospital|MetroStop|MainSquare|Museum;\n" +
            "\n" +
            "    signal { bool taxi; int peole; }\n" +
            "    space { " +
            "       real length;\n" +
            "    }\n" +
            "    domain boolean;\n" +
            "    formula aFormula = somewhere [0.0, 1.0] ( taxi );\n"+
            "    formula anotherFormula = everywhere [0.0, 1.0] ( taxi );\n";

    @Test
    void shouldLoadAScriptFromCode() throws IOException {
        ScriptLoader loader = ScriptLoader.loaderFromCode(CITY_CODE);
        assertNotNull( loader.getScript() );
    }

    @Test
    void shouldLoadAScriptFromCodeAndItsSpatial() throws IOException {
        ScriptLoader loader = ScriptLoader.loaderFromCode(CITY_CODE);
        MoonLightScript script = loader.getScript();
        assertFalse(script.isTemporal());
        assertTrue(script.isSpatialTemporal());
    }

    @Test
    void shouldLoadAScriptFromCodeAndItsSpatialWithTheRightMonitors() throws IOException {
        ScriptLoader loader = ScriptLoader.loaderFromCode(CITY_CODE);
        MoonLightScript script = loader.getScript();
        assertEquals( 2, script.getMonitors().length);
        assertEquals("aFormula", script.getMonitors()[0]);
        assertEquals("anotherFormula", script.getMonitors()[1]);
    }
}