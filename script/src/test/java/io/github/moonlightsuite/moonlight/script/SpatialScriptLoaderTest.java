package io.github.moonlightsuite.moonlight.script;

import io.github.moonlightsuite.moonlight.MoonLightScript;
import io.github.moonlightsuite.moonlight.MoonLightSpatialTemporalScript;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SpatialScriptLoaderTest {

    public final static String CITY_CODE = "type poiType = BusStop|Hospital|MetroStop|MainSquare|Museum;\n" +
            "\n" +
            "    signal { bool taxi; int peole; }\n" +
            "    space { " +
            "       edges{ real length; }\n" +
            "    }\n" +
            "    domain boolean;\n" +
            "    formula aFormula = somewhere [0.0, 1.0] ( taxi );\n"+
            "    formula anotherFormula = everywhere [0.0, 1.0] ( taxi );\n";

    public final static String CODE = "signal { real x; real y;}\n" +
            "domain boolean;\n" +
            "formula phi1(real LB, real UB) = globally [LB, UB]  ( x >= y );\n" +
            "formula phi2(real k) = globally [0, 1]  ( x >= k );";

    public final static String PATTERN = "signal { real A; real B; }\n" +
            "space { edges { int hop; } }\n" +
            "domain minmax;\n" +
            "formula lowValues = (A<=0.5) ;\n" +
            "formula highValues = (A>0.5);\n" +
            "formula reachability = lowValues & (lowValues reach[0, 3] highValues); \n" +
            "formula reachP = lowValues  reach[0, 6] (! (lowValues | highValues));  \n" +
            "formula surround0 = lowValues & (! reachP); \n" +
            "formula escP = escape[6, 60] lowValues;\n" +
            "formula surround = lowValues & ((!reachP) & (!escP));\n";

    public final static String CALLING_FORMULA = "signal { real A; real B; }\n" +
            "domain minmax;\n" +
            "formula one = (A<=0.5) ;\n" +
            "formula two = !one;\n";

    public final static String SENSOR = "signal { int nodeType; real battery; real temperature; }\n" +
            "space { edges { int hop; real dist; } }\n" +
            "domain boolean;\n" +
            "formula atom = (nodeType==3) ;\n"+
            "formula P1 = atom reach (hop)[0, 1] ((nodeType==2) | (nodeType==1));\n" +
            "formula Ppar (int k) = atom reach (hop)[0, k] ( nodeType== 1) ;\n" +
            "formula P2 = escape(hop)[5, inf] (battery > 0.1) ;\n" +
            "formula P3 = somewhere(dist)[0, 250] (battery > 0.5) ;\n" +
            "formula P3bis = somewhere(hop)[0, 3] (battery > 0.5) ;\n" +
            "formula P4 = (nodeType==3) reach (hop)[0, 1]((nodeType==2) reach (hop)[0, 5] (nodeType==1));\n" +
            "formula P5 = everywhere(dist)[0, inf]P4;\n" +
            "formula Pnest = everywhere(dist)[0, inf]P3;\n" +
            "formula ReachQ = (temperature > 10)reach(hop)[0, 10] (battery > 0.5);\n" +
            "formula SensNetk = somewhere(hop)[0, 3] (battery > 0.5);\n" +
            "formula SensBool = everywhere(hop)[0, 5] (nodeType==2);\n" +
            "formula Pbattery = (!atom) -> ((!atom) reach (hop)[0, 2] (battery > 0.5));\n" +
            "formula P6 = eventually(battery > 0.5);\n" +
            "formula PT1 = globally P4;\n" +
            "formula PT2 = everywhere(dist)[0, inf](eventually[0,5](battery > 0.5));\n" +
            "formula PE = escape(hop)[2, inf] (nodeType==3) ;\n";

    @Test
    void shouldLoadAScriptFromCode() throws IOException, MoonLightScriptLoaderException {
        ScriptLoader loader = ScriptLoader.loaderFromCode(CITY_CODE);
        assertNotNull( loader.getScript() );
    }

    @Test
    void shouldLoadAScriptFromCodeAndItsSpatial() throws IOException, MoonLightScriptLoaderException {
        ScriptLoader loader = ScriptLoader.loaderFromCode(CITY_CODE);
        MoonLightScript script = loader.getScript();
        assertFalse(script.isTemporal());
        assertTrue(script.isSpatialTemporal());
    }

    @Test
    void shouldLoadAScriptFromCodeAndItsSpatialWithTheRightMonitors() throws IOException, MoonLightScriptLoaderException {
        ScriptLoader loader = ScriptLoader.loaderFromCode(CITY_CODE);
        MoonLightScript script = loader.getScript();
        assertEquals( 2, script.getMonitors().length);
        assertEquals("aFormula", script.getMonitors()[0]);
        assertEquals("anotherFormula", script.getMonitors()[1]);
    }

    @Test
    void shouldLoadAScriptFromCode2() throws IOException, MoonLightScriptLoaderException {
        ScriptLoader loader = ScriptLoader.loaderFromCode(CODE);
        assertNotNull( loader.getScript() );
    }

    @Test
    void shouldLoadAScriptFromPattern() throws IOException, MoonLightScriptLoaderException {
        ScriptLoader loader = ScriptLoader.loaderFromCode(PATTERN);
        MoonLightScript script = loader.getScript();
        assertNotNull(script);
        assertTrue(script.isSpatialTemporal());
        MoonLightSpatialTemporalScript stScript = script.spatialTemporal();
        stScript.setMinMaxDomain();
    }

    @Test
    void shouldLoadAScriptFromCalling() throws IOException, MoonLightScriptLoaderException {
        ScriptLoader loader = ScriptLoader.loaderFromCode(CALLING_FORMULA);
        assertNotNull( loader.getScript() );
    }

    @Test
    void shouldLoadAScriptFromSensor() throws IOException, MoonLightScriptLoaderException {
        ScriptLoader loader = ScriptLoader.loaderFromCode(SENSOR);
        assertNotNull( loader.getScript() );
    }

}
