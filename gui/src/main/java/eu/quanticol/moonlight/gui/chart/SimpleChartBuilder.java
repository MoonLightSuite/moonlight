package eu.quanticol.moonlight.gui.chart;

import eu.quanticol.moonlight.gui.graph.TimeGraph;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import org.apache.commons.lang3.StringUtils;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Class that builds a simple chart from a {@link TimeGraph}
 */
public class SimpleChartBuilder implements ChartBuilder {

    private final List<List<String>> attributes = new ArrayList<>();

    public List<List<String>> getAttributes() {
        return attributes;
    }

    public void addAttributes(String[] attributes) {
        this.attributes.add(Arrays.asList(attributes));
    }

    private ArrayList<XYChart.Series<Number, Number>> list = new ArrayList<>();

    @Override
    public void clearList() {
        list.clear();
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

    @Override
    public ArrayList<XYChart.Series<Number, Number>> getSeriesFromStaticGraph(String line) {
        int index = 5;
        int node = 0;
        XYChart.Series<Number, Number> series = null;
        String[] attributes = line.split(",");
        double time = Double.parseDouble(attributes[0]);
        for (int i = 0; i < 5; i++) {
            List<String> timeAttributes = Arrays.asList(attributes);
            this.attributes.add(timeAttributes);
            int finalNode = node;
            if (list.stream().noneMatch(numberNumberSeries -> numberNumberSeries.getName().equals("Node " + finalNode))) {
                series = new XYChart.Series<>();
                series.setName("Node " + node);
                list.add(series);
            } else {
                Optional<XYChart.Series<Number, Number>> series1 = list.stream().filter(numberNumberSeries -> numberNumberSeries.getName().equals("Node " + finalNode)).findFirst();
                if (series1.isPresent())
                    series = series1.get();
            }
            double variable = Double.parseDouble(StringUtils.substringBefore(attributes[index], "]"));
            series.getData().add(new XYChart.Data<>(time, variable));
            node++;
            index += 5;
        }
        return list;
    }


    @Override
    public void addData(LineChart<Number, Number> lineChart, String[] attributes) {
        int index = 5;
        int node = 0;
        XYChart.Series<Number, Number> series = null;
        double time = Double.parseDouble(attributes[0]);
        for (int i = 0; i < 5; i++) {
            int finalNode = node;
            Optional<XYChart.Series<Number, Number>> series1 = lineChart.getData().stream().filter(numberNumberSeries -> numberNumberSeries.getName().equals("Node " + finalNode)).findFirst();
            if (series1.isPresent())
                series = series1.get();
            double variable = Double.parseDouble(StringUtils.substringBefore(attributes[index], "]"));
            series.getData().add(new XYChart.Data<>(time, variable));
            node++;
            index += 5;
        }
    }

//    @Override
//    public List<XYChart.Series<Number, Number>> getSeriesFromNodes(Graph staticGraph) {
//        List<XYChart.Series<Number, Number>> series = new ArrayList<>();
//        int totSeries = staticGraph.getNodeCount();
//        int node = 0;
//        while (node < totSeries) {
//            XYChart.Series<Number, Number> seriesX = new XYChart.Series<>();
//            seriesX.setName("Node " + node);
//            addData(seriesX, staticGraph.getNode(String.valueOf(node)));
//            series.add(seriesX);
//            node++;
//        }
//        return series;
//    }
//
//    private void addData(XYChart.Series<Number, Number> series, Node node) {
//        String attributes = node.getAttribute("Attributes").toString();
//        String[] a = attributes.split(", ");
//        double variable = Double.parseDouble(StringUtils.substringBefore(a[2], "]"));
//        series.getData().add(new XYChart.Data<>(, variable));
//    }

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