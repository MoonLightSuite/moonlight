package eu.quanticol.moonlight.gui.graph;

import eu.quanticol.moonlight.gui.chart.JavaFXChartController;
import eu.quanticol.moonlight.gui.filter.JavaFXFiltersController;
import eu.quanticol.moonlight.gui.JavaFXMainController;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
import eu.quanticol.moonlight.gui.util.SimpleMouseManager;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.graphstream.graph.Graph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.view.Viewer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for graphs
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class JavaFXGraphController {

    @FXML
    Label graphType;
    @FXML
    BorderPane borderPane;
    @FXML
    Label infoNode;
    @FXML
    JavaFXNodesTableController nodeTableComponentController;
    @FXML
    JavaFXFiltersController filtersComponentController;
    @FXML
    Slider slider;
    @FXML
    SubScene scene;

    private List<TimeGraph> graphList = new ArrayList<>();
    private Graph currentGraph;
    private String theme;
    private JavaFXChartController chartController;
    private GraphController graphController;
    private JavaFXMainController mainController;
    private boolean csvRead = false;
    private GraphType graphVisualization;
    private final ArrayList<FxViewer> viewers = new ArrayList<>();
    private final Label label = new Label();
    private final ArrayList<Double> time = new ArrayList<>();

    private final ChangeListener<? super Number> sliderListener = (obs, oldValue, newValue) -> {
        label.setText(String.valueOf(this.time.get(newValue.intValue())));
        changeGraphView(String.valueOf(this.time.get(newValue.intValue())));
    };

    public boolean getCsvRead() {
        return this.csvRead;
    }

    public List<TimeGraph> getGraphList() {
        return graphList;
    }


    public void setTheme(String theme) {
        this.theme = theme;
    }

    public void injectMainController(JavaFXMainController mainController, JavaFXChartController chartComponentController) {
        this.mainController = mainController;
        this.chartController = chartComponentController;
        initialize();
    }

    private void initialize() {
        this.graphController = SimpleGraphController.getInstance();
        this.nodeTableComponentController.injectGraphController(graphController);
        this.filtersComponentController.injectGraphController(mainController, this, chartController);
    }

    /**
     * Open the explorer to choose a file
     *
     * @param extensions extension of a file to choose
     *
     * @return the file chosen
     */
    private File open(String description, String extensions) {
        FileChooser fileChooser = new FileChooser();
        Stage stage = (Stage) mainController.getRoot().getScene().getWindow();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(description, extensions);
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser.showOpenDialog(stage);
    }

    /**
     * Opens explorer with only .tra files
     */
    public void openTraExplorer() {
        System.setProperty("org.graphstream.ui", "javafx");
        File file = open("TRA Files", "*.tra");
        if (file != null) {
            resetAll();
            createGraphFromFile(file);
            nodeTableComponentController.initTable();
        } else {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.info("No file chosen.");
        }

    }

    /**
     * Opens explorer with only .csv files
     */
    public void openCSVExplorer() {
        chartController.reset();
        File file = open("CSV Files", "*.csv");
        if (file != null) {
            try {
                readCSV(file);
                if (graphVisualization.equals(GraphType.DYNAMIC))
                    chartController.createDataFromGraphs(graphList);
            } catch (Exception e) {
                DialogBuilder d = new DialogBuilder(mainController.getTheme());
                e.printStackTrace();
                d.error("Failed to load chart data.");
            }
        } else {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.info("No file chosen.");
        }
    }

    /**
     * Opens explorer with only .csv files
     */
    public void openConstantCsvExplorer() {
        chartController.reset();
        File file = open("CSV Files", "*.csv");
        if (file != null) {
            try {
                readConstantCSV(file);
            } catch (Exception e) {
                DialogBuilder d = new DialogBuilder(mainController.getTheme());
                e.printStackTrace();
                d.error("Failed to load chart data.");
            }
        } else {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.info("No file chosen.");
        }
    }

    /**
     * Reads a .csv file as a file with constants attributes
     *
     */
    private void readConstantCSV(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            if (graphVisualization.equals(GraphType.STATIC))
            graphController.createPositions(line);
            if (graphVisualization.equals(GraphType.DYNAMIC))
                graphController.createNodesVector(line);
        }
        chartController.initConstantChart(file);
        this.csvRead =true;
}


    /**
     * Reset all lists and info
     */
    private void resetAll() {
        viewers.clear();
        graphList.clear();
        time.clear();
        nodeTableComponentController.resetTable();
        filtersComponentController.resetFiltersNewFile();
        chartController.reset();
        resetSlider();
        csvRead = false;
        infoNode.setText(" ");
    }

    private void resetSlider() {
        slider.valueProperty().removeListener(sliderListener);
        slider.setLabelFormatter(null);
        label.setText("");
        slider.setMin(0);
        slider.setMax(50);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setValue(slider.getMin());
    }

    /**
     * Read a .csv file to get info about nodes
     *
     * @param file .csv file
     */
    private void readCSV(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        if (graphVisualization.equals(GraphType.STATIC)) {
            getStaticAttributesFromCsv(br);
        } else
            getDynamicAttributesFromCsv(br);
        this.csvRead = true;

    }

    /**
     * Creates vectors for every node in a single instant
     *
     * @param line a string of a time instant with all info about nodes
     */
    private void createNodesVector(String line) {
        graphController.createNodesVector(line);
    }

    /**
     * Sets positions of nodes
     */
    private void createPositions(String line) {
        graphController.createPositions(line);
    }

    /**
     * Reads attributes of nodes of a static graph from a file and creates positions and charts
     */
    private void getStaticAttributesFromCsv(BufferedReader br) throws IOException {
        String line = br.readLine();
        if (line != null) {
            createPositions(line);
            resetCharts();
            createSeriesFromStaticGraph(line);
            while (((line = br.readLine()) != null)) {
                addLineDataToSeries(line);
            }
            chartController.initStatic();
        }
    }

    /**
     * For each line of a file adds data to the charts
     */
    private void addLineDataToSeries(String line) {
        chartController.addLineDataToSeries(line);
    }

    /**
     * Creates the series of charts corresponding to nodes of a static graph
     */
    private void createSeriesFromStaticGraph(String line) {
        chartController.createSeriesFromStaticGraph(line);
    }

    /**
     * Gets attributes of nodes of a dynamic graph from a file
     */
    private void getDynamicAttributesFromCsv(BufferedReader br) throws IOException {
        graphController.setGraphList(graphList);
        String line;
        while (((line = br.readLine()) != null)) {
            createNodesVector(line);
        }
        graphList = graphController.getGraphList();
    }

    private void resetCharts() {
        chartController.resetCharts();
    }

    /**
     * Create a graph from a file
     *
     * @param file file to read
     */
    private void createGraphFromFile(File file) {
        try {
            graphController.setGraphList(graphList);
            graphVisualization = graphController.createGraphFromFile(file);
            graphList = graphController.getGraphList();
            for (TimeGraph g : graphList) {
                g.getGraph().setAttribute("ui.stylesheet", "url('" + this.theme + "')");
            }
            if (graphVisualization.equals(GraphType.STATIC)) {
                slider.setDisable(true);
                showStaticGraph(graphController.getStaticGraph());
            } else if (graphVisualization.equals(GraphType.DYNAMIC)) {
                createViews();
                createTimeSlider();
                changeGraphView(String.valueOf(graphList.get(0).getTime()));
            }
        } catch (Exception e) {
            DialogBuilder dialogBuilder = new DialogBuilder(mainController.getTheme());
            e.printStackTrace();
            dialogBuilder.error("Failed to generate graph.");
        }
    }

    /**
     * Shows the static graph
     */
    private void showStaticGraph(Graph staticGraph) {
        if (staticGraph.hasAttribute("ui.stylesheet"))
            staticGraph.removeAttribute("ui.stylesheet");
        staticGraph.setAttribute("ui.stylesheet", "url('" + this.theme + "')");
        this.currentGraph = staticGraph;
        FxViewer v = new FxViewer(staticGraph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        v.addDefaultView(false, new FxGraphRenderer());
        if (this.csvRead)
            v.disableAutoLayout();
        else v.enableAutoLayout();
        FxViewPanel panel = (FxViewPanel) v.getDefaultView();
        scene.setRoot(panel);
        borderPane.setCenter(scene);
        scene.setVisible(true);
        scene.heightProperty().bind(borderPane.heightProperty());
        scene.widthProperty().bind(borderPane.widthProperty());
        SimpleMouseManager sm = new SimpleMouseManager(staticGraph, chartController);
        sm.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("LabelProperty")) {
                infoNode.setText(evt.getNewValue().toString());
            }
        });
        v.getDefaultView().setMouseManager(sm);

    }

    private void createTimeSlider() {
        slider.setDisable(false);
        time.clear();
        for (TimeGraph t : graphList) {
            time.add(t.getTime());
        }
        slider.setMin(time.get(0));
        slider.setMax(time.size() - 1);
        slider.setValue(time.get(0));
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Double object) {
                int index = object.intValue();
                return String.valueOf(time.get(index));
            }

            @Override
            public Double fromString(String string) {
                return Double.parseDouble(string);
            }
        });
        addListenersToSlider();
    }

    private void addListenersToSlider() {
        slider.applyCss();
        slider.layout();
        Pane thumb = (Pane) slider.lookup(".thumb");
        if (!thumb.getChildren().contains(label)) {
            thumb.getChildren().add(label);
            label.setTextAlignment(TextAlignment.CENTER);
            thumb.setPrefHeight(20);
        }
        slider.valueProperty().addListener(sliderListener);
        slider.setOnMousePressed(event -> borderPane.getScene().setCursor(Cursor.CLOSED_HAND));
        slider.setOnMouseReleased(event -> borderPane.getScene().setCursor(Cursor.DEFAULT));
    }


    /**
     * Changes visualization of a dynamic graph in time
     *
     * @param time instant chosen
     */
    private void changeGraphView(String time) {
        Optional<TimeGraph> g = graphList.stream().filter(timeGraph -> timeGraph.getTime() == Double.parseDouble(time)).findFirst();
        g.ifPresent(timeGraph -> changeView(timeGraph.getGraph(), Double.parseDouble(time)));
    }

    /**
     * @return current graph displayed
     */
    public Graph getCurrentGraph() {
        return currentGraph;
    }

    /**
     * Creates a viewer for each graph
     */
    private void createViews() {
        for (TimeGraph t : graphList) {
            FxViewer viewer = new FxViewer(t.getGraph(), FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
            viewer.addView(String.valueOf(t.getTime()), new FxGraphRenderer());
            viewers.add(viewer);
        }
    }


    /**
     * Change viewer in order to display a different graph
     *
     * @param graph graph to show
     * @param time  instant related to the graph
     */
    private void changeView(Graph graph, Double time) {
        setGraphAttribute(graph, time);
        Optional<FxViewer> fv = viewers.stream().filter(fxViewer -> fxViewer.getView(String.valueOf(time)) != null).findFirst();
        if (fv.isPresent()) {
            FxViewer v = fv.get();
            if (this.csvRead)
                v.disableAutoLayout();
            else v.enableAutoLayout();
            FxViewPanel panel = (FxViewPanel) v.getView(String.valueOf(time));
            scene.setRoot(panel);
            borderPane.setCenter(scene);
            scene.setVisible(true);
            scene.heightProperty().bind(borderPane.heightProperty());
            scene.widthProperty().bind(borderPane.widthProperty());
            SimpleMouseManager sm = new SimpleMouseManager(graph, time, chartController);
            sm.addPropertyChangeListener(evt -> {
                if (evt.getPropertyName().equals("LabelProperty")) {
                    infoNode.setText(evt.getNewValue().toString());
                }
            });
            v.getView(String.valueOf(time)).setMouseManager(sm);
        }
    }

    /**
     * Sets attributes to the graph displayed
     */
    private void setGraphAttribute(Graph graph, Double time) {
        Optional<TimeGraph> g = graphList.stream().filter(timeGraph -> timeGraph.getTime() == time).findFirst();
        g.ifPresent(timeGraph -> currentGraph = g.get().getGraph());
        if (graph.hasAttribute("ui.stylesheet"))
            graph.removeAttribute("ui.stylesheet");
        graph.setAttribute("ui.stylesheet", "url('" + this.theme + "')");
    }

    @FXML
    private void deselectFiltersTable() {
        filtersComponentController.getTableFilters().getSelectionModel().clearSelection();
    }

    @FXML
    private void deselectNodeTable() {
        nodeTableComponentController.nodesTable.getSelectionModel().clearSelection();
    }
}