package eu.quanticol.moonlight.gui.util;

import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.util.ArrayList;

public class PositionsLinker {

    @FXML
    MenuButton x = new MenuButton();
    @FXML
    MenuButton y = new MenuButton();

    private ArrayList<String> attributes = new ArrayList<>();
    private String columnX = null;
    private String columnY = null;
    private String theme;
    private Stage stage;

    public String getColumnX() {
        return columnX;
    }

    public String getColumnY() {
        return columnY;
    }

    public void addColumnsToMenuButtons() {
        for (int i = 1; i < attributes.size(); i++) {
            MenuItem menuItem1 = new MenuItem(attributes.get(i));
            MenuItem menuItem2 = new MenuItem(attributes.get(i));
            x.getItems().add(menuItem1);
            y.getItems().add(menuItem2);
        }
        x.getItems().forEach(menuItem -> menuItem.setOnAction(event -> x.setText(menuItem.getText())));
        y.getItems().forEach(menuItem -> menuItem.setOnAction(event -> y.setText(menuItem.getText())));
    }

    @FXML
    private void saveColumns(){
        if(!(x.getText().equals("Column") || y.getText().equals("Column"))) {
                this.columnX = x.getText();
                this.columnY = y.getText();
                this.stage.close();
        }else {
            DialogBuilder dialogBuilder = new DialogBuilder(theme);
            dialogBuilder.error("Insert columns!");
        }
    }

    @FXML
    private void closeWindow(){
        this.stage.close();
    }

    @FXML
    private void reset(){
        x.setText("Column");
        y.setText("Column");
    }

    public void setAttributes(ArrayList<String> attributes) {
        this.attributes = attributes;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
