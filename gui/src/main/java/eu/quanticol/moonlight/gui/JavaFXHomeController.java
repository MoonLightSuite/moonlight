package eu.quanticol.moonlight.gui;

import eu.quanticol.moonlight.gui.filter.SimpleFiltersController;
import eu.quanticol.moonlight.gui.io.JsonThemeLoader;
import eu.quanticol.moonlight.gui.io.ThemeLoader;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
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
    ListView<String> recentFiles;

    @FXML
    MenuButton theme;
    @FXML
    MenuItem lightTheme;
    @FXML
    MenuItem darkTheme;

    private ThemeLoader themeLoader = new JsonThemeLoader();

    private final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    public String getTheme() {
        return themeLoader.getGeneralTheme();
    }


    public void initialize() {
        showProjectPane();
        loadTheme();
        initThemeMenu();
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
}
