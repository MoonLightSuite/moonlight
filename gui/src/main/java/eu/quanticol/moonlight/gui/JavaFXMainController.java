package eu.quanticol.moonlight.gui;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import eu.quanticol.moonlight.gui.chart.ChartVisualization;
import eu.quanticol.moonlight.gui.chart.JavaFXChartController;
import eu.quanticol.moonlight.gui.filter.Filter;
import eu.quanticol.moonlight.gui.graph.JavaFXGraphController;
import eu.quanticol.moonlight.gui.io.JsonThemeLoader;
import eu.quanticol.moonlight.gui.io.ProjectSaver;
import eu.quanticol.moonlight.gui.io.SimpleProjectSaver;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
    private ProjectSaver p = null;
    private final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    private JavaFXMainController newWindowController = null;

    public String getTheme() {
        return JsonThemeLoader.getInstance().getGeneralTheme();
    }

    public VBox getRoot() {
        return this.root;
    }

    public static void setPrincipal(Stage stage) {
        principal = stage;
    }

    public JavaFXHomeController getHomeController() {
        return this.homeController;
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
        p = new SimpleProjectSaver(graphComponentController, chartComponentController);
        loadTheme();
    }

    /**
     * Loads the theme
     */
    public void loadTheme() {
        try {
            JsonThemeLoader.getInstance().getThemeFromJson();
            initializeThemes();
        } catch (Exception e) {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            DialogBuilder d = new DialogBuilder(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
            d.warning("Failed loading theme.");
        }
    }

    /**
     * Initializes the theme for the window and the graphs
     */
    public void initializeThemes() {
        if (root.getStylesheets() != null) {
            if (!root.getStylesheets().isEmpty())
                root.getStylesheets().clear();
            root.getStylesheets().add(JsonThemeLoader.getInstance().getGeneralTheme());
        }
        if (this.graphComponentController.getCurrentGraph() != null && this.graphComponentController.getCurrentGraph().hasAttribute("ui.stylesheet")) {
            this.graphComponentController.getCurrentGraph().removeAttribute("ui.stylesheet");
            this.graphComponentController.getCurrentGraph().setAttribute("ui.stylesheet", "url('" + JsonThemeLoader.getInstance().getGraphTheme() + "')");
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
     * @param file file to open
     */
    public void openTra(File file) throws IOException {
        graphComponentController.openRecentTRA(file);
        menuCSV.setDisable(false);
    }

    /**
     * Open the file .csv choose from recent files
     *
     * @param file file to open
     */
    public void openCSV(File file) {
        graphComponentController.openRecentCSV(file);
    }

    /**
     * Closes the project opened
     */
    @FXML
    private void closeProject() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
        backHome();
    }

    /**
     * Saves a project
     */
    @FXML
    private void saveProject() {
        p.setTra(graphComponentController.getTra());
        p.setCsv(graphComponentController.getCsv());
        p.setFilters(new ArrayList<>(graphComponentController.getFiltersComponentController().getTableFilters().getItems().stream().toList()));
        p.setColumnsAttributes(graphComponentController.getColumnsAttributes());
        p.setPositionX(graphComponentController.getLinkController().getColumnX());
        p.setPositionY(graphComponentController.getLinkController().getColumnY());
        p.setGraphVisualization(chartComponentController.getGraphVisualization());
        p.setIndexOfAttributeChart(chartComponentController.getIndexOfAttributes());
        p.setStage(root);
        p.saveProject();
    }

    /**
     * Opens a project
     */
    @FXML
    private void openProject() {
        try {
            p.openProject();
            int choice = chooseWindow();
            switch (choice) {
                case 0:
                    p.setChartController(chartComponentController);
                    p.setGraphController(graphComponentController);
                    initializeProject(p);
                    break;
                case 1:
                    openNewProjectWindow();
                    if(newWindowController != null) {
                        p.setGraphController(newWindowController.graphComponentController);
                        p.setChartController(newWindowController.chartComponentController);
                        initializeProject(p);
                    }
                    break;
                case 2:
                    break;
            }
        } catch (IOException e) {
            DialogBuilder d = new DialogBuilder(getTheme());
            d.error("Failed opening project");
        }
    }

    /**
     * Asks if the project should be opened in the same window or in a new one
     */
    private int chooseWindow() {
        ButtonType thisWindow = new ButtonType("This Window", ButtonBar.ButtonData.OK_DONE);
        ButtonType newWindow = new ButtonType("New Window", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = initAlert(thisWindow, newWindow, cancel);
        alert.showAndWait();
        if (alert.getResult() == thisWindow) {
            return 0;
        } else if (alert.getResult() == newWindow) {
            return 1;
        } else if (alert.getResult() == cancel) {
            return 2;
        }
        return 2;
    }

    private Alert initAlert(ButtonType thisWindow, ButtonType newWindow, ButtonType cancel) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Where would you like to open the project?", thisWindow, newWindow, cancel);
        alert.setTitle("Open Project");
        alert.setHeaderText(null);
        alert.initStyle(StageStyle.UNDECORATED);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getTheme());
        dialogPane.getStyleClass().add("dialog");
        return alert;
    }

    /**
     * Initialize all graphs, charts, and filters of an opened project
     */
    public void initializeProject(ProjectSaver p) throws IOException {
        initVariablesProject(p);
        p.getGraphController().openRecentTRA(p.getTra());
        if (p.getGraphController().getLinkController().getColumnX() != null && p.getGraphController().getLinkController().getColumnY() != null) {
            p.getGraphController().setPositionAssigned(true);
            switch (p.getGraphController().getGraphVisualization()) {
                case STATIC -> p.getGraphController().reloadStaticPositions();
                case DYNAMIC -> p.getGraphController().reloadDynamicPositions();
            }
        }
        if (p.getChartVisualization().equals(ChartVisualization.PIECEWISE))
            p.getGraphController().openRecentCSV(p.getCsv());
        else p.getGraphController().openConstantCsv(p.getCsv());
        p.getGraphController().getFiltersComponentController().getTableFilters().getItems().clear();
        for (Filter f : p.getFilters()) {
            p.getGraphController().getFiltersComponentController().addFilter(f);
        }
    }

    private void initVariablesProject(ProjectSaver p) {
        p.getGraphController().setColumnsAttributes(p.getColumnsAttributes());
        p.getGraphController().getLinkController().setAttributes(p.getColumnsAttributes());
        p.getGraphController().getLinkController().setColumnX(p.getPositionX());
        p.getGraphController().getLinkController().setColumnY(p.getPositionY());
        p.getGraphController().setCsv(p.getCsv());
        p.getChartController().setIndexOfAttributes(p.getIndexOfAttributeChart());
    }

    /**
     * Opens a new window with a project
     */
    private void openNewProjectWindow() {
        try {
            FXMLLoader fxmlLoader;
            fxmlLoader = new FXMLLoader(classLoader.getResource("fxml/mainComponent.fxml"));
            Parent newRoot = fxmlLoader.load();
            JavaFXMainController mainController = fxmlLoader.getController();
            homeController.addController(mainController);
            mainController.setHomeController(homeController);
            newWindowController = mainController;
            Stage stage = new Stage();
            setStage(newRoot, stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setStage(Parent newRoot, Stage stage) {
        stage.setTitle("MoonLight");
        stage.initStyle(StageStyle.DECORATED);
        stage.setScene(new Scene(newRoot));
        Image icon = new Image(Objects.requireNonNull(classLoader.getResource("images/ML.png")).toString());
        stage.getIcons().add(icon);
    }

    /**
     * Backs to the home of the app
     */
    @FXML
    private void backHome() {
        if (principal != null && principal.isShowing())
            principal.toFront();
        if (!(principal == null || principal.isShowing()))
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
            JsonThemeLoader.getInstance().saveToJson();
            homeController.changeThemeToAll();
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
            JsonThemeLoader.getInstance().saveToJson();
            homeController.changeThemeToAll();
        } catch (Exception e) {
            DialogBuilder d = new DialogBuilder(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
            d.warning("Failed loading theme.");
        }
    }
}
