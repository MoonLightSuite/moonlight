package eu.quanticol.moonlight.gui;

import eu.quanticol.moonlight.gui.io.*;
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
    MenuButton theme;
    @FXML
    MenuItem lightTheme;
    @FXML
    MenuItem darkTheme;

    private ThemeLoader themeLoader = new JsonThemeLoader();
    private final FilesLoader filesLoader = new JsonFilesLoader();
    private final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    public String getTheme() {
        return themeLoader.getGeneralTheme();
    }

    public ListView<RecentFile> getRecentFiles() {
        return recentFiles;
    }

    public void initialize() throws IOException {
        showProjectPane();
        loadTheme();
        initThemeMenu();
        loadFilesOnList();
    }

    private void initThemeMenu() {
        if (themeLoader.getGeneralTheme().equals(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString()))
            theme.setText("Light Theme");
        else theme.setText("Dark Theme");
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
    private void loadTheme() {
        try {
            themeLoader = JsonThemeLoader.getThemeFromJson();
            initializeThemes();
        } catch (Exception e) {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            DialogBuilder d = new DialogBuilder(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
            d.warning("Failed loading theme.");
        }
    }

    private void initializeThemes() {
        if (root.getStylesheets() != null) {
            if (!root.getStylesheets().isEmpty())
                root.getStylesheets().clear();
            root.getStylesheets().add(themeLoader.getGeneralTheme());
        }
    }

    @FXML
    private void showProjectPane() {
        projectsPane.setVisible(true);
        projectsPane.setDisable(false);
        themePane.setVisible(false);
        themePane.setDisable(true);
        projectsButton.setStyle("-fx-background-color: #4e75a8");
        themeButton.setStyle("-fx-background-color: lightgrey");
    }

    @FXML
    private void showThemePane() {
        projectsPane.setVisible(false);
        projectsPane.setDisable(true);
        themePane.setVisible(true);
        themePane.setDisable(false);
        projectsButton.setStyle("-fx-background-color: lightgrey");
        themeButton.setStyle("-fx-background-color: #4e75a8");
    }

    @FXML
    private void selectLightTheme() {
        try {
            themeLoader.setGeneralTheme(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
            themeLoader.setGraphTheme(Objects.requireNonNull(classLoader.getResource("css/graphLightTheme.css")).toURI().toString());
            themeLoader.saveToJson();
            initializeThemes();
        } catch (Exception e) {
            DialogBuilder d = new DialogBuilder(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
            d.warning("Failed loading theme.");
        }
    }

    @FXML
    private void selectDarkTheme() {
        try {
            themeLoader.setGeneralTheme(Objects.requireNonNull(classLoader.getResource("css/darkTheme.css")).toString());
            themeLoader.setGraphTheme(Objects.requireNonNull(classLoader.getResource("css/graphDarkTheme.css")).toURI().toString());
            themeLoader.saveToJson();
            initializeThemes();
        } catch (Exception e) {
            DialogBuilder d = new DialogBuilder(Objects.requireNonNull(classLoader.getResource("css/lightTheme.css")).toString());
            d.warning("Failed loading theme.");
        }
    }

    @FXML
    private void openInputSignal() {
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            Parent newRoot = FXMLLoader.load(Objects.requireNonNull(classLoader.getResource("fxml/mainComponent.fxml")));
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