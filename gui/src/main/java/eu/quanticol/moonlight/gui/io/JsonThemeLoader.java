package eu.quanticol.moonlight.gui.io;

import com.google.gson.Gson;
import com.google.gson.InstanceCreator;
import com.google.gson.reflect.TypeToken;
import com.sun.javafx.property.adapter.PropertyDescriptor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
    private static JsonThemeLoader themeLoader = null;

    private final transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private JsonThemeLoader() {
        this.generalTheme = null;
        this.graphTheme = null;
    }

    public static JsonThemeLoader getInstance() {
        if (themeLoader == null)
            themeLoader = new JsonThemeLoader();
        return themeLoader;
}

    public String getGeneralTheme() {
        return generalTheme;
    }

    public void setGeneralTheme(String newTheme) {
        String oldTheme = this.generalTheme;
        this.generalTheme = newTheme;
        this.propertyChangeSupport.firePropertyChange("GeneralTheme", oldTheme, newTheme);
    }

    public String getGraphTheme() {
        return graphTheme;
    }

    public void setGraphTheme(String newTheme) {
        String oldTheme = this.graphTheme;
        this.graphTheme = newTheme;
        this.propertyChangeSupport.firePropertyChange("GraphTheme", oldTheme, newTheme);
    }

    /**
     * Add a listener to a property that changes
     *
     * @param listener listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
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
    public void getThemeFromJson() throws IOException, URISyntaxException {
        Gson gson = new Gson();
        String path = System.getProperty("user.home");
        path += File.separator + "MoonLightConfig" + File.separator + "theme.json";
        File userFile = new File(path);
        userFile.getParentFile().mkdirs();
        if (!userFile.exists()) {
            if (userFile.createNewFile())
                initializeFile();
        } else {
            Reader reader = new FileReader(userFile);
            Type theme = new TypeToken<JsonThemeLoader>() {
            }.getType();
            themeLoader = gson.fromJson(reader, theme);
            if (themeLoader.getGeneralTheme() == null) {
                initializeFile();
            }
            reader.close();
        }
    }

    /**
     * Initialize an empty file for themes
     */
    private void initializeFile() throws IOException, URISyntaxException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        themeLoader.setGeneralTheme(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
        themeLoader.setGraphTheme(Objects.requireNonNull(classLoader.getResource("css/graphLightTheme.css")).toURI().toString());
        themeLoader.saveToJson();
    }
}
