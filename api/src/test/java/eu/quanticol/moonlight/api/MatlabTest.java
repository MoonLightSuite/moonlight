package eu.quanticol.moonlight.api;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MatlabTest {

    @Test
    @Disabled
    void compileScript() throws IOException {
        String a = "signal { real x; real y; real z;}\n" +
                "   domain boolean;\n" +
                "   formula aFormula = globally [73, 98] ( x>=0 );\n";

        Matlab.compileScript(a);
        System.out.println();
    }
}