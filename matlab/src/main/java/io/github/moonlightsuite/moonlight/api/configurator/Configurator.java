package io.github.moonlightsuite.moonlight.api.configurator;

import java.io.InputStream;
import java.util.Properties;

public class Configurator {
    private static final Properties PROPERTIES = load();
    static final String STALIRO_PATH = PROPERTIES.getProperty("STALIRO_PATH");
    static final String BREACH_PATH = PROPERTIES.getProperty("BREACH_PATH");
    static final String UTILITY_PATH = PROPERTIES.getProperty("UTILITY_PATH");

    static Properties load() {
        Properties prop = new Properties();
        try {
            String configFile = System.getProperty("user.name") + ".properties";
            InputStream in = Configurator.class.getResourceAsStream(configFile);
            prop.load(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prop;
    }
}
