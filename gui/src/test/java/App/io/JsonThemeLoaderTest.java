package App.io;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import eu.quanticol.moonlight.gui.io.JsonThemeLoader;
import eu.quanticol.moonlight.gui.io.ThemeLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonThemeLoaderTest {

    static ThemeLoader themeLoader = new JsonThemeLoader();
    static ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    static String generalTheme;
    static String graphTheme;

    @Test
    void saveToJsonTest() throws IOException {
        themeLoader.setGeneralTheme(generalTheme);
        themeLoader.setGraphTheme(graphTheme);
        themeLoader.saveToJson();
        Gson gson = new Gson();
        File file = new File((Objects.requireNonNull(classLoader.getResource("json/theme.json"))).getFile());
        Reader reader = new FileReader(file);
        Type theme = new TypeToken<JsonThemeLoader>() {}.getType();
        JsonThemeLoader fromJson = gson.fromJson(reader, theme);
        assertEquals(fromJson.getGeneralTheme(),generalTheme);
        assertEquals(fromJson.getGraphTheme(),graphTheme);
    }

    @Test
    @BeforeAll
    static void getThemeFromJsonTest() throws IOException, URISyntaxException {
        ThemeLoader loader = JsonThemeLoader.getThemeFromJson();
        generalTheme = loader.getGeneralTheme();
        graphTheme = loader.getGraphTheme();
        String pathGeneralTheme = Objects.requireNonNull(classLoader.getResource("css/darkTheme.css")).toString();
        String pathGraphTheme = Objects.requireNonNull(classLoader.getResource("css/graphDarkTheme.css")).toString();
        themeLoader.setGeneralTheme(pathGeneralTheme);
        themeLoader.setGraphTheme(pathGraphTheme);
        themeLoader.saveToJson();
        ThemeLoader loader1 = JsonThemeLoader.getThemeFromJson();
        assertEquals(loader1.getGeneralTheme(),pathGeneralTheme );
        assertEquals(loader1.getGraphTheme(), pathGraphTheme);
    }

    @Test
    @AfterEach
    void resetFile() throws IOException {
        themeLoader.setGeneralTheme(generalTheme);
        themeLoader.setGraphTheme(graphTheme);
        themeLoader.saveToJson();
    }
}