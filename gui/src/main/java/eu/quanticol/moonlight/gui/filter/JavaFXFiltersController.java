package eu.quanticol.moonlight.gui.filter;

import eu.quanticol.moonlight.gui.chart.JavaFXChartController;
import eu.quanticol.moonlight.gui.graph.TimeGraph;
import eu.quanticol.moonlight.gui.graph.JavaFXGraphController;
import eu.quanticol.moonlight.gui.JavaFXMainController;
import eu.quanticol.moonlight.gui.io.FiltersLoader;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
import eu.quanticol.moonlight.gui.io.JsonFiltersLoader;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.graphstream.graph.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller of JavaFX for filters
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class JavaFXFiltersController {

    @FXML
    TextField text;
    @FXML
    MenuButton attribute;
    @FXML
    MenuButton operator;
    @FXML
    TableView<Filter> tableFilters;
    @FXML
    TableColumn<Filter, String> attributeColumn;
    @FXML
    TableColumn<Filter, String> operatorColumn;
    @FXML
    TableColumn<Filter, Double> valueColumn;
    @FXML
    TableColumn<Filter, Void> resetColumn;

    private JavaFXMainController mainController;
    private JavaFXGraphController graphController;
    private JavaFXChartController chartController;
    private final ArrayList<Node> nodes = new ArrayList<>();
    private FiltersController filtersController;
    private final FiltersLoader jsonFiltersLoader = new JsonFiltersLoader();
    private final ArrayList<FilterGroup> filterGroups = new ArrayList<>();

    public TableView<Filter> getTableFilters() {
        return tableFilters;
    }

    public void injectGraphController(JavaFXMainController mainController, JavaFXGraphController graphController, JavaFXChartController chartController) {
        this.mainController = mainController ;
        this.graphController = graphController;
        this.chartController = chartController;
    }

    /**
     * Assigns the text of the clicked menuItem to the menuButton.
     */
    @FXML
    public void initialize() {
        attribute.getItems().forEach(menuItem -> menuItem.setOnAction(event -> attribute.setText(menuItem.getText())));
        operator.getItems().forEach(menuItem -> menuItem.setOnAction(event -> operator.setText(menuItem.getText())));
        filtersController = SimpleFiltersController.getInstance();
    }

    /**
     * Resets texFields and buttons.
     */
    @FXML
    private void reset() {
        attribute.setText("Attribute");
        operator.setText("Operator");
        text.clear();
    }

    /**
     * Checks if the value entered by the user contains only numbers.
     *
     * @return   string of value
     */
    public String getValue(){
        String value = text.getText();
        if(value.contains(","))
            value = value.replace(",",".");
        if (value.matches("[0-9.]*"))
            return value;
        else
            throw new IllegalArgumentException("Value must contains only numbers.");
    }

    /**
     * Matches the {@link Filter} fields with the columns of the table.
     */
    private void setCellValueFactory() {
        attributeColumn.setCellValueFactory(filter -> new SimpleObjectProperty<>(filter.getValue().getAttribute()));
        operatorColumn.setCellValueFactory(filter -> new SimpleObjectProperty<>(filter.getValue().getOperator()));
        valueColumn.setCellValueFactory(filter -> new SimpleObjectProperty<>(filter.getValue().getValue()));
        addButtonToTable();
    }

    /**
     * Adds a delete button for each row of table.
     */
    private void addButtonToTable() {
        Callback<TableColumn<Filter, Void>, TableCell<Filter, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Filter, Void> call(TableColumn<Filter, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button();
                    {
                        btn.setOnAction((ActionEvent event) -> {
                            Filter filter = getTableView().getItems().get(getIndex());
                            deleteFilter(filter);
                        });
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty)
                            setGraphic(null);
                        else {
                            setGraphic(btn);
                            btn.setGraphic(new ImageView((Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("images/remove.png"))).toString()));                        }
                    }
                };
            }
        };
        resetColumn.setCellFactory(cellFactory);
    }

    /**
     * Deletes a {@link Filter} from graph and filtersTable.
     *
     * @param filter filter to delete
     */
    @FXML
    private void deleteFilter(Filter filter) {
        ObservableList<Filter> filters = tableFilters.getItems();
        if (filters.size() == 1)
            resetFilters();
        else {
            filters.remove(filter);
            nodes.clear();
            filters.forEach(this::checkFilter);
        }
    }

    /**
     * Resets all filters added from graphs and filtersTable.
     */
    @FXML
    public void resetFilters() {
        graphController.getGraphList().forEach(timeGraph -> {
            int countNodes = timeGraph.getGraph().getNodeCount();
            for (int i = 0; i < countNodes; i++)
                timeGraph.getGraph().getNode(i).removeAttribute("ui.class");
        });
        tableFilters.getItems().clear();
        nodes.clear();
        chartController.selectAllSeries();
    }

    /**
     * Resets filters from table.
     */
    public void resetFiltersNewFile() {
        tableFilters.getItems().clear();
        nodes.clear();
    }

    /**
     * @return ArrayList of times, takes from all {@link TimeGraph}.
     */
    private ArrayList<Double> getTimes() {
        ArrayList<Double> times = new ArrayList<>();
        graphController.getGraphList().forEach(timeGraph -> times.add(timeGraph.getTime()));
        return times;
    }

    /**
     * Applies filter entered from user.
     */
    @FXML
    private void saveFilter() {
        DialogBuilder dialogBuilder = new DialogBuilder(mainController.getTheme());
        try {
            if (graphController.getCsvRead()) {
                if (!(text.getText().equals("") || attribute.getText().equals("Attribute") || operator.getText().equals("Operator"))) {
                    double value = Double.parseDouble(getValue());
                    Filter filter = new SimpleFilter(attribute.getText(), operator.getText(), value);
                    addFilter(filter);
                } else if (!tableFilters.getItems().isEmpty()) {
                    chartController.deselectAllSeries();
                    nodes.forEach(node -> chartController.selectOneSeries(node.getId()));
                }
            } else
                dialogBuilder.warning("No attributes found.");
            reset();
        } catch (Exception e) {
            reset();
            dialogBuilder.error("Failed saving filter.");
        }
    }

    /**
     * Opens a dialog to insert a name of filters and save them on file.
     */
    @FXML
    private void openSaveDialogInput() {
        DialogBuilder d = new DialogBuilder(mainController.getTheme());
        ArrayList<Filter> filters = new ArrayList<>(tableFilters.getItems());
        if (!filters.isEmpty()) {
            Optional<String> result = setDialog("Save filters in Json file");
            result.ifPresent(name -> {
                try {
                    String filterInfo = jsonFiltersLoader.saveToJson(filters, filterGroups, name);
                    d.info(filterInfo);
                } catch (IOException e) {
                    d.error("Failed saving filters on file.");
                }
            });
        } else
            d.warning("No filters in table.");
    }

    /**
     * Opens a dialog to insert a name of filters and import them from file.
     */
    @FXML
    private void openImportDialogInput() {
        DialogBuilder d = new DialogBuilder(mainController.getTheme());
        if (graphController.getCsvRead()) {
            Optional<String> result = setDialog("Import filters from Json file");
            result.ifPresent(name -> {
                try {
                    importFilters(d, name);
                } catch (Exception e) {
                    d.error("Failed importing filters from file.");
                }
            });
        }
        else
            d.warning("Insert attributes.");
    }

    /**
     * Import filters from file.
     *
     * @param d     dialogBuilder
     * @param name  name of group of filters to import
     */
    private void importFilters(DialogBuilder d, String name) throws IOException {
        ArrayList<Filter> filters = new ArrayList<>();
        tableFilters.getItems().clear();
        if (graphController.getCsvRead()) {
            if (jsonFiltersLoader.getFromJson(name, filters)) {
                tableFilters.getItems().addAll(filters);
                setCellValueFactory();
                tableFilters.getItems().forEach(this::checkFilter);
            } else
                d.warning("Filter not found.");
        } else
            d.warning("Insert attributes!");
    }

    /**
     *Sets the properties of a dialogInput.
     *
     * @param title title of dialog
     * @return      dialog showed
     */
    private Optional<String> setDialog(String title){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.getDialogPane().getStylesheets().add(mainController.getTheme());
        dialog.setHeaderText("Enter filters name:");
        dialog.setContentText("Name:");
        return dialog.showAndWait();
    }

    /**
     * Adds filter to graph and table.
     *
     * @param filter {@link Filter} to add
     */
    public void addFilter(Filter filter) {
        ArrayList<Filter> filters = new ArrayList<>(tableFilters.getItems());
        if (!filters.contains(filter)) {
            filtersController.validationFilter(filter,filters);
            tableFilters.getItems().add(filter);
            checkFilter(filter);
            reset();
            setCellValueFactory();
        } else
            throw new IllegalArgumentException("Filter already present.");
    }

    /**
     * Checks if there are nodes in the graph that correspond to it.
     * @param f filter to check
     */
    private void checkFilter(Filter f) {
        chartController.deselectAllSeries();
        ArrayList<Filter> filters = new ArrayList<>(tableFilters.getItems());
        graphController.getGraphList().forEach(g -> filtersController.checkFilter(f,filters,nodes,g,getTimes()));
        nodes.forEach(node -> chartController.selectOneSeries(node.getId()));
    }
}