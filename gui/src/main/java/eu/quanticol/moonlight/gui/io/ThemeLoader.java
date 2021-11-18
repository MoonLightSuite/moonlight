package eu.quanticol.moonlight.gui.io;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Class to load the theme and save it
 */
public class ThemeLoader {

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
        Writer writer = Files.newBufferedWriter(Paths.get("gui/src/main/resources/json/theme.json"));
        gson.toJson(this, writer);
        writer.close();
    }

    /**
     * Gets the theme from a json file
     */
    public static ThemeLoader getThemeFromJson() throws IOException {
        Gson gson = new Gson();
        Reader reader = Files.newBufferedReader(Paths.get("gui/src/main/resources/json/theme.json"));
        Type theme = new TypeToken<ThemeLoader>() {
        }.getType();
        ThemeLoader fromJson = gson.fromJson(reader, theme);
        reader.close();
        return fromJson;
    }
}
