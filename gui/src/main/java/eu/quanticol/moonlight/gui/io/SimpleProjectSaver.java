package eu.quanticol.moonlight.gui.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import eu.quanticol.moonlight.gui.chart.ChartVisualization;
import eu.quanticol.moonlight.gui.chart.JavaFXChartController;
import eu.quanticol.moonlight.gui.filter.Filter;
import eu.quanticol.moonlight.gui.filter.SimpleFilter;
import eu.quanticol.moonlight.gui.graph.GraphType;
import eu.quanticol.moonlight.gui.graph.JavaFXGraphController;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;

import static eu.quanticol.moonlight.gui.io.Serializer.interfaceSerializer;

public class SimpleProjectSaver implements ProjectSaver {

    private String tra = null;
    private String csv = null;
    private ArrayList<String> columnsAttributes = new ArrayList<>();
    private int indexOfAttributeChart = 1;
    private ChartVisualization chartVisualization = null;
    private ArrayList<Filter> filters = new ArrayList<>();
    private String positionX = null;
    private String positionY = null;
    private transient final JavaFXGraphController graphController;
    private transient final JavaFXChartController chartController;
    private transient Stage stage = null;

    public SimpleProjectSaver(JavaFXGraphController graphController, JavaFXChartController chartController) {
        this.graphController = graphController;
        this.chartController = chartController;
    }

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

    @Override
    public ProjectSaver openProject() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Json Project", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        DialogBuilder d = new DialogBuilder(JsonThemeLoader.getInstance().getGeneralTheme());
        if (file == null)
            d.info("No file opened");
        else {
            try {
                fromJson(file);
                initializeProject();
                return this;
            } catch (IOException e) {
                d.error("Failed importing project");
            }
        }
        return null;
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

    private void initializeProject() throws IOException {
        graphController.setColumnsAttributes(this.columnsAttributes);
        graphController.getLinkController().setColumnX(this.positionX);
        graphController.getLinkController().setColumnY(this.positionY);
        graphController.openRecentTRA(new File(this.tra));
        graphController.setCsv(new File(this.csv));
        if (graphController.getGraphVisualization().equals(GraphType.DYNAMIC))
            graphController.reloadDynamicPositions();
        else graphController.reloadStaticPositions();
        if (this.chartVisualization.equals(ChartVisualization.PIECEWISE))
            graphController.openRecentCSV(new File(this.csv));
        else graphController.openConstantCsv(new File(this.csv));
        graphController.getFiltersComponentController().getTableFilters().getItems().clear();
        for (Filter f : this.filters) {
            graphController.getFiltersComponentController().addFilter(f);
        }
    }
}
