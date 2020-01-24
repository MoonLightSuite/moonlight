package eu.quanticol.moonlight.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MatlabTest {

    @Test
    void compileScript() throws IOException {
        String a = "monitor RandomFormulae {\n" +
                "      \t\t\t\tsignal { real x; real y; real z;}\n" +
                "      \t\t\t\tdomain boolean;\n" +
                "      \t\t\t\tformula globally [73, 98] #[ x>=0 ]#;\n" +
                "      \t\t\t}";

        Matlab.compileScript(a);
        System.out.println();
    }
}