package eu.quanticol.moonlight.gui.io;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Class to load the theme and save it
 */
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
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File((Objects.requireNonNull(classLoader.getResource("json/theme.json"))).getFile());
        Writer writer = new FileWriter(file);
        gson.toJson(this, writer);
        writer.close();
    }

    /**
     * Gets the theme from a json file
     */
    public static JsonThemeLoader getThemeFromJson() throws IOException, URISyntaxException {
        Gson gson = new Gson();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File((Objects.requireNonNull(classLoader.getResource("json/theme.json"))).getFile());
        Reader reader = new FileReader(file);
        Type theme = new TypeToken<JsonThemeLoader>() {
        }.getType();
        JsonThemeLoader fromJson = gson.fromJson(reader, theme);
        if (fromJson == null) {
            fromJson = new JsonThemeLoader();
            fromJson.setGeneralTheme(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
            fromJson.setGraphTheme(Objects.requireNonNull(classLoader.getResource("css/graphLightTheme.css")).toURI().toString());
            fromJson.saveToJson();
        }
        reader.close();
        return fromJson;
    }
}


