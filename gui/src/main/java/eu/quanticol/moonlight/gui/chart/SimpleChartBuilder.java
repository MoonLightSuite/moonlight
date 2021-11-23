package eu.quanticol.moonlight.gui.chart;

import eu.quanticol.moonlight.gui.graph.TimeGraph;
import javafx.scene.chart.XYChart;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Class that builds a simple chart from a {@link TimeGraph}
 */
public class SimpleChartBuilder implements ChartBuilder {

    private final ArrayList<ArrayList<String>> attributes = new ArrayList<>();

    public ArrayList<ArrayList<String>> getAttributes() {
        return attributes;
    }

    public void addAttributes(String[] attributes) {
        ArrayList<String> a = new ArrayList<>(Arrays.stream(attributes).toList());
        this.attributes.add(a);
    }

    private final ArrayList<XYChart.Series<Number, Number>> listLinear = new ArrayList<>();

    private final ArrayList<XYChart.Series<Number, Number>> listLog = new ArrayList<>();

    public ArrayList<XYChart.Series<Number, Number>> getListLinear() {
        return listLinear;
    }

    public ArrayList<XYChart.Series<Number, Number>> getListLog() {
        return listLog;
    }


    /**
     * Clears the lists of series
     */
    @Override
    public void clearList() {
        listLinear.clear();
        listLog.clear();
    }


    /**
     * Gets all nodes info and create a relative series for each
     *
     * @param timeGraph a {@link TimeGraph}
     *
     * @return a list of all series
     */
    public List<XYChart.Series<Number, Number>> getSeriesFromNodes(List<TimeGraph> timeGraph) {
        List<XYChart.Series<Number, Number>> series = new ArrayList<>();
        int totSeries = timeGraph.get(0).getGraph().getNodeCount();
        ArrayList<Double> times = new ArrayList<>();
        timeGraph.forEach(timeGraph1 -> times.add(timeGraph1.getTime()));
        int node = 0;
        while (node < totSeries) {
            XYChart.Series<Number, Number> seriesX = new XYChart.Series<>();
            seriesX.setName("Node " + node);
            addData(seriesX, times, timeGraph, node);
            series.add(seriesX);
            node++;
        }
        return series;
    }

    /**
     * Creates series of a chart from a file of a static graph
     */
    @Override
    public ArrayList<XYChart.Series<Number, Number>> getSeriesFromStaticGraph(String line, ArrayList<XYChart.Series<Number, Number>> list, boolean first) {
        int index = 5;
        int node = 0;
        XYChart.Series<Number, Number> series = null;
        String[] attributes = line.split(", ");
        double time = Double.parseDouble(attributes[0]);
        checkFirst(first, attributes);
        for (int i = 0; i < 5; i++) {
            int finalNode = node;
            series = getSeries(list, node, series, finalNode);
            double variable = Double.parseDouble(StringUtils.substringBefore(attributes[index], "]"));
            series.getData().add(new XYChart.Data<>(time, variable));
            node++;
            index += 5;
        }
        return list;
    }


    /**
     * Creates and returns a series if it doesn't exist or returns the existing series
     *
     */
    private XYChart.Series<Number, Number> getSeries(ArrayList<XYChart.Series<Number, Number>> list, int node, XYChart.Series<Number, Number> series, int finalNode) {
        if (list.stream().noneMatch(numberNumberSeries -> numberNumberSeries.getName().equals("Node " + finalNode))) {
            series = new XYChart.Series<>();
            series.setName("Node " + node);
            list.add(series);
        } else {
            Optional<XYChart.Series<Number, Number>> series1 = list.stream().filter(numberNumberSeries -> numberNumberSeries.getName().equals("Node " + finalNode)).findFirst();
            if (series1.isPresent())
                series = series1.get();
        }
        return series;
    }

    /**
     * Checks if the attributes have already been added
     *
     */
    private void checkFirst(boolean first, String[] attributes) {
        if(first) {
            ArrayList<String> timeAttributes = new ArrayList<>(Arrays.asList(attributes));
            this.attributes.add(timeAttributes);
        }
    }


    /**
     * Adds data to the chart from an array of attributes
     *
     */
    @Override
    public void addLineData(List<XYChart.Series<Number, Number>> series, String[] attributes) {
        int index = 5;
        int node = 0;
        XYChart.Series<Number, Number> numberSeries = null;
        double time = Double.parseDouble(attributes[0]);
        for (int i = 0; i < 5; i++) {
            int finalNode = node;
            Optional<XYChart.Series<Number, Number>> series1 = series.stream().filter(numberNumberSeries -> numberNumberSeries.getName().equals("Node " + finalNode)).findFirst();
            if (series1.isPresent())
                numberSeries = series1.get();
            double variable = Double.parseDouble(StringUtils.substringBefore(attributes[index], "]"));
            numberSeries.getData().add(new XYChart.Data<>(time, variable));
            node++;
            index += 5;
        }
    }


    /**
     * Add all data of a series
     *
     * @param series     series to add data
     * @param times      list of time instants
     * @param timeGraph  list of {@link TimeGraph}
     * @param seriesNode id of a node in a graph
     */
    private void addData(XYChart.Series<Number, Number> series, ArrayList<Double> times, List<TimeGraph> timeGraph, int seriesNode) {
        for (double t : times) {
            for (TimeGraph graph : timeGraph) {
                if (graph.getTime() == t) {
                    String attributes = graph.getGraph().getNode(seriesNode).getAttribute("time" + t).toString();
                    String[] a = attributes.split(", ");
                    double variable = Double.parseDouble(StringUtils.substringBefore(a[4], "]"));
                    series.getData().add(new XYChart.Data<>(t, variable));
                }
            }
        }
    }
}