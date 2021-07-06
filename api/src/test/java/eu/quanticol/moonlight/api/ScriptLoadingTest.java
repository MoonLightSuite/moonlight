package eu.quanticol.moonlight.api;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.script.MoonLightScriptLoaderException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScriptLoadingTest {

    @Test
    @Disabled
    public void testLoadScript() throws IOException, MoonLightScriptLoaderException {
        Matlab mlClass = new Matlab();
        URL url = ScriptLoadingTest.class.getClassLoader().getResource("multipleMonitors.mls");
        MoonLightScript script = Matlab.loadFromFile(url.getFile());
        assertTrue(script.isTemporal());
    }

}


