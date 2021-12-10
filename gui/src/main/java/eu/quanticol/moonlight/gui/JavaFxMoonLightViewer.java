package eu.quanticol.moonlight.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.Objects;

/**
 * This class is responsible for the principle window of app
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class JavaFxMoonLightViewer extends Application {

    /**
     * Launch the principle stage.
     *
     * @param stage           Stage to open.
     * @throws Exception      Exception.
     */
    public void start(Stage stage) throws Exception {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        FXMLLoader fxmlLoader = new FXMLLoader(classLoader.getResource("fxml/homeComponent.fxml"));
        VBox root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.getIcons().add(new Image((Objects.requireNonNull(classLoader.getResource("images/ML.png"))).toString()));
        stage.setTitle("Welcome to MoonLight");
        stage.initStyle(StageStyle.DECORATED);
        stage.setMinHeight(root.getMinHeight() + 40);
        stage.setMinWidth(root.getMinWidth() + 20);
        JavaFXMainController.setPrincipal(stage);
        stage.show();
    }
}
