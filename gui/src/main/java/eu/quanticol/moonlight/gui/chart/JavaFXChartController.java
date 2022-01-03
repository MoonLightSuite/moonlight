package eu.quanticol.moonlight.gui.chart;

import eu.quanticol.moonlight.gui.JavaFXMainController;
import eu.quanticol.moonlight.gui.graph.JavaFXGraphController;
import eu.quanticol.moonlight.gui.graph.TimeGraph;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
import eu.quanticol.moonlight.gui.util.LineChartWithMarkers;
import eu.quanticol.moonlight.gui.util.LogarithmicAxis;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Controller of JavaFX for a chart
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class JavaFXChartController {

    @FXML
    MenuButton attribute;
    @FXML
    NumberAxis yAxis = new NumberAxis();
    @FXML
    NumberAxis xAxis = new NumberAxis();
    @FXML
    NumberAxis xLAxis = new NumberAxis();
    @FXML
    LogarithmicAxis yLAxis = new LogarithmicAxis();
    @FXML
    NumberAxis yCAxis = new NumberAxis();
    @FXML
    NumberAxis xCAxis = new NumberAxis();
    @FXML
    ListView<CheckBox> list;
    @FXML
    LineChartWithMarkers<Number, Number> lineChartLog = new LineChartWithMarkers<>(xLAxis, yLAxis);
    @FXML
    LineChartWithMarkers<Number, Number> lineChart = new LineChartWithMarkers<>(xAxis, yAxis);
    @FXML
    LineChartWithMarkers<Number, Number> constantChart = new LineChartWithMarkers<>(xCAxis, yCAxis);
    @FXML
    TableView<Series<Number, Number>> variables;
    @FXML
    TableColumn<Series<Number, Number>, String> nameVColumn;
    @FXML
    TableColumn<Series<Number, Number>, Number> minColumn;
    @FXML
    TableColumn<Series<Number, Number>, Number> maxColumn;
    @FXML
    RadioButton linear = new RadioButton();
    @FXML
    RadioButton logarithmic = new RadioButton();
    @FXML
    Label attributes;
    @FXML
    RowConstraints labelPane;
    @FXML
    ScrollPane scrollPane;

    private JavaFXMainController mainController;
    private JavaFXGraphController javaFXGraphController;
    private final ChartBuilder cb = new SimpleChartBuilder();
    private int indexOfAttributes = 1;

    private ChartVisualization chartVisualization;

    public void injectMainController(JavaFXMainController mainController, JavaFXGraphController graphController) {
        this.mainController = mainController;
        this.javaFXGraphController = graphController;
    }

    public MenuButton getAttribute() {
        return attribute;
    }

    public void setAttributes(String attribute) {
        attributes.setText(attribute);
    }

    public int getIndexOfAttributes() {
        return indexOfAttributes;
    }

    public void setIndexOfAttributes(int indexOfAttributes) {
        this.indexOfAttributes = indexOfAttributes;
    }


    public ChartVisualization getGraphVisualization() {
        return chartVisualization;
    }

    public void setGraphVisualization(ChartVisualization chartVisualization) {
        this.chartVisualization = chartVisualization;
    }

    /**
     * Create a chart from a {@link TimeGraph} using a {@link ChartBuilder}
     *
     * @param timeGraph a {@link TimeGraph}
     */
    public void createDataFromGraphs(List<TimeGraph> timeGraph, int index) {
        try {
            lineChart.getData().addAll(cb.getSeriesFromNodes(timeGraph, index));
            lineChartLog.getData().addAll(cb.getSeriesFromNodes(timeGraph, index));
            linearSelected();
            init();
        } catch (Exception e) {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.error("Failed to load chart data. Open an other file.");
        }
    }

    /**
     * Adds a toolTip to all nodes of series
     *
     * @param l lineChart
     */
    private void showToolTip(LineChart<Number, Number> l) {
        for (Series<Number, Number> s : l.getData()) {
            for (XYChart.Data<Number, Number> d : s.getData()) {
                Tooltip t = new Tooltip(s.getName());
                t.setShowDelay(Duration.seconds(0));
                Tooltip.install(d.getNode(), t);
            }
        }
    }

    /**
     * Loads a vertical line to a lineChart
     *
     * @param lineChart lineChart which to add line
     */
    private void loadVerticalLine(LineChartWithMarkers<Number, Number> lineChart) {
        XYChart.Data<Number, Number> verticalMarker = new XYChart.Data<>(0, 0);
        lineChart.addVerticalValueMarker(verticalMarker);
        ArrayList<Double> time = javaFXGraphController.getTime();
        javaFXGraphController.getSlider().valueProperty().addListener((obs, oldValue, newValue) -> {
            Double value = javaFXGraphController.nearest(time, newValue.doubleValue());
            Platform.runLater(() -> verticalMarker.setXValue(value));
        });
    }

    /**
     * Removes a vertical line to all lineCharts
     */
    public void removeVerticalLine() {
        lineChart.removeVerticalValueMarker();
        lineChartLog.removeVerticalValueMarker();
        constantChart.removeVerticalValueMarker();
    }

    /**
     * Creates series from attributes in a file of a static graph
     *
     * @param line line to read
     */
    public void createSeriesFromStaticGraph(String line, int index) {
        lineChart.getData().addAll(cb.getSeriesFromStaticGraph(line, cb.getListLinear(), true, index));
        lineChartLog.getData().addAll(cb.getSeriesFromStaticGraph(line, cb.getListLog(), false, index));
    }

    /**
     * For a line of a file with attributes, add data to the charts
     *
     * @param line to read
     */
    public void addLineDataToSeries(String line, int index) {
        String[] attributes = line.split(",");
        cb.addAttributes(attributes);
        javaFXGraphController.getFiltersComponentController().getFiltersController().addAttributes(attributes);
        cb.addLineData(lineChart.getData().stream().toList(), attributes, index);
        cb.addLineData(lineChartLog.getData().stream().toList(), attributes, index);
    }

    private void init() {
        initLists();
        showToolTip(lineChart);
        showToolTip(lineChartLog);
        loadVerticalLine(lineChart);
        loadVerticalLine(lineChartLog);
    }

    public void initStatic() {
        initLists();
        showToolTip(lineChart);
        showToolTip(lineChartLog);
        addListener(lineChart);
        addListener(lineChartLog);
    }

    /**
     * Writes on a label the attributes of a node clicked in the charts
     *
     * @param chart lineChart
     */
    public void addListener(LineChart<Number, Number> chart) {
        ArrayList<String> columnsAttributes = javaFXGraphController.getColumnsAttributes();
        int size = columnsAttributes.size() - 1;
        for (Series<Number, Number> s : chart.getData()) {
            for (XYChart.Data<Number, Number> d : s.getData()) {
                d.getNode().setOnMouseClicked(event -> {
                    int id = Integer.parseInt(s.getName().substring(5));
                    Double time = d.getXValue().doubleValue();
                    String v = d.getYValue().toString();
                    int index = columnsAttributes.indexOf(getAttribute().getText());
                    for (ArrayList<String> a : cb.getAttributes())
                        if (Double.valueOf(a.get(0)).equals(time) && (a.get((id * size) + index).replaceAll("\\s+", "")).equals(v)) {
                            StringBuilder toShow = new StringBuilder("Node " + id + " attributes: ");
                            for (int i = 1; i < columnsAttributes.size(); i++) {
                                toShow.append(columnsAttributes.get(i));
                                toShow.append(": ").append(a.get(i + (size * id))).append(", ");
                            }
                            toShow.deleteCharAt(toShow.length() - 2);
                            attributes.setText(String.valueOf(toShow));
                            labelPane.setPrefHeight(35);
                        }
                });
            }
        }
    }

    @FXML
    public void clearLabel(MouseEvent event) {
        if (!event.getTarget().getClass().equals(StackPane.class)) {
            attributes.setText(" ");
            labelPane.setPrefHeight(0);
        }
    }

    public void clearMenuButton() {
        attribute.setText("Attribute");
        attribute.getItems().clear();
    }

    /**
     * Creates a listView with radioButtons for choose the attribute's series
     *
     * @param names names of attributes
     */
    public void loadAttributesList(ArrayList<String> names) {
        ArrayList<String> attributes = new ArrayList<>();
        ArrayList<MenuItem> menuItems = new ArrayList<>();
        for (int i = 1; i <= names.size() - 1; i++)
            attributes.add(names.get(i));
        attributes.forEach(a -> {
            MenuItem menuItem = new MenuItem();
            menuItem.setText(a);
            menuItems.add(menuItem);
        });
        this.attribute.getItems().addAll(menuItems);
        attribute.getItems().forEach(menuItem -> menuItem.setOnAction(event -> changeChartSeries(menuItem)));
        javaFXGraphController.getFiltersComponentController().loadAttributes();
    }

    /**
     * Changes series into chart based on the attribute selected by the user
     *
     * @param menuItem menuItem selected
     */
    private void changeChartSeries(MenuItem menuItem) {
        attribute.setText(menuItem.getText());
            if (javaFXGraphController.getGraphList().size() == 0) {
                if (constantChart.isVisible()) {
                    createDataFromConstantGraph();
                    addListener(constantChart);
                } else {
                    createDataFromStaticGraph();
                    initStatic();
                }
            } else {
                if (constantChart.isVisible())
                    createDataFromConstantGraph();
                else
                    createDataFromDynamicGraph();
            }
            javaFXGraphController.getFiltersComponentController().changeAttributeSaveFilters();
    }

    /**
     * Initializes the two charts
     */
    @FXML
    public void initialize() {
        lineChartLog.setVisible(false);
        lineChartLog.setAnimated(false);
        constantChart.setVisible(false);
        constantChart.setAnimated(false);
        lineChart.setAnimated(false);
    }

    @FXML
    private void logarithmicSelected() {
        lineChart.setVisible(false);
        constantChart.setVisible(false);
        lineChartLog.setVisible(true);
        linear.setSelected(false);
        logarithmic.requestFocus();
        logarithmic.setSelected(true);
        linear.setVisible(true);
        logarithmic.setVisible(true);
    }

    @FXML
    public void linearSelected() {
        lineChartLog.setVisible(false);
        constantChart.setVisible(false);
        lineChart.setVisible(true);
        linear.setSelected(true);
        linear.requestFocus();
        logarithmic.setSelected(false);
        linear.setVisible(true);
        logarithmic.setVisible(true);
    }

    private void initLists() {
        initVariablesList();
        showList();
    }

    /**
     * Reloads chart of a constant graph from file .csv
     */
    private void createDataFromConstantGraph() {
        resetChartsWithoutMarker();
        deselectInconstantCharts();
        try {
            constantChart.getData().addAll(cb.createSeriesForConstantChart(javaFXGraphController.getCsv(), javaFXGraphController.getColumnsAttributes().indexOf(attribute.getText())));
            indexOfAttributes = javaFXGraphController.getColumnsAttributes().indexOf(attribute.getText());
        } catch (IOException e) {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.error("Failed to load chart data");
        }
        initLists();
        showToolTip(constantChart);
        loadVerticalLine(constantChart);
    }

    /**
     * Reloads chart of a static graph from file .csv
     */
    private void createDataFromStaticGraph() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(javaFXGraphController.getCsv()));
            String line = br.readLine();
            if (line != null) {
                if (line.contains("time"))
                    line = br.readLine();
                resetChartsWithoutMarker();
                createSeriesFromStaticGraph(line, javaFXGraphController.getColumnsAttributes().indexOf(attribute.getText()));
                do
                    addLineDataToSeries(line, javaFXGraphController.getColumnsAttributes().indexOf(attribute.getText()));
                while (((line = br.readLine()) != null));
                indexOfAttributes = javaFXGraphController.getColumnsAttributes().indexOf(attribute.getText());
            }
        } catch (Exception e) {
            DialogBuilder dialogBuilder = new DialogBuilder(mainController.getTheme());
            dialogBuilder.error("Failed to load chart data");
        }
    }

    /**
     * Reloads chart of a dynamic graph from file .csv
     */
    private void createDataFromDynamicGraph() {
        resetChartsWithoutMarker();
        lineChart.getData().addAll(cb.getSeriesFromNodes(javaFXGraphController.getGraphList(), javaFXGraphController.getColumnsAttributes().indexOf(attribute.getText()) - 1));
        lineChartLog.getData().addAll(cb.getSeriesFromNodes(javaFXGraphController.getGraphList(), javaFXGraphController.getColumnsAttributes().indexOf(attribute.getText()) - 1));
        indexOfAttributes = javaFXGraphController.getColumnsAttributes().indexOf(attribute.getText()) - 1;
        init();
    }

    /**
     * Initialize the table about series info, as min and max value
     */
    private void initVariablesList() {
        List<Series<Number, Number>> items = new ArrayList<>();
        if (lineChart.isVisible() || lineChartLog.isVisible())
            initList(items, lineChart);
        else initList(items, constantChart);
        setMinMaxValueFactory();
    }

    private void initList(List<Series<Number, Number>> items, LineChart<Number, Number> chart) {
        chart.getData().forEach(e -> {
            if (e != null)
                items.add(e);
        });
        variables.getItems().clear();
        for (Series<Number, Number> m : items) {
            variables.getItems().add(m);
        }
    }

    public void reset() {
        clearChartData();
        this.removeVerticalLine();
    }

    private void clearChartData() {
        this.lineChartLog.getData().clear();
        this.lineChart.getData().clear();
        this.constantChart.getData().clear();
        this.list.getItems().clear();
        this.variables.getItems().clear();
        variables.setDisable(false);
    }

    public void resetCharts() {
        cb.clearList();
        reset();
    }

    public void resetChartsWithoutMarker() {
        cb.clearList();
        clearChartData();
    }

    /**
     * Select all series and checkbox
     */
    @FXML
    public void selectAllSeries() {
        list.getItems().forEach(checkBox -> checkBox.setSelected(true));
    }

    /**
     * Initialize all checkbox in a list and their listener
     */
    private void showList() {
        if (list != null && !list.getItems().isEmpty())
            list.getItems().clear();
        final ObservableList<CheckBox> variables = FXCollections.observableArrayList();
        if (lineChart.isVisible() || lineChartLog.isVisible())
            checkBoxInit(variables, lineChart);
        else if (constantChart.isVisible())
            checkBoxInit(variables, constantChart);
        if (!variables.isEmpty())
            list.getItems().addAll(variables);
    }

    private void checkBoxInit(ObservableList<CheckBox> variables, LineChart<Number, Number> chart) {
        for (Series<Number, Number> series : chart.getData()) {
            CheckBox ck = new CheckBox(series.getName());
            ck.setSelected(true);
            ck.selectedProperty().addListener((observable, oldValue, newValue) -> {
                ck.setSelected(!oldValue);
                changeVisibility(ck.getText());
            });
            variables.add(ck);
        }
    }

    /**
     * Change visibility of a series in all charts based on its name
     *
     * @param name name of the series
     */
    private void changeVisibility(String name) {
        if (lineChart.isVisible() || lineChartLog.isVisible()) {
            changeSingleChartVisibility(name, lineChart);
            changeSingleChartVisibility(name, lineChartLog);
        } else
            changeSingleChartVisibility(name, constantChart);
    }

    /**
     * Change visibility of a series in a single chart based on its name
     *
     * @param name      name of the series
     * @param lineChart chart
     */
    private void changeSingleChartVisibility(String name, LineChart<Number, Number> lineChart) {
        new Thread(() -> {
            try {
                Thread.sleep(500);
                Platform.runLater(() -> lineChart.getData().forEach(series -> {
                    if (series.getName().equals(name)) {
                        series.getNode().setVisible(!series.getNode().isVisible());
                        series.getData().forEach(data -> {
                            if (data.getNode() != null)
                                data.getNode().setVisible(!data.getNode().isVisible());
                        });
                    }
                }));
            } catch (InterruptedException e) {
                DialogBuilder d = new DialogBuilder(mainController.getTheme());
                d.error("Failed updating chart.");
            }
        }).start();
    }

    /**
     * Select only one series in all charts. The series to be displayed must be only one.
     *
     * @param seriesName name of the series
     */
    public void selectOnlyOneSeries(String seriesName) {
        list.getItems().forEach(checkBox -> checkBox.setSelected(checkBox.getText().equals(seriesName)));
    }

    /**
     * Select one series in all charts
     *
     * @param seriesName name of the series
     */
    public void selectOneSeries(String seriesName) {
        list.getItems().forEach(checkBox -> {
            if (checkBox.getText().equals("Node " + seriesName))
                checkBox.setSelected(true);
        });
    }

    /**
     * Deselect all series in all charts
     */
    @FXML
    public void deselectAllSeries() {
        list.getItems().forEach(checkBox -> checkBox.setSelected(false));
    }

    /**
     * Get min and max of the value of a series and put them in the table
     */
    private void setMinMaxValueFactory() {
        nameVColumn.setCellValueFactory(value -> new SimpleObjectProperty<>(value.getValue().getName()));
        minColumn.setCellValueFactory(value -> new SimpleObjectProperty<>(getMinSeries(value.getValue())));
        maxColumn.setCellValueFactory(value -> new SimpleObjectProperty<>(getMaxSeries(value.getValue())));
    }

    /**
     * Return the min value of a series
     *
     * @param series all series
     *
     * @return min value
     */
    public Number getMinSeries(Series<Number, Number> series) {
        OptionalDouble d = series.getData().stream().mapToDouble(num -> num.getYValue().doubleValue()).min();
        if (d.isPresent())
            return d.getAsDouble();
        return 0;
    }

    /**
     * Return the max value of a series
     *
     * @param series all series
     *
     * @return max value
     */
    public Number getMaxSeries(Series<Number, Number> series) {
        OptionalDouble d = series.getData().stream().mapToDouble(num -> num.getYValue().doubleValue()).max();
        if (d.isPresent())
            return d.getAsDouble();
        return 0;
    }

    @FXML
    private void deselectSeriesList() {
        list.getSelectionModel().clearSelection();
    }

    @FXML
    private void deselectSeriesTable() {
        variables.getSelectionModel().clearSelection();
    }

    public void initConstantChart(File file) throws IOException {
        resetCharts();
        deselectInconstantCharts();
        constantChart.getData().addAll(cb.createSeriesForConstantChart(file, indexOfAttributes));
        initLists();
        showToolTip(constantChart);
        loadVerticalLine(constantChart);
    }

    public void addListenerConstantChart() {
        addListener(this.constantChart);
    }

    private void deselectInconstantCharts() {
        lineChart.setVisible(false);
        lineChartLog.setVisible(false);
        linear.setVisible(false);
        logarithmic.setVisible(false);
        constantChart.setVisible(true);
    }
}