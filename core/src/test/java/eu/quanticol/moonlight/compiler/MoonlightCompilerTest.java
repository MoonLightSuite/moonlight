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
        		"\n" + 
        		"\n" + 
        		"import eu.quanticol.moonlight.MoonLightScript;\n" + 
        		"import eu.quanticol.moonlight.SpatialTemporalScriptComponent;\n" + 
        		"import eu.quanticol.moonlight.TemporalScriptComponent;\n" + 
        		"\n" + 
        		"/**\n" + 
        		" * @author loreti\n" + 
        		" *\n" + 
        		" */\n" + 
        		"public class TestScript extends MoonLightScript {\n" + 
        		"\n" + 
        		"	public TestScript() {\n" + 
        		"		super(new String[0], new String[0]);\n" + 
        		"	}\n" + 
        		"\n" + 
        		"	@Override\n" + 
        		"	public TemporalScriptComponent<?> selectTemporalComponent(String name) {\n" + 
        		"		// TODO Auto-generated method stub\n" + 
        		"		return null;\n" + 
        		"	}\n" + 
        		"\n" + 
        		"	@Override\n" + 
        		"	public SpatialTemporalScriptComponent<?> selectSpatialTemporalComponent(String name) {\n" + 
        		"		// TODO Auto-generated method stub\n" + 
        		"		return null;\n" + 
        		"	}\n" + 
        		"\n" + 
        		"	@Override\n" + 
        		"	public TemporalScriptComponent<?> selectDefaultTemporalComponent() {\n" + 
        		"		// TODO Auto-generated method stub\n" + 
        		"		return null;\n" + 
        		"	}\n" + 
        		"\n" + 
        		"	@Override\n" + 
        		"	public SpatialTemporalScriptComponent<?> selectDefaultSpatialTemporalComponent() {\n" + 
        		"		// TODO Auto-generated method stub\n" + 
        		"		return null;\n" + 
        		"	}\n" + 
        		"\n" + 
        		"\n" + 
        		"}";

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