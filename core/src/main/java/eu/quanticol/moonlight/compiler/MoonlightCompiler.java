package eu.quanticol.moonlight.compiler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


public class MoonlightCompiler {

    private MoonlightCompiler() {
        //Utility class
    }

    public static <T> T getIstance(String source, Class<T> object) throws IOException, ReflectiveOperationException {
        String replace = object.getPackageName().replace(".", File.separator);
        String tempDirWithPrefix = Files.createTempDirectory(UUID.randomUUID().toString()).toString();
        Path tmp = Paths.get(tempDirWithPrefix, replace);
        File root = new File(tmp.toUri()); // On Windows running on C:\, this is C:\java.
        root.mkdirs();
        File sourceFile = new File(root, object.getSimpleName() + ".java");
        Files.write(sourceFile.toPath(), source.getBytes(StandardCharsets.UTF_8));
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, sourceFile.getPath());
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{root.toURI().toURL()});
        Class<?> cls = Class.forName(object.getCanonicalName(), true, classLoader); // Should print "hello".
        return (T) cls.getDeclaredConstructor().newInstance(); // Should print "world"

    }
}
