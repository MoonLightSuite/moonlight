package eu.quanticol.moonlight.gui.chart;

import eu.quanticol.moonlight.gui.graph.TimeGraph;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import org.graphstream.graph.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for a builder of charts from a graph
 */
public interface ChartBuilder {

    /**
     * Create series of a chart from nodes in a graph
     * @param timeGraph a {@link TimeGraph}
     * @return a list of series created
     */
    List<XYChart.Series<Number, Number>> getSeriesFromNodes(List<TimeGraph> timeGraph);

    ArrayList<XYChart.Series<Number, Number>> getSeriesFromStaticGraph(String line);

    void clearList();

    void addData(LineChart<Number, Number> lineChart, String[] attributes);

    void addAttributes(String[] attributes);
//    List<XYChart.Series<Number, Number>> getSeriesFromNodes(Graph staticGraph);
}
