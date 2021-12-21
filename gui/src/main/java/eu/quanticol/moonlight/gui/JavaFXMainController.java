package eu.quanticol.moonlight.gui;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import eu.quanticol.moonlight.gui.chart.JavaFXChartController;
import eu.quanticol.moonlight.gui.filter.Filter;
import eu.quanticol.moonlight.gui.graph.JavaFXGraphController;
import eu.quanticol.moonlight.gui.io.JsonThemeLoader;
import eu.quanticol.moonlight.gui.io.ProjectSaver;
import eu.quanticol.moonlight.gui.io.SimpleProjectSaver;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    @FXML
    FontAwesomeIcon homeImage;

    private JavaFXHomeController homeController = null;

    private static Stage principal = null;

    ProjectSaver p = null;

    private final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    public String getTheme() {
        return  JsonThemeLoader.getInstance().getGeneralTheme();
    }

    public VBox getRoot() {
        return this.root;
    }

    public static void setPrincipal(Stage stage) { principal = stage; }

    public JavaFXHomeController getHomeController() { return this.homeController; }

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
        p = new SimpleProjectSaver(graphComponentController, chartComponentController);
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
        graphComponentController.openTRAExplorer();
        menuCSV.setDisable(false);
    }

    /**
     * Open the file .tra choose from recent files
     *
     * @param file   file to open
     */
    public void openTra(File file) throws IOException {
        graphComponentController.openRecentTRA(file);
        menuCSV.setDisable(false);
    }

    /**
     * Open the file .csv choose from recent files
     *
     * @param file   file to open
     */
    public void openCSV(File file) {
        graphComponentController.openRecentCSV(file);
    }

    /**
     * Closes the project opened
     */
    @FXML
    private void closeProject(){
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
        backHome();
    }

    @FXML
    private void saveProject() {
        p.setTra(graphComponentController.getTra());
        p.setCsv(graphComponentController.getCsv());
        p.setFilters(new ArrayList<>(graphComponentController.getFiltersComponentController().getTableFilters().getItems().stream().toList()));
        p.setColumnsAttributes(graphComponentController.getColumnsAttributes());
        p.setPositionX(graphComponentController.getLinkController().getColumnX());
        p.setPositionY(graphComponentController.getLinkController().getColumnY());
        //todo
        p.setGraphVisualization(chartComponentController.getGraphVisualization());
        p.setIndexOfAttributeChart(chartComponentController.getIndexOfAttributes());
        p.setStage(root);
        p.saveProject();

    }

    @FXML
    private void openProject() {
        p.openProject();
    }


    /**
     * Backs to the home of the app
     */
    @FXML
    private void backHome(){
        if(principal!= null && principal.isShowing())
            principal.toFront();
        if(!(principal == null || principal.isShowing()))
            principal.show();
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
