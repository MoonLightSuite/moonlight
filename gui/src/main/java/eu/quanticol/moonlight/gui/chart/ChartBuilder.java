package eu.quanticol.moonlight.gui.chart;

import eu.quanticol.moonlight.gui.graph.TimeGraph;
import javafx.scene.chart.XYChart.Series;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface that defines a builder of charts from a graph
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public interface ChartBuilder {

    ArrayList<Series<Number, Number>> createSeriesForConstantChart(File file, int index) throws IOException;

    /**
     * Create series of a chart from nodes in a graph
     * @param timeGraph a {@link TimeGraph}
     * @return a list of series created
     */
    List<Series<Number, Number>> getSeriesFromNodes(List<TimeGraph> timeGraph, int index);

    /**
     * Creates series of a chart from a file of a static graph
     */
    ArrayList<Series<Number, Number>> getSeriesFromStaticGraph(String line, ArrayList<Series<Number, Number>> list, boolean first,int index);

    /**
     * Clears the lists of series
     */
    void clearList();

    /**
     * Adds data to the chart from an array of attributes
     *
     */
    void addLineData(List<Series<Number, Number>> series, String[] attributes, int index);

    ArrayList<ArrayList<String>> getAttributes();

    ArrayList<Series<Number, Number>> getListLinear();

    ArrayList<Series<Number, Number>> getListLog();

    void addAttributes(String[] attributes);
}
