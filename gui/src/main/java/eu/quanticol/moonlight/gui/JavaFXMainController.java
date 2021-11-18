package eu.quanticol.moonlight.gui;

import eu.quanticol.moonlight.gui.chart.JavaFXChartController;
import eu.quanticol.moonlight.gui.graph.JavaFXGraphController;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
import eu.quanticol.moonlight.gui.io.ThemeLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * Main controller of the application. It has other controllers nested in it.
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
//    @FXML
//    HBox bar;

    private ThemeLoader themeLoader = new ThemeLoader();

    public JavaFXChartController getChartComponentController() {
        return chartComponentController;
    }

    public JavaFXGraphController getGraphComponentController() {
        return graphComponentController;
    }

    public String getTheme() {
        return themeLoader.getGeneralTheme();
    }

    public VBox getRoot() {
        return this.root;
    }

    /**
     * Gets all info and controllers from the others fxml files included and inject this {@link JavaFXMainController} in its nested controllers.
     * Loads the theme if it was saved.
     */
    @FXML
    public void initialize() {
        this.chartComponentController.injectMainController(this);
        this.graphComponentController.injectMainController(this, chartComponentController);
        loadTheme();
    }

    /**
     * Loads the theme
     */
    private void loadTheme() {
        try {
            if (ThemeLoader.getThemeFromJson() != null) {
                themeLoader = ThemeLoader.getThemeFromJson();
                initializeThemes();
            }
        } catch (Exception e) {
            DialogBuilder d = new DialogBuilder("css/lightTheme.css");
            d.warning(e.getMessage());
        }
    }

    /**
     * Initializes the theme for the window and the graphs
     */
    private void initializeThemes() {
        if (root.getStylesheets() != null) {
            if (!root.getStylesheets().isEmpty())
                root.getStylesheets().clear();
            root.getStylesheets().add(themeLoader.getGeneralTheme());
        }
        if (this.graphComponentController.getCurrentGraph() != null && this.graphComponentController.getCurrentGraph().hasAttribute("ui.stylesheet")) {
            this.graphComponentController.getCurrentGraph().removeAttribute("ui.stylesheet");
            this.graphComponentController.getCurrentGraph().setAttribute("ui.stylesheet", themeLoader.getGraphTheme());
        }
        graphComponentController.setTheme(themeLoader.getGraphTheme());
    }

    /**
     * Open the explorer to choose a .csv file
     */
    @FXML
    private void openCsvExplorer() {
        graphComponentController.openCSVExplorer();
    }

    /**
     * Open the explorer to choose a .tra file
     */
    @FXML
    private void openTraExplorer() {
        graphComponentController.openTraExplorer();
        menuCSV.setDisable(false);
    }

    /**
     * Load dark theme to the window.
     */
    @FXML
    private void loadDarkTheme() {
        try {
            themeLoader.setGeneralTheme("css/darkTheme.css");
            themeLoader.setGraphTheme("url('file://src/main/resources/css/graphDarkTheme.css')");
            themeLoader.saveToJson();
            initializeThemes();
        } catch (Exception e) {
            DialogBuilder d = new DialogBuilder("css/lightTheme.css");
            d.warning(e.getMessage());
        }
    }

    /**
     * Load light theme to the window.
     */
    @FXML
    private void loadLightTheme() {
        try {
            themeLoader.setGeneralTheme("css/lightTheme.css");
            themeLoader.setGraphTheme("url('file://src/main/resources/css/graphLightTheme.css')");
            themeLoader.saveToJson();
            initializeThemes();
        } catch (Exception e) {
            DialogBuilder d = new DialogBuilder("css/lightTheme.css");
            d.warning("Failed saving theme.");
        }
    }
}
