package eu.quanticol.moonlight.xtext.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.xtext.ScriptLoader;

class TestScriptLoader {

	@Test
	void testLoadScriptFromCode() {
		ScriptLoader loader = new ScriptLoader();
		MoonLightScript script = loader.compileScript("type poiType = BusStop|Hospital|MetroStop|MainSquare|Museum;		\n" + 
				"			\n" + 
				"			monitor City {\n" + 
				"				signal { bool taxi; int peole; }\n" + 
				"				space { locations {poiType poi; }\n" + 
				"				edges { real length; }\n" + 
				"				}\n" + 
				"				domain boolean;\n" + 
				"				formula somewhere [0.0, 1.0] #[ taxi ]#;\n" + 
				"			}\n" + 
				"\n" + 
				"			monitor City2 {\n" + 
				"				signal { bool taxi; int peole; }\n" + 
				"				space { locations {poiType poi; }\n" + 
				"				edges { real length; }\n" + 
				"				}\n" + 
				"				domain boolean;\n" + 
				"				formula somewhere [0.0, 1.0] #[ taxi ]#;\n" + 
				"			}");
		assertNotNull(script);
		
	}

	@Test
	void testLoadScriptFromFile() {
		ScriptLoader loader = new ScriptLoader();
		File file = new File(getClass().getClassLoader().getResource("testscript.mls").getFile());
		MoonLightScript script = loader.loadFile(file.getAbsolutePath());
		assertNotNull(script);
	}

}
