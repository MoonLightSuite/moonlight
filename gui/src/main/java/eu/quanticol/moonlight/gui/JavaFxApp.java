package eu.quanticol.moonlight.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Class for fxml load
 */
public class JavaFxApp extends Application {

    public void start(Stage stage) throws Exception {
        VBox root = FXMLLoader.load(getClass().getResource("/fxml/mainComponent.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.getIcons().add(new Image("/images/ML.png"));
        stage.setTitle("MoonLightViewer");
        stage.initStyle(StageStyle.DECORATED);
        stage.show();
    }
}
