package eu.quanticol.moonlight.api;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.xtext.ScriptLoader;

public class Matlab {

    public static MoonLightScript loadFromFile(String filePath) {
        return new ScriptLoader().loadFile(filePath);
    }

    public static MoonLightScript compileScript(String fileContent) {
        return new ScriptLoader().compileScript(fileContent);
    }


}