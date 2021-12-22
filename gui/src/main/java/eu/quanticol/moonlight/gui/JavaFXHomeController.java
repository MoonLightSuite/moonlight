package eu.quanticol.moonlight.gui;

import eu.quanticol.moonlight.gui.io.*;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Home controller of the application.
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class JavaFXHomeController {

    @FXML
    TextField searchField;
    @FXML
    VBox root;
    @FXML
    Button projectsButton;
    @FXML
    Button themeButton;
    @FXML
    GridPane projectsPane;
    @FXML
    GridPane themePane;
    @FXML
    ListView<RecentFile> recentFiles;
    @FXML
    ImageView logo;
    @FXML
    ImageView reset;
    @FXML
    ImageView search;
    @FXML
    MenuButton theme;
    @FXML
    MenuItem lightTheme;
    @FXML
    MenuItem darkTheme;

    private final FilesLoader filesLoader = new JsonFilesLoader();
    private final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    private JavaFXMainController mainController = null;
    private final ArrayList<JavaFXMainController> controllers = new ArrayList<>();

    /**
     * Initialize all
     */
    public void initialize() throws IOException {
        initImages();
        initButtons();
        showProjectPane();
        projectsButton.setStyle("-fx-background-color: #788aaf");
        loadTheme();
        initThemeMenu();
        loadFilesOnList();
        addListenerToList();
        addListenersToSearch();
    }

    public void addController(JavaFXMainController controller) {
        controllers.add(controller);
    }

    private void resetSearch() {
        searchField.setText("");
        reset.setVisible(false);
    }

    /**
     * Adds mouseListener on doubleClick on listView
     */
    private void addListenerToList() {
        recentFiles.setOnMouseClicked(click -> {
            RecentFile recentFile = recentFiles.getSelectionModel().getSelectedItem();
            if (recentFile != null) {
                if (click.getClickCount() == 2) {
                    if (recentFile.getType() == FileType.TRA)
                        openProjectTRA(recentFile);
                    if (recentFile.getType() == FileType.CSV)
                        openProjectCSV(recentFile);
                    if(recentFile.getType() == FileType.JSON)
                        openProject(recentFile);
                    }
                }
        });
    }

    /**
     * Adds listener when user writes or deletes on the search textField
     */
    private void addListenersToSearch() {
        searchField.setOnKeyPressed(press -> {
            search();
            reset.setVisible(true);
            reset.setImage(new Image(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("images/cancel.png")).toString()));
        });
        searchField.addEventFilter(Event.ANY, e -> reset.setVisible(!searchField.getText().equals("")));
        reset.setOnMouseClicked(click -> {
            searchField.setText("");
            reset.setVisible(false);
        });
    }

    /**
     * Searches in recent files from user input
     */
    private void search() {
        FilteredList<RecentFile> filteredData = new FilteredList<>(recentFiles.getItems(), p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(file -> {
                if (newValue == null || newValue.isEmpty())
                    return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return file.getPathFile().toLowerCase().contains(lowerCaseFilter);
            });
        });
        recentFiles.setItems(filteredData);
    }

    /**
     * Initialize menu buttons
     */
    private void initButtons() {
        try {
            JsonThemeLoader.getInstance().getThemeFromJson();
        } catch (Exception e) {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            DialogBuilder d = new DialogBuilder(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
            d.warning("Failed loading theme.");
        }
        projectsButton.setOnMouseClicked(event -> {
            projectsButton.setStyle("-fx-background-color: #788aaf");
            themeButton.setStyle(" -fx-background-color: transparent");
        });
        themeButton.setOnMouseClicked(event -> {
            themeButton.setStyle("-fx-background-color: #788aaf");
            projectsButton.setStyle("-fx-background-color: transparent");
        });
    }

    private void initImages() {
        Image image = new Image(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("images/ML.png")).toString());
        logo.setImage(image);
        Image image2 = new Image(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("images/search.png")).toString());
        search.setImage(image2);
    }

    private void initThemeMenu() {
        lightTheme.setOnAction(event -> {
            selectLightTheme();
            theme.setText(lightTheme.getText());
        });
        darkTheme.setOnAction(event -> {
            selectDarkTheme();
            theme.setText(darkTheme.getText());
        });
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
     * Changes theme of the window
     */
    private void initializeThemes() {
        if (root.getStylesheets() != null) {
            if (!root.getStylesheets().isEmpty())
                root.getStylesheets().clear();
            root.getStylesheets().add(JsonThemeLoader.getInstance().getGeneralTheme());
        }
        if (JsonThemeLoader.getInstance() != null) {
            if (JsonThemeLoader.getInstance().getGeneralTheme().equals(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString()))
                theme.setText("Light Theme");
            else theme.setText("Dark Theme");
        }
    }

    /**
     * Changes theme to all windows
     */
    public void changeThemeToAll() {
        this.initializeThemes();
        for (JavaFXMainController main : controllers) {
            main.initializeThemes();
        }
    }

    /**
     * Shows the pane relative to projects
     */
    @FXML
    private void showProjectPane() {
        projectsPane.setVisible(true);
        projectsPane.setDisable(false);
        themePane.setVisible(false);
        themePane.setDisable(true);
    }

    /**
     * Shows the pane relative to themes
     */
    @FXML
    private void showThemePane() {
        projectsPane.setVisible(false);
        projectsPane.setDisable(true);
        themePane.setVisible(true);
        themePane.setDisable(false);
    }

    /**
     * Selects the light theme
     */
    @FXML
    private void selectLightTheme() {
        try {
            JsonThemeLoader.getInstance().setGeneralTheme(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
            JsonThemeLoader.getInstance().setGraphTheme(Objects.requireNonNull(classLoader.getResource("css/graphLightTheme.css")).toURI().toString());
            changeThemeToAll();
            JsonThemeLoader.getInstance().saveToJson();
        } catch (Exception e) {
            DialogBuilder d = new DialogBuilder(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
            d.warning("Failed loading theme.");
        }
    }

    /**
     * Selects the dark theme
     */
    @FXML
    private void selectDarkTheme() {
        try {
            JsonThemeLoader.getInstance().setGeneralTheme(Objects.requireNonNull(classLoader.getResource("css/darkTheme.css")).toString());
            JsonThemeLoader.getInstance().setGraphTheme(Objects.requireNonNull(classLoader.getResource("css/graphDarkTheme.css")).toURI().toString());
            changeThemeToAll();
            JsonThemeLoader.getInstance().saveToJson();
        } catch (Exception e) {
            DialogBuilder d = new DialogBuilder(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
            d.warning("Failed loading theme.");
        }
    }

    /**
     * Opens the window for input signal analysis
     */
    @FXML
    private void openInputSignal() {
        try {
            FXMLLoader fxmlLoader;
            fxmlLoader = new FXMLLoader(classLoader.getResource("fxml/mainComponent.fxml"));
            Parent newRoot = fxmlLoader.load();
            JavaFXMainController mainController = fxmlLoader.getController();
            controllers.add(mainController);
            mainController.setHomeController(this);
            this.mainController = mainController;
            Stage stage = new Stage();
            setStage(newRoot, stage);
            stage.showAndWait();
            resetSearch();
        } catch (IOException e) {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.error("Failed to load chart data");
        }
    }


    /**
     * Opens the window for input signal analysis of a recent file .tra
     *
     * @param recentFile file to open
     */
    private void openProjectTRA(RecentFile recentFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(classLoader.getResource("fxml/mainComponent.fxml"));
            Parent newRoot = fxmlLoader.load();
            JavaFXMainController mainController = fxmlLoader.getController();
            mainController.setHomeController(this);
            this.mainController = mainController;
            Stage stage = new Stage();
            setStage(newRoot, stage);
            stage.show();
            File file = new File(recentFile.getPathFile());
            mainController.openTra(file);
            resetSearch();
        } catch (IOException e) {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.error("Failed to load chart data");
        }
    }

    /**
     * Loads the recent .csv file on the opened window
     *
     * @param recentFile file to open
     */
    private void openProjectCSV(RecentFile recentFile) {
        if (mainController != null) {
            File file = new File(recentFile.getPathFile());
            mainController.openCSV(file);
        }
    }

    /**
     * Loads the recent .json file on the opened window
     *
     * @param recentFile file to open
     */
    private void openProject(RecentFile recentFile) {
        File file = new File(recentFile.getPathFile());
        openNewProjectWindow();
        mainController.openProjectFromHome(file);
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
            this.addController(mainController);
            this.mainController = mainController;
            mainController.setHomeController(this);
            mainController.setNewWindowController(mainController);
            Stage stage = new Stage();
            setStage(newRoot, stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Sets properties of stage
     *
     * @param newRoot root of scene
     * @param stage   stage
     */
    private void setStage(Parent newRoot, Stage stage) {
        stage.setTitle("MoonLight");
        stage.initStyle(StageStyle.DECORATED);
        stage.setScene(new Scene(newRoot));
        Image icon = new Image(Objects.requireNonNull(classLoader.getResource("images/ML.png")).toString());
        stage.getIcons().add(icon);
    }

    /**
     * Load recent files on the listView
     */
    public void loadFilesOnList() throws IOException {
        ObservableList<RecentFile> filesList = FXCollections.observableArrayList();
        recentFiles.setItems(filesList);
        String path = System.getProperty("user.home");
        path += File.separator + "MoonLightConfig" + File.separator + "files.json";
        File userFile = new File(path);
        if (userFile.length() != 0) {
            ArrayList<RecentFile> files = filesLoader.getFilesFromJson();
            for (int i = files.size() - 1; i >= 0; i--)
                filesList.add(files.get(i));
            recentFiles.setItems(filesList);
            addImagesToList();
        }
    }

    /**
     * Adds icons to recent files in listView
     */
    private void addImagesToList() {
        recentFiles.setCellFactory(param -> new ListCell<>() {
            private final ImageView displayImage = new ImageView();

            @Override
            public void updateItem(RecentFile file, boolean empty) {
                super.updateItem(file, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (file.getType() == FileType.TRA)
                        displayImage.setImage(new Image((Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("images/graph.png"))).toString()));
                    if (file.getType() == FileType.CSV)
                        displayImage.setImage(new Image(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("images/chart.png")).toString()));
                    if(file.getType() == FileType.JSON)
                        displayImage.setImage(new Image(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("images/openProject.png")).toString()));
                    setText(file.getPathFile());
                    setGraphic(displayImage);
                }
            }
        });
    }
}