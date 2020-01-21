package eu.quanticol.moonlight.api;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.xtext.ScriptLoader;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class Matlab {

    public static MoonLightScript loadFromFile(String filePath) {
        return new ScriptLoader().loadFile(filePath);
    }

    public static MoonLightScript compileScript(String fileContent) {
        return new ScriptLoader().compileScript(fileContent);
    }

    public static JavaCompiler pippo() {
        return ToolProvider.getSystemJavaCompiler();
    }

    public static MoonLightScript loadJavaClass(String filePath) throws MalformedURLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        int i = filePath.lastIndexOf(System.getProperty("file.separator")) + 1;
        int j = filePath.lastIndexOf(".");
        String className = filePath.substring(i, j);
        String folderLocation = filePath.substring(0, i);
        File file = new File(folderLocation);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        System.out.println(compiler);
        System.out.println(filePath);
        compiler.run(null, null, null, filePath);

        // Convert File to a URL
        URL url = file.toURI().toURL();          // file:/c:/myclasses/
        URL[] urls = new URL[]{url};

        // Create a new class loader with the directory
        ClassLoader cl = new URLClassLoader(urls);

        // Load in the class; MyClass.class should be located in
        // the directory file:/c:/myclasses/com/mycompany
        return (MoonLightScript) cl.loadClass(className).newInstance();
    }


}