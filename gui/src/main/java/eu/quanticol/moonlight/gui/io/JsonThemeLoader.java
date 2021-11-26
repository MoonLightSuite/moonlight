package eu.quanticol.moonlight.gui.io;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Class that implements the {@link ThemeLoader} interface and is responsible to load the theme and save it
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class JsonThemeLoader implements ThemeLoader {

    private String generalTheme;
    private String graphTheme;

    public String getGeneralTheme() {
        return generalTheme;
    }

    public void setGeneralTheme(String generalTheme) {
        this.generalTheme = generalTheme;
    }

    public String getGraphTheme() {
        return graphTheme;
    }

    public void setGraphTheme(String graphTheme) {
        this.graphTheme = graphTheme;
    }

    /**
     * Save the theme chosen in a json file
     */
    public void saveToJson() throws IOException {
        Gson gson = new Gson();
        String path = System.getProperty("user.home");
        path += File.separator + "MoonLightConfig" + File.separator + "theme.json";
        File userFile = new File(path);
        userFile.getParentFile().mkdirs();
        if(!userFile.exists())
            userFile.createNewFile();
        Writer writer = new FileWriter(userFile);
        gson.toJson(this, writer);
        writer.close();
    }

    /**
     * Gets the theme from a json file
     *
     */
    public static ThemeLoader getThemeFromJson() throws IOException, URISyntaxException {
        Gson gson = new Gson();
        ThemeLoader fromJson = null;
        String path = System.getProperty("user.home");
        path += File.separator + "MoonLightConfig" + File.separator + "theme.json";
        File userFile = new File(path);
        userFile.getParentFile().mkdirs();
        if (!userFile.exists()) {
            if (userFile.createNewFile())
                fromJson = initializeFile();
        } else {
            Reader reader = new FileReader(userFile);
            Type theme = new TypeToken<JsonThemeLoader>() {
            }.getType();
            fromJson = gson.fromJson(reader, theme);
            if (fromJson == null)
                fromJson = initializeFile();
            reader.close();
        }
        return fromJson;
    }


    /**
     * Initialize an empty file for themes
     */
    private static ThemeLoader initializeFile() throws IOException, URISyntaxException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        ThemeLoader fromJson = new JsonThemeLoader();
        fromJson.setGeneralTheme(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
        fromJson.setGraphTheme(Objects.requireNonNull(classLoader.getResource("css/graphLightTheme.css")).toURI().toString());
        fromJson.saveToJson();
        return fromJson;
    }
}
