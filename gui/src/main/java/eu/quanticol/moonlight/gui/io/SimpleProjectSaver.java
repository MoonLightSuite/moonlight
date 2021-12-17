package eu.quanticol.moonlight.gui.io;

import com.google.gson.Gson;
import eu.quanticol.moonlight.gui.chart.JavaFXChartController;
import eu.quanticol.moonlight.gui.filter.Filter;
import eu.quanticol.moonlight.gui.graph.JavaFXGraphController;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

public class SimpleProjectSaver implements ProjectSaver {

    private File tra = null;
    private File csv = null;
    private ArrayList<String> columnsAttributes = new ArrayList<>();
    private int indexOfAttributeChart = 1;
    private ArrayList<Filter> filters = new ArrayList<>();
    private transient final JavaFXGraphController graphController;
    private transient final JavaFXChartController chartController;
    private transient Stage stage = null;


    public SimpleProjectSaver(JavaFXGraphController graphController, JavaFXChartController chartController) {
        this.graphController = graphController;
        this.chartController = chartController;
    }

    public File getTra() {
        return tra;
    }

    @Override
    public void setStage(VBox root) {
        this.stage = (Stage) root.getScene().getWindow();
    }

    public void setTra(File tra) {
        this.tra = tra;
    }

    public File getCsv() {
        return csv;
    }

    public void setCsv(File csv) {
        this.csv = csv;
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
                Gson gson = new Gson();
                Writer writer = new FileWriter(file);
                gson.toJson(this, writer);
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void openProject() {

    }
}
