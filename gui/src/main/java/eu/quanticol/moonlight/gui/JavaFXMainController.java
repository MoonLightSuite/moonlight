package eu.quanticol.moonlight.gui;

import eu.quanticol.moonlight.gui.chart.JavaFXChartController;
import eu.quanticol.moonlight.gui.graph.JavaFXGraphController;
import eu.quanticol.moonlight.gui.io.ThemeLoader;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
import eu.quanticol.moonlight.gui.io.JsonThemeLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Menu;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Objects;


/**
 * Main controller of the application. It has other controllers nested in it.
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class JavaFXMainController {

    @FXML
    AnchorPane chartComponent;
    @FXML
    JavaFXChartController chartComponentController;
    @FXML
    AnchorPane graphComponent;
    @FXML
    JavaFXGraphController graphComponentController;
    @FXML
    VBox root;
    @FXML
    Menu menuCSV;

    private JavaFXHomeController homeController = null;

    private final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    public String getTheme() {
        return  JsonThemeLoader.getInstance().getGeneralTheme();
    }

    public VBox getRoot() {
        return this.root;
    }

    public void setHomeController(JavaFXHomeController homeController) {
        this.homeController = homeController;
    }

    /**
     * Gets all info and controllers from the others fxml files included and inject this {@link JavaFXMainController} in its nested controllers.
     * Loads the theme if it was saved.
     */
    @FXML
    public void initialize() {
        this.chartComponentController.injectMainController(this, graphComponentController);
        this.graphComponentController.injectMainController(this, chartComponentController);
        loadTheme();
    }

    /**
     * Loads the theme
     */
    public void loadTheme() {
        try {
            initializeThemes();
            JsonThemeLoader.getInstance().addPropertyChangeListener(evt -> {
                if (evt.getPropertyName().equals("GeneralTheme") || evt.getPropertyName().equals("GraphTheme"))
                    loadTheme();
            });
        } catch (Exception e) {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            DialogBuilder d = new DialogBuilder(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
            d.warning("Failed loading theme.");
        }
    }

    /**
     * Initializes the theme for the window and the graphs
     */
    private void initializeThemes() {
        if (root.getStylesheets() != null) {
            if (!root.getStylesheets().isEmpty())
                root.getStylesheets().clear();
            root.getStylesheets().add(JsonThemeLoader.getInstance().getGeneralTheme());
        }
        if (this.graphComponentController.getCurrentGraph() != null && this.graphComponentController.getCurrentGraph().hasAttribute("ui.stylesheet")) {
            this.graphComponentController.getCurrentGraph().removeAttribute("ui.stylesheet");
            this.graphComponentController.getCurrentGraph().setAttribute("ui.stylesheet",  "url('" + JsonThemeLoader.getInstance().getGraphTheme() + "')");
        }
        graphComponentController.setTheme(JsonThemeLoader.getInstance().getGraphTheme());
    }

    /**
     * Open the explorer to choose a .csv file for pieceWise linear visualization
     */
    @FXML
    private void openCsvExplorer() {
        graphComponentController.openCSVExplorer();
    }

    /**
     * Open the explorer to choose a .csv file for stepWise constant visualization
     */
    @FXML
    private void openConstantCsvExplorer() {
        graphComponentController.openConstantCsvExplorer();
    }

    /**
     * Open the explorer to choose a .tra file
     */
    @FXML
    private void openTraExplorer() throws IOException {
        graphComponentController.openTraExplorer();
        menuCSV.setDisable(false);
    }

    /**
     * Load dark theme to the window.
     */
    @FXML
    private void loadDarkTheme() {
        try {
            JsonThemeLoader.getInstance().setGeneralTheme(Objects.requireNonNull(classLoader.getResource("css/darkTheme.css")).toString());
            JsonThemeLoader.getInstance().setGraphTheme(Objects.requireNonNull(classLoader.getResource("css/graphDarkTheme.css")).toURI().toString());
            initializeThemes();
            JsonThemeLoader.getInstance().saveToJson();
            homeController.loadTheme();
            JsonThemeLoader.getInstance().addPropertyChangeListener(evt -> {
                if (evt.getPropertyName().equals("GeneralTheme") || evt.getPropertyName().equals("GraphTheme"))
                    loadTheme();
            });
        } catch (Exception e) {
            DialogBuilder d = new DialogBuilder(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
            d.warning("Failed loading theme.");
        }
    }

    /**
     * Load light theme to the window.
     */
    @FXML
    private void loadLightTheme() {
        try {
            JsonThemeLoader.getInstance().setGeneralTheme(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
            JsonThemeLoader.getInstance().setGraphTheme(Objects.requireNonNull(classLoader.getResource("css/graphLightTheme.css")).toURI().toString());
            initializeThemes();
            JsonThemeLoader.getInstance().saveToJson();
            homeController.loadTheme();
            JsonThemeLoader.getInstance().addPropertyChangeListener(evt -> {
                if (evt.getPropertyName().equals("GeneralTheme") || evt.getPropertyName().equals("GraphTheme"))
                    loadTheme();
            });
        } catch (Exception e) {
            DialogBuilder d = new DialogBuilder(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
            d.warning("Failed loading theme.");
        }
    }
}
