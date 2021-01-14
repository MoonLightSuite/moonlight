package eu.quanticol.moonlight;
import java.io.IOException;

import eu.quanticol.moonlight.compiler.MoonlightCompiler;
import eu.quanticol.moonlight.xtext.ScriptLoader;

public class MoonLightConsole {

	private final MoonlightCompiler compiler;
	private final ScriptLoader scriptLoader;
	
	public MoonLightConsole() throws IOException {
		this(new MoonlightCompiler());
	}
	
	public MoonLightConsole(MoonlightCompiler moonlightCompiler) {
		this.compiler = moonlightCompiler;
		this.scriptLoader = new ScriptLoader(this.compiler);
	}
	
	public void generateScriptClassFromString(String packageName, String className,  String code) {
		scriptLoader.generateJavaClassesFromCode(packageName, className, code);
	}

	public void generateScriptClassFromString(String code) {
		scriptLoader.generateJavaClassesFromCode(code);
	}

	public void generateScriptClassFromFile(String packageName, String className,  String filePath) {
		scriptLoader.generateJavaClassesFromFile(packageName, className, filePath);
	}

	public void generateScriptClassFromFile(String filePath) {
		scriptLoader.generateJavaClassesFromFile(filePath);
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {

			throw new IllegalArgumentException("Expected: <scriptfile> <outputdir>");
		}
		MoonLightConsole mlc = new MoonLightConsole(new MoonlightCompiler(args[1]));
		mlc.generateScriptClassFromFile(args[0]);
	}
}