package eu.quanticol.moonlight.gui.util;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Class that links columns name to attribute of a csv file.
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class AttributesLinker {

    @FXML
    AnchorPane anchor;
    @FXML
    GridPane grid;

    private final ArrayList<String> names = new ArrayList<>();
    private final ArrayList<TextField> texts = new ArrayList<>();
    private String theme;

    public ArrayList<String> getNames() {
        return names;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public AnchorPane getAnchor() {
        return anchor;
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> anchor.requestFocus());
    }

    /**
     * Adds labels to window based on the attribute's number
     *
     * @param totalAttributes   number of attributes
     */
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

    /**
     * Saves names entered by user to associate with columns
     */
    @FXML
    private void saveAssociation() {
        DialogBuilder dialogBuilder = new DialogBuilder(theme);
        ArrayList<String> attributes = new ArrayList<>();
        texts.forEach(t -> attributes.add(t.getText()));
        if(texts.stream().noneMatch(t -> t.getText().equals(""))) {
            if (!checkDuplicates(attributes)) {
                names.add("time");
                for (TextField n : texts)
                    names.add(n.getText());
                Stage stage = (Stage) anchor.getScene().getWindow();
                stage.close();
            } else {
                dialogBuilder.warning("Insert columns with different names");
                texts.forEach(t -> t.setText(""));
            }
        } else dialogBuilder.warning("Insert all columns names");
    }

    /**
     * Checks if into an arrayList there are duplicates
     *
     * @param array   arrayList to check
     * @return        true if there are duplicates, false if there aren't
     */
    private boolean checkDuplicates(ArrayList<String> array) {
        String tmp;
        for (int i = 0; i < array.size(); i++) {
            tmp = array.get(i);
            for (int j = i+1; j< array.size(); j++)
                if (array.get(j).equals(tmp))
                    return true;
        }
        return false;
    }
}
