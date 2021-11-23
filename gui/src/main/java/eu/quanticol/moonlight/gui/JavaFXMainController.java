package eu.quanticol.moonlight.gui;

import eu.quanticol.moonlight.gui.chart.JavaFXChartController;
import eu.quanticol.moonlight.gui.graph.JavaFXGraphController;
import eu.quanticol.moonlight.gui.io.ThemeLoader;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
import eu.quanticol.moonlight.gui.io.JsonThemeLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

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

    private ThemeLoader themeLoader = new JsonThemeLoader();

    private final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

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
            JsonThemeLoader.getThemeFromJson();
            themeLoader = JsonThemeLoader.getThemeFromJson();
            initializeThemes();
        } catch (Exception e) {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            DialogBuilder d = new DialogBuilder(Objects.requireNonNull(classLoader.getResource("css/lightTheme.json")).toString());
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
            this.graphComponentController.getCurrentGraph().setAttribute("ui.stylesheet",  "url('" + themeLoader.getGraphTheme() + "')");
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
            themeLoader.setGeneralTheme(Objects.requireNonNull(classLoader.getResource("css/darkTheme.css")).toString());
            themeLoader.setGraphTheme(Objects.requireNonNull(classLoader.getResource("css/graphDarkTheme.css")).toURI().toString());
            themeLoader.saveToJson();
            initializeThemes();
        } catch (Exception e) {
            DialogBuilder d = new DialogBuilder(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
            d.warning(e.getMessage());
        }
    }

    /**
     * Load light theme to the window.
     */
    @FXML
    private void loadLightTheme() {
        try {
            themeLoader.setGeneralTheme(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
            themeLoader.setGraphTheme(Objects.requireNonNull(classLoader.getResource("css/graphLightTheme.css")).toURI().toString());
            themeLoader.saveToJson();
            initializeThemes();
        } catch (Exception e) {
            DialogBuilder d = new DialogBuilder(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
            d.warning("Failed saving theme.");
        }
    }


//    /**
//     * Closes window and terminate the application run
//     */
//    @FXML
//    private void close() {
//        Platform.exit();
//    }
//
//    /**
//     * Maximize the size of window
//     */
//    @FXML
//    private void maximize() {
//        Stage stage = (Stage) root.getScene().getWindow();
//        stage.setMaximized(!stage.isMaximized());
//    }
//
//    /**
//     * Minimize the window to icon
//     */
//    @FXML
//    private void minimize() {
//        Stage stage = (Stage) root.getScene().getWindow();
//        stage.setIconified(true);
//    }
//
//    private double x, y;
//
//    /**
//     * Performs drag of window (on vbox)
//     */
//    @FXML
//    private void dragged(MouseEvent event) {
//        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        stage.setX(event.getScreenX() - x);
//        stage.setY(event.getScreenY() - y);
//    }
//
//    /**
//     * Gets positions of scene when mouse is pressed (on vbox)
//     */
//    @FXML
//    private void pressed(MouseEvent event) {
//        x = event.getSceneX();
//        y = event.getSceneY();
//    }
}
