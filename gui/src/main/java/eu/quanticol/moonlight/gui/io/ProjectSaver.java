package eu.quanticol.moonlight.gui.io;

import eu.quanticol.moonlight.gui.chart.ChartVisualization;
import eu.quanticol.moonlight.gui.filter.Filter;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;

public interface ProjectSaver {

    void saveProject();

    ProjectSaver openProject();

    File getTra();

    void setStage(VBox root);

    void setTra(File tra);

    File getCsv();

    void setCsv(File csv);

    ChartVisualization getGraphVisualization();

    void setGraphVisualization(ChartVisualization chartVisualization);

    ArrayList<String> getColumnsAttributes();

    void setColumnsAttributes(ArrayList<String> columnsAttributes);

    int getIndexOfAttributeChart();

    void setIndexOfAttributeChart(int indexOfAttributeChart);

    ArrayList<Filter> getFilters();

    void setFilters(ArrayList<Filter> filters);

    String getPositionX();

    void setPositionX(String positionX);

    String getPositionY();

    void setPositionY(String positionY);

}
