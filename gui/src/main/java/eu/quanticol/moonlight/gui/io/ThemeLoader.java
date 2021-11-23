package eu.quanticol.moonlight.gui.io;

import java.io.IOException;


public interface ThemeLoader {

    String getGeneralTheme();

    void setGeneralTheme(String generalTheme);

    String getGraphTheme();

    void setGraphTheme(String graphTheme);

    void saveToJson() throws IOException;
}

