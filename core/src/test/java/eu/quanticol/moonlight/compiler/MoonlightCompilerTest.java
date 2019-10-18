package eu.quanticol.moonlight.compiler;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoonlightCompilerTest {

    @Test
    void testGetIstance() throws ReflectiveOperationException, IOException {
        String source = "package eu.quanticol.moonlight.compiler;\n" +
                "\n" +
                "public class TestLocal {  public TestLocal() {} public int test(){return 3;} }";

        TestLocal istance = MoonlightCompiler.getIstance(source, TestLocal.class);

        assertEquals(3, istance.test());
    }
}