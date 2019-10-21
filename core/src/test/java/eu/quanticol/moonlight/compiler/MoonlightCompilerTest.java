package eu.quanticol.moonlight.compiler;

import org.junit.jupiter.api.Test;

import eu.quanticol.moonlight.MoonLightScript;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MoonlightCompilerTest {

    @Test
    void testGetIstance() throws ReflectiveOperationException, IOException {
    	MoonlightCompiler comp = new MoonlightCompiler();
        String source = "package eu.quanticol.moonlight.test;\n" +
                "import eu.quanticol.moonlight.compiler.TestLocal;"
                + "\n" +
                "public class TestClass implements TestLocal {  public TestClass() {} public int test(){return 3;} }";

        TestLocal instance = comp.getIstance("eu.quanticol.moonlight.test", "TestClass", source, TestLocal.class);

        assertEquals(3, instance.test());
    }

    @Test
    void testLoadScript() throws ReflectiveOperationException, IOException {
    	MoonlightCompiler comp = new MoonlightCompiler();
        String source = "package eu.quanticol.moonlight.test;\n" +
                "import eu.quanticol.moonlight.MoonLightScript;"
                + "\n" +
                "public class TestScript implements MoonLightScript {  "
                + 
                "   public void monitor( String label, String inputFile , String outputFile ) {}\n" + 
                "	\n" + 
                "	public String[] getMonitors() { return null; } " + 
                "	\n" + 
                "	public String getInfo( String monitor ) { return \"\"; }"
                + "}";

        MoonLightScript instance = comp.getIstance("eu.quanticol.moonlight.test", "TestScript", source, MoonLightScript.class);

        assertNotNull( instance );
    }

    
//    @Test
//    void testCompileMoonLightClasses() {
//    	String source = "package eu.quanticol.moonlight.compiler;\n" + 
//    			"\n" + 
//    			"import eu.quanticol.moonlight.util.Pair;\n" + 
//    			"\n" + 
//    			"public class TestLocal extends Pair<Integer,Integer> {\n" + 
//    			"    public TestLocal(int x , int y) {\n" + 
//    			"    	super(x,y);\n" + 
//    			"    }\n" + 
//    			"}\n" + 
//    			"";
//        TestLocal istance = MoonlightCompiler.getIstance(source, TestLocal.class);
//
//        assertEquals(3, istance.test());
//    	
//    }
}