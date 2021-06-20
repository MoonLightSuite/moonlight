package eu.quanticol.moonlight.api;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.script.ScriptLoader;

import java.io.IOException;

public class Matlab {

    public static MoonLightScript loadFromFile(String filePath) throws IOException {
        return ScriptLoader.loaderFromFile(filePath).getScript();
    }

    public static MoonLightScript loadFromCode(String code) throws IOException {
        return ScriptLoader.loaderFromCode(code).getScript();
    }


}