package eu.quanticol.moonlight.gui.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import eu.quanticol.moonlight.gui.chart.ChartVisualization;
import eu.quanticol.moonlight.gui.chart.JavaFXChartController;
import eu.quanticol.moonlight.gui.filter.Filter;
import eu.quanticol.moonlight.gui.filter.SimpleFilter;
import eu.quanticol.moonlight.gui.graph.JavaFXGraphController;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;

import static eu.quanticol.moonlight.gui.io.Serializer.interfaceSerializer;

/**
 * Class that implements the {@link ProjectSaver} interface and saves all files and settings of a project in a json file.
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class SimpleProjectSaver implements ProjectSaver {

    private String tra = null;
    private String csv = null;
    private ArrayList<String> columnsAttributes = new ArrayList<>();
    private int indexOfAttributeChart = 1;
    private ChartVisualization chartVisualization = null;
    private ArrayList<Filter> filters = new ArrayList<>();
    private String positionX = null;
    private String positionY = null;
    private transient JavaFXGraphController graphController;
    private transient JavaFXChartController chartController;
    private transient Stage stage = null;

    public SimpleProjectSaver(JavaFXGraphController graphController, JavaFXChartController chartController) {
        this.graphController = graphController;
        this.chartController = chartController;
    }

    /**
     * @return whether if is visualized a piecewise or stepwise chart
     */
    public ChartVisualization getGraphVisualization() {
        return chartVisualization;
    }

    public void setGraphVisualization(ChartVisualization chartVisualization) {
        this.chartVisualization = chartVisualization;
    }

    public File getTra() {
        return new File(tra);
    }

    @Override
    public void setStage(VBox root) {
        this.stage = (Stage) root.getScene().getWindow();
    }

    public void setTra(File tra) {
        this.tra = tra.getAbsolutePath();
    }

    public File getCsv() {
        return new File(csv);
    }

    public void setCsv(File csv) {
        this.csv = csv.getAbsolutePath();
    }

    public ArrayList<String> getColumnsAttributes() {
        return columnsAttributes;
    }

    public void setColumnsAttributes(ArrayList<String> columnsAttributes) {
        this.columnsAttributes = columnsAttributes;
    }

    public int getIndexOfAttributeChart() {
        return indexOfAttributeChart;
    }

    public void setIndexOfAttributeChart(int indexOfAttributeChart) {
        this.indexOfAttributeChart = indexOfAttributeChart;
    }

    public ArrayList<Filter> getFilters() {
        return filters;
    }

    public void setFilters(ArrayList<Filter> filters) {
        this.filters = filters;
    }

    public String getPositionX() {
        return positionX;
    }

    public void setPositionX(String positionX) {
        this.positionX = positionX;
    }

    public String getPositionY() {
        return positionY;
    }

    public void setPositionY(String positionY) {
        this.positionY = positionY;
    }

    @Override
    public ChartVisualization getChartVisualization() {
        return chartVisualization;
    }

    @Override
    public JavaFXGraphController getGraphController() {
        return graphController;
    }

    @Override
    public JavaFXChartController getChartController() {
        return chartController;
    }

    @Override
    public void setGraphController(JavaFXGraphController graphController) {
        this.graphController = graphController;
    }

    @Override
    public void setChartController(JavaFXChartController chartController) {
        this.chartController = chartController;
    }

    /**
     * Saves all files and settings in a json file chosen from the file explorer
     */
    @Override
    public void saveProject() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Json Project", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(stage);
        saveToJson(file);
        DialogBuilder d = new DialogBuilder(JsonThemeLoader.getInstance().getGeneralTheme());
        if (file != null)
            d.info("Project saved");
        else
            d.error("Failed saving project");
    }

    private void saveToJson(File file) {
        try {
            if (file != null) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(ProjectSaver.class, interfaceSerializer(SimpleProjectSaver.class))
                        .registerTypeAdapter(Filter.class, interfaceSerializer(SimpleFilter.class))
                        .create();
                Writer writer = new FileWriter(file);
                gson.toJson(this, writer);
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens a project saved in a json file, chosen from the file explorer
     */
    @Override
    public void openProject() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Json Project", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        openProject(file);
    }

    /**
     * Opens a project
     */
    public void openProject(File file) {
        DialogBuilder d = new DialogBuilder(JsonThemeLoader.getInstance().getGeneralTheme());
        if (file == null)
            d.info("No file opened");
        else {
            try {
                fromJson(file);
                graphController.getJsonFilesLoader().saveToJson(file.getPath(),FileType.JSON);
            } catch (IOException e) {
                d.error("Failed importing project");
            }
        }
    }


    private void fromJson(File file) throws IOException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ProjectSaver.class, interfaceSerializer(SimpleProjectSaver.class))
                .registerTypeAdapter(Filter.class, interfaceSerializer(SimpleFilter.class))
                .create();
        Reader reader = new FileReader(file);
        Type project = new TypeToken<ProjectSaver>() {
        }.getType();
        ProjectSaver projectSaver = gson.fromJson(reader, project);
        saveToObject(projectSaver);
        reader.close();
    }

    /**
     * Saves the variables from a json file to the object of this class
     */
    private void saveToObject(ProjectSaver projectSaver) {
        this.csv = projectSaver.getCsv().getAbsolutePath();
        this.tra = projectSaver.getTra().getAbsolutePath();
        this.filters = projectSaver.getFilters();
        this.columnsAttributes = projectSaver.getColumnsAttributes();
        this.chartVisualization = projectSaver.getGraphVisualization();
        this.indexOfAttributeChart = projectSaver.getIndexOfAttributeChart();
        this.positionX = projectSaver.getPositionX();
        this.positionY = projectSaver.getPositionY();
    }

}
