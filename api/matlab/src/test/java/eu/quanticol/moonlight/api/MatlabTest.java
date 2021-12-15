package eu.quanticol.moonlight.api;

import eu.quanticol.moonlight.script.MoonLightScriptLoaderException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MatlabTest {

    @Test
    @Disabled
    void compileScript() throws IOException, MoonLightScriptLoaderException {
        String a = "signal { real x; real y; real z;}\n" +
                "   domain boolean;\n" +
                "   formula aFormula = globally [73, 98] ( x>=0 );\n";

        Matlab.loadFromCode(a);
        System.out.println();
    }
}