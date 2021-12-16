package eu.quanticol.moonlight.gui.graph;

import eu.quanticol.moonlight.gui.chart.JavaFXChartController;
import eu.quanticol.moonlight.gui.filter.JavaFXFiltersController;
import eu.quanticol.moonlight.gui.JavaFXMainController;
import eu.quanticol.moonlight.gui.io.*;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
import eu.quanticol.moonlight.gui.util.LinkAttributes;
import eu.quanticol.moonlight.gui.util.SimpleMouseManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.graphstream.graph.Graph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.view.Viewer;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controller of JavaFX for graphs
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
    private RunnableSlider runnable = null;
    private final FilesLoader jsonFilesLoader= new JsonFilesLoader();
    private ArrayList<String> columnsAttributes = new ArrayList<>();
    private String positionX = null;
    private String positionY = null;

    /**
     * Listener for slider that updates the label of slider thumb and the graph visualized
     */
    private final ChangeListener<? super Number> sliderListener = (obs, oldValue, newValue) -> {
        Double value = nearest(time, newValue.doubleValue());
        Platform.runLater(() -> {
            label.setText(String.valueOf(value));
            changeGraphView(String.valueOf(value));
        });
    };

    public ArrayList<Double> getTime() {
        return time;
    }

    public Slider getSlider() {
        return slider;
    }

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
        loadPlaySpaceBar();
    }

    /**
     * Open the explorer to choose a file
     *
     * @param description description of file to choose
     * @param extensions  extension of a file to choose
     * @return            the file chosen
     */
    private File open(String description, String extensions) {
        if(runnable != null && slider != null)
            runnable.shutdown();
        filtersComponentController.resetFilters();
        FileChooser fileChooser = new FileChooser();
        Stage stage = (Stage) mainController.getRoot().getScene().getWindow();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(description, extensions);
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser.showOpenDialog(stage);
    }

    /**
     * Opens explorer with only .tra files
     */
    public void openTRAExplorer() throws IOException {
        System.setProperty("org.graphstream.ui", "javafx");
        File file = open("TRA Files", "*.tra");
        loadTRA(file);
    }

    /**
     * Opens the chosen .tra file from recent
     */
    public void openRecentTRA(File file) throws IOException {
        System.setProperty("org.graphstream.ui", "javafx");
        loadTRA(file);
    }

    /**
     * Loads the .tra file
     *
     * @param file   file .tra
     */
    private void loadTRA(File file) throws IOException {
        if (file != null) {
            resetAll();
            createGraphFromFile(file);
            nodeTableComponentController.initTable();
            addRecentFile(file.getPath(), FileType.TRA);
        } else {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.info("No file chosen.");
        }
    }

    /**
     * Opens explorer with only .csv files for pieceWise linear visualization
     */
    public void openCSVExplorer() {
        File file = open("CSV Files", "*.csv");
        if (file != null) {
            loadCSV(file);
        } else {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.info("No file chosen.");
        }
    }

    /**
     * Opens the chosen .csv file from recent
     */
    public void openRecentCSV(File file){
        if (file != null) {
            loadCSV(file);
        } else {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.info("No file chosen.");
        }
    }

    /**
     * Loads the .csv file
     *
     * @param file   file .csv
     */
    //todo
    private void loadCSV(File file) {
        try {
            chartController.reset();
            readCSV(file);
            if (graphVisualization.equals(GraphType.DYNAMIC))
                chartController.createDataFromGraphs(graphList);
            addRecentFile(file.getPath(), FileType.CSV);
        } catch (Exception e) {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.error("Failed to load chart data.");
        }
    }

    /**
     * Opens explorer with only .csv files for constant stepWise visualization
     */
    //todo
    public void openConstantCsvExplorer() {
        File file = open("CSV Files", "*.csv");
        if (file != null) {
            try {
                chartController.reset();
                readConstantCSV(file);
                addRecentFile(file.getPath(), FileType.CSV);
            } catch (Exception e) {
                DialogBuilder d = new DialogBuilder(mainController.getTheme());
                d.error("Failed to load chart data.");
            }
        } else {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.info("No file chosen.");
        }
    }

    /**
     * Adds the file opened into a json file and into the recent list
     *
     * @param file      path of file
     * @param fileType  type of file
     */
    private void addRecentFile(String file, FileType fileType) throws IOException {
        jsonFilesLoader.saveToJson(file, fileType);
        mainController.getHomeController().loadFilesOnList();
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
        if (graphVisualization.equals(GraphType.STATIC))
            getStaticAttributesFromCsv(br);
            else
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
     *
     * @param line a string of a time instant with all info about nodes
     */
    private void createPositions(String line) {
        graphController.createPositions(line);
    }

    /**
     * Reads attributes of nodes of a static graph from a file and creates positions and charts
     *
     * @param br bufferedReader
     */
    private void getStaticAttributesFromCsv(BufferedReader br) throws IOException {
        String line = br.readLine();
        if (line != null) {
            if(line.contains("time")) {
                associateAttributesColumns(line);
            } else {
                openAssociateAttributesWindow(line);
                createPositions(line);
                resetCharts();
                createSeriesFromStaticGraph(line);
                while (((line = br.readLine()) != null))
                    addLineDataToSeries(line);
                chartController.initStatic();
            }
        }
    }

    private void openAssociateAttributesWindow(String line) {
        String[] split = line.split(";");
        int totalAttributes = (split.length - 1) / graphController.getTotNodes();
        openWindow(totalAttributes);
    }

    private void openWindow(int totalAttributes) {
        try {
            FXMLLoader fxmlLoader;
            fxmlLoader = new FXMLLoader(ClassLoader.getSystemClassLoader().getResource("fxml/associateAttributes.fxml"));
            Parent newRoot = fxmlLoader.load();
            Stage stage = new Stage();
            LinkAttributes controller = fxmlLoader.getController();
            initializeWindow(newRoot, stage, totalAttributes, controller);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void initializeWindow(Parent newRoot, Stage stage, int totalAttributes, LinkAttributes controller) {
        stage.setTitle("MoonLight");
        stage.initStyle(StageStyle.DECORATED);
        stage.setScene(new Scene(newRoot));
        Image icon = new Image(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("images/ML.png")).toString());
        stage.getIcons().add(icon);
        stage.showAndWait();
        GridPane gridpane = new GridPane();
        for(int i = 1; i <= totalAttributes; i++) {
            Label column = new Label("Column " + i);
            TextField name = new TextField("Insert name of column");
            gridpane.add(column, 0, i - 1); // column=1 row=0
            gridpane.add(name, 1, i - 1);
        }
        controller.getScroll().setContent(gridpane);
    }

    private void associateAttributesColumns(String line) {
        String[] split = line.split(";");
        ArrayList<String> array = new ArrayList<>(Arrays.asList(split));
        columnsAttributes = array;
        graphController.setColumnsAttributes(array);
    }

    /**
     * For each line of a file adds data to the charts
     *
     * @param line a string of a time instant with all info about nodes
     */
    private void addLineDataToSeries(String line) {
        chartController.addLineDataToSeries(line);
    }

    /**
     * Creates the series of charts corresponding to nodes of a static graph
     *
     * @param line a string of a time instant with all info about nodes
     */
    private void createSeriesFromStaticGraph(String line) {
        chartController.createSeriesFromStaticGraph(line);
    }

    /**
     * Gets attributes of nodes of a dynamic graph from a file
     *
     * @param br bufferedReader
     */
    private void getDynamicAttributesFromCsv(BufferedReader br) throws IOException {
        graphController.setGraphList(graphList);
        String line = br.readLine();
        if(line.contains("time")) {
            associateAttributesColumns(line);
        } else {
            openAssociateAttributesWindow(line);
            createNodesVector(line);
            while (((line = br.readLine()) != null))
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
            for (TimeGraph g : graphList)
                g.getGraph().setAttribute("ui.stylesheet", "url('" + this.theme + "')");
            createGraph();
        } catch (Exception e) {
            DialogBuilder dialogBuilder = new DialogBuilder(mainController.getTheme());
            dialogBuilder.error("Failed to generate graph.");
        }
    }

    /**
     * Creates a static or dynamic graph
     */
    private void createGraph() {
        if (graphVisualization.equals(GraphType.STATIC)) {
            slider.setDisable(true);
            showStaticGraph(graphController.getStaticGraph());
        } else if (graphVisualization.equals(GraphType.DYNAMIC)) {
            createViews();
            createTimeSlider();
            changeGraphView(String.valueOf(graphList.get(0).getTime()));
        }
    }

    /**
     * Shows the static graph
     *
     * @param staticGraph  static Graph
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
        setSceneProperties((FxViewPanel) v.getDefaultView());
        SimpleMouseManager sm = new SimpleMouseManager(staticGraph, chartController);
        sm.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("LabelProperty"))
                infoNode.setText(evt.getNewValue().toString());
        });
        v.getDefaultView().setMouseManager(sm);
    }

    /**
     * Sets the scene's properties
     *
     * @param panel fxViewPanel
     */
    private void setSceneProperties(FxViewPanel panel) {
        scene.setRoot(panel);
        borderPane.setCenter(scene);
        scene.setVisible(true);
        scene.heightProperty().bind(borderPane.heightProperty());
        scene.widthProperty().bind(borderPane.widthProperty());
    }

    private void createTimeSlider() {
        setSlider();
        slider.setLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Double n) {
                int index = n.intValue();
                return String.valueOf(time.get(index));
            }

            @Override
            public Double fromString(String string) {
                return Double.parseDouble(string);
            }
        });
        addListenersToSlider();
    }

    /**
     * Returns the nearest element of a double in an arraylist
     * @param time arraylist of double
     * @param index double to compare
     * @return the nearest number in the arraylist
     */
    public double nearest(ArrayList<Double> time, double index) {
        if(!time.isEmpty()) {
            double nearest = time.get(0);
            double current = Double.MAX_VALUE;
            for (Double d : time) {
                if (Math.abs(d - index) <= current) {
                    nearest = d;
                    current = Math.abs(d - index);
                }
            }
            return nearest;
        } else return 0;
    }

    /**
     * Sets values of slider
     */
    private void setSlider() {
        slider.setDisable(false);
        time.clear();
        for (TimeGraph t : graphList)
            time.add(t.getTime());
        slider.setMin(time.get(0));
        slider.setMax(time.get(time.size() - 1));
        slider.setValue(time.get(0));
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        toolTipSlider();
    }

    /**
     * Initialize tooltip for slider animation
     */
    private void toolTipSlider() {
        Tooltip t = new Tooltip("Press space bar to start/stop slider animation");
        t.setShowDelay(Duration.seconds(0));
        Tooltip.install(slider, t);
    }

    /**
     * Loads listener on spaceBar pressed
     */
    private void loadPlaySpaceBar() {
        runnable = new RunnableSlider(slider);
        AtomicBoolean pressed = new AtomicBoolean(false);
        Runnable run = () -> Platform.runLater(runnable);
        mainController.getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE) {
                if (!pressed.get()) {
                    runnable.restart();
                    Thread thread = new Thread(run);
                    thread.start();
                    pressed.set(true);
                } else {
                    runnable.shutdown();
                    pressed.set(false);
                }
            }
        });
    }

    /**
     * Listener for the slider. Changes view of graph with the selected time of slider
     */
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
            setSceneProperties((FxViewPanel) v.getView(String.valueOf(time)));
            SimpleMouseManager sm = new SimpleMouseManager(graph, time, chartController);
            sm.addPropertyChangeListener(evt -> {
                if (evt.getPropertyName().equals("LabelProperty"))
                    infoNode.setText(evt.getNewValue().toString());
            });
            v.getView(String.valueOf(time)).setMouseManager(sm);
        }
    }

    /**
     * Sets attributes to the graph displayed
     *
     * @param graph graph
     * @param time  time instant
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