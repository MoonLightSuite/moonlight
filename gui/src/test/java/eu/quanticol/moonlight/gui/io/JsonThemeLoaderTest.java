package eu.quanticol.moonlight.gui.io;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import eu.quanticol.moonlight.gui.io.JsonThemeLoader;
import eu.quanticol.moonlight.gui.io.ThemeLoader;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JsonThemeLoaderTest {

    static ThemeLoader themeLoader = JsonThemeLoader.getInstance();
    static String resetGeneralTheme;
    static String resetGraphTheme;
    static final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    static String generalTheme;
    static String graphTheme;
    static final String path = System.getProperty("user.home") + File.separator + "MoonLightConfig" + File.separator + "theme.json";
    static final File file = new File(path);

    @Test
    @BeforeAll
    @Order(1)
    static void saveTheme() throws IOException, URISyntaxException {
        JsonThemeLoader.getInstance().getThemeFromJson();
        themeLoader = JsonThemeLoader.getInstance();
        resetGeneralTheme = themeLoader.getGeneralTheme();
        resetGraphTheme = themeLoader.getGraphTheme();
    }

    @Test
    @BeforeAll
    @Order(2)
    static void getThemeFromJsonTest() throws IOException, URISyntaxException {
        JsonThemeLoader.getInstance().getThemeFromJson();
        generalTheme = JsonThemeLoader.getInstance().getGeneralTheme();
        graphTheme = JsonThemeLoader.getInstance().getGraphTheme();
        String pathGeneralTheme = Objects.requireNonNull(classLoader.getResource("css/darkTheme.css")).toString();
        String pathGraphTheme = Objects.requireNonNull(classLoader.getResource("css/graphDarkTheme.css")).toString();
        themeLoader.setGeneralTheme(pathGeneralTheme);
        themeLoader.setGraphTheme(pathGraphTheme);
        themeLoader.saveToJson();
        JsonThemeLoader.getInstance().getThemeFromJson();
        ThemeLoader loader1 = JsonThemeLoader.getInstance();
        assertEquals(loader1.getGeneralTheme(), pathGeneralTheme);
        assertEquals(loader1.getGraphTheme(), pathGraphTheme);
    }

    @Test
    void saveToJsonTest() throws IOException {
        JsonThemeLoader.getInstance().setGeneralTheme(generalTheme);
        JsonThemeLoader.getInstance().setGraphTheme(graphTheme);
        JsonThemeLoader.getInstance().saveToJson();
        Gson gson = new Gson();
        Reader reader = new FileReader(file);
        Type theme = new TypeToken<JsonThemeLoader>() {
        }.getType();
        JsonThemeLoader fromJson = gson.fromJson(reader, theme);
        assertEquals(fromJson.getGeneralTheme(), generalTheme);
        assertEquals(fromJson.getGraphTheme(), graphTheme);
    }



    @Test
    @AfterEach
    void resetFile() throws IOException {
        JsonThemeLoader.getInstance().setGeneralTheme(generalTheme);
        JsonThemeLoader.getInstance().setGraphTheme(graphTheme);
        JsonThemeLoader.getInstance().saveToJson();
    }

    @Test
    @AfterAll
    static void resetTheme() throws IOException {
        JsonThemeLoader.getInstance().setGeneralTheme(resetGeneralTheme);
        JsonThemeLoader.getInstance().setGraphTheme(resetGraphTheme);
        JsonThemeLoader.getInstance().saveToJson();
    }
}