package eu.quanticol.moonlight.gui.graph;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;

/**
 * Class controller of a table of nodes
 */
public class JavaFXNodesTableController {

    @FXML
    TableView<String> nodesTable;
    @FXML
    TableColumn<String, String> nameColumn;
    private GraphController graphController;

    public void injectGraphController(GraphController graphController) {
        this.graphController = graphController;
    }

    /**
     * Inits the values in the table
     */
    public void initTable() {
        List<Integer> items = new ArrayList<>();
        for (int i = 0; i < graphController.getTotNodes(); i++) {
            items.add(i);
        }
        nodesTable.getItems().clear();
        for (int n : items) {
            nodesTable.getItems().add("Node "+ n);
        }
        setCellValueFactory();
    }

    private void setCellValueFactory() {
        nameColumn.setCellValueFactory(value -> new SimpleObjectProperty<>(value.getValue()));
    }

    public void resetTable() {
        nodesTable.getItems().clear();
    }
}