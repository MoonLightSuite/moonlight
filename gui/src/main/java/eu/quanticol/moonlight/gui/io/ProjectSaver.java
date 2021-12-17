package eu.quanticol.moonlight.gui.io;

import eu.quanticol.moonlight.gui.filter.Filter;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public interface ProjectSaver {

    void saveProject();

    void openProject();

    File getTra();

    void setStage(VBox root);

    void setTra(File tra);

    File getCsv();

    void setCsv(File csv);

    ArrayList<String> getColumnsAttributes();

    void setColumnsAttributes(ArrayList<String> columnsAttributes);

    int getIndexOfAttributeChart();

    void setIndexOfAttributeChart(int indexOfAttributeChart);

    ArrayList<Filter> getFilters();

    void setFilters(ArrayList<Filter> filters);

}
