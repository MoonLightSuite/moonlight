package eu.quanticol.moonlight.gui;

import eu.quanticol.moonlight.gui.io.*;
import eu.quanticol.moonlight.gui.io.JsonThemeLoader;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
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
    ImageView search;
    @FXML
    MenuButton theme;
    @FXML
    MenuItem lightTheme;
    @FXML
    MenuItem darkTheme;

    private final FilesLoader filesLoader = new JsonFilesLoader();
    private final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    public ListView<RecentFile> getRecentFiles() {
        return recentFiles;
    }

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
    }

    /**
     * Initialize menu buttons
     */
    private void initButtons() {
        try {
            JsonThemeLoader.getInstance().getThemeFromJson();
        } catch (Exception e ) {
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
        if (JsonThemeLoader.getInstance() != null)
            if (JsonThemeLoader.getInstance().getGeneralTheme().equals(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString()))
                theme.setText("Light Theme");
            else theme.setText("Dark Theme");
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
            initializeThemes();
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
            initializeThemes();
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
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            FXMLLoader fxmlLoader = new FXMLLoader(classLoader.getResource("fxml/mainComponent.fxml"));
            Parent newRoot = fxmlLoader.load();
            JavaFXMainController mainController = fxmlLoader.getController();
            mainController.setHomeController(this);
            Stage stage = new Stage();
            stage.setTitle("MoonLight");
            stage.initStyle(StageStyle.DECORATED);
            stage.setScene(new Scene(newRoot));
            Image icon = new Image(Objects.requireNonNull(classLoader.getResource("images/ML.png")).toString());
            stage.getIcons().add(icon);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load recent files on list
     */
    @FXML
    private void loadFilesOnList() throws IOException {
        String path = System.getProperty("user.home");
        path += File.separator + "MoonLightConfig" + File.separator + "files.json";
        File userFile = new File(path);
        if(userFile.length() != 0) {
            ArrayList<RecentFile> files = filesLoader.getFilesFromJson();
            for (int i = files.size()-1; i >= 0; i--)
                recentFiles.getItems().add(files.get(i));
            addImagesToFiles();
        }
    }

    /**
     * Adds icons to recent files
     */
    private void addImagesToFiles() {
        recentFiles.setCellFactory(param -> new ListCell<>() {
            private final ImageView displayImage = new ImageView();
            @Override
            public void updateItem(RecentFile file, boolean empty) {
                super.updateItem(file, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    if (file.getType() == FileType.TRA)
                        displayImage.setImage(new Image((Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("images/graph.png"))).toString()));
                    if(file.getType() == FileType.CSV)
                        displayImage.setImage(new Image(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("images/chart.png")).toString()));
                    setText(file.getPathFile());
                    setGraphic(displayImage);
                }
            }
        });
    }
}