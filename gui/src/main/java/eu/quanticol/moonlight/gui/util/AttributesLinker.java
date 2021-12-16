package eu.quanticol.moonlight.gui.util;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;

public class AttributesLinker {

    @FXML
    AnchorPane anchor;
    @FXML
    GridPane grid;

    private final ArrayList<String> names = new ArrayList<>();
    private final ArrayList<TextField> texts = new ArrayList<>();

    public ArrayList<String> getNames() {
        return names;
    }

    public AnchorPane getAnchor() {
        return anchor;
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> anchor.requestFocus());
    }

    public void addLabels(int totalAttributes) {
        grid.setPadding(new Insets(10, 10, 10, 10));
        RowConstraints rowConstraints = new RowConstraints(50);
        rowConstraints.setValignment(VPos.CENTER);
        grid.setHgap(10);
        for (int i = 1; i <= totalAttributes; i++) {
            Label column = new Label("Column " + i);
            TextField name = new TextField();
            GridPane.setHgrow(name, Priority.ALWAYS);
            name.setPromptText("Insert name");
            texts.add(name);
            grid.add(column, 0, i - 1);
            grid.add(name, 1, i - 1);
            grid.getRowConstraints().add(rowConstraints);
        }
    }


    @FXML
    private void saveAssociation() {
        names.add("time");
        for (TextField n : texts) {
            names.add(n.getText());
        }
        Stage stage = (Stage) anchor.getScene().getWindow();
        stage.close();
    }

}
