package eu.quanticol.moonlight.gui.chart;

import eu.quanticol.moonlight.gui.chart.SimpleChartBuilder;
import eu.quanticol.moonlight.gui.graph.SimpleTimeGraph;
import eu.quanticol.moonlight.gui.graph.TimeGraph;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleChartBuilderTest {

    @Test
    void getSeriesFromNodesTest() {
        Graph graph = new SingleGraph("0");
        Node n00 = graph.addNode(String.valueOf(0));
        ArrayList<String> vector = new ArrayList<>(Arrays.asList("1", "2", "2.5", "0.5", "0"));
        n00.setAttribute("time" + 0.0, vector);
        Node n10 = graph.addNode(String.valueOf(1));
        ArrayList<String> vector1 = new ArrayList<>(Arrays.asList("10", "20", "2.5", "0.5", "1"));
        n10.setAttribute("time" + 0.0, vector1);
        TimeGraph t1 = new SimpleTimeGraph(graph, 0.0);
        Graph graph2 = new SingleGraph("1");
        Node n01 = graph2.addNode(String.valueOf(0));
        ArrayList<String> vector2 = new ArrayList<>(Arrays.asList("3", "4", "2.5", "0.5", "5"));
        n01.setAttribute("time" + 1.0, vector2);
        Node n11 = graph2.addNode(String.valueOf(1));
        ArrayList<String> vector3 = new ArrayList<>(Arrays.asList("3", "4", "2.5", "0.5", "7"));
        n11.setAttribute("time" + 1.0, vector3);
        TimeGraph t2 = new SimpleTimeGraph(graph2, 1.0);
        ArrayList<TimeGraph> list = new ArrayList<>(Arrays.asList(t1, t2));
        SimpleChartBuilder simpleChartBuilder = new SimpleChartBuilder();
        List<Series<Number, Number>> series = simpleChartBuilder.getSeriesFromNodes(list);
        Series<Number, Number> s0 = new Series<>();
        s0.setName("Node " + 0);
        s0.getData().add(new Data<>(0.0, 0.0));
        s0.getData().add(new Data<>(1.0, 5.0));
        Series<Number, Number> s1 = new Series<>();
        s1.setName("Node " + 1);
        s1.getData().add(new Data<>(0.0, 1.0));
        s1.getData().add(new Data<>(1.0, 7.0));
        List<Series<Number, Number>> series1 = new ArrayList<>(Arrays.asList(s0, s1));
        assertEquals(series.get(0).getData().toString(), series1.get(0).getData().toString());
        assertEquals(series.get(1).getData().toString(), series1.get(1).getData().toString());
        assertEquals(series.toString(), series1.toString());
        assertEquals(series1.size(), 2);
        series1.remove(series1.get(0));
        assertEquals(series1.size(), 1);
        assertNotEquals(series.toString(), series1.toString());
    }

    @Test
    void getSeriesFromStaticGraphTest() {
        String line = "0, 155.05290550508354, 197.4287636921495, 5.696894467052598, 0.9011928036062729, 0, 138.7384637743081, 97.47360568313172, 3.9675733703908946, 0.255938829010682, 25, 92.90203975390901, 188.15696964044184, 6.114450372652869, 0.8553343447074815, 10, 88.79064023230296, 117.00681495028866, 1.0626328870598654, 0.4560818434879118, 0, 4.748694971371492, 30.985655177864114, 0.7224531443252975, 0.861984983062122, 0\n";
        ArrayList<Series<Number, Number>> list = new ArrayList<>();
        SimpleChartBuilder simpleChartBuilder = new SimpleChartBuilder();
        ArrayList<Series<Number, Number>> series = simpleChartBuilder.getSeriesFromStaticGraph(line, list, false);
        assertEquals(series.size(), 5);
        assertEquals(series.get(0).getData().get(0).toString(), new Data<>(0.0, 0.0).toString());
        assertEquals(series.get(1).getData().get(0).toString(), new Data<>(0.0, 25.0).toString());
        assertEquals(series.get(2).getData().get(0).toString(), new Data<>(0.0, 10.0).toString());
        assertThrows(IndexOutOfBoundsException.class, () -> new Data<>(series.get(3).getData().get(1).getXValue(), series.get(3).getData().get(1).getYValue()));
        assertNotEquals(series.get(1).getData().get(0).getYValue(), 0.0);
        assertEquals(series.get(1).getData().get(0).getYValue(), 25.0);
    }

    @Test
    void addLineDataTest() {
        ArrayList<Series<Number, Number>> list = new ArrayList<>();
        String line = "0, 155.05290550508354, 197.4287636921495, 5.696894467052598, 0.9011928036062729, 0, 138.7384637743081, 97.47360568313172, 3.9675733703908946, 0.255938829010682, 25, 92.90203975390901, 188.15696964044184, 6.114450372652869, 0.8553343447074815, 10, 88.79064023230296, 117.00681495028866, 1.0626328870598654, 0.4560818434879118, 0, 4.748694971371492, 30.985655177864114, 0.7224531443252975, 0.861984983062122, 0\n";
        SimpleChartBuilder simpleChartBuilder = new SimpleChartBuilder();
        list = simpleChartBuilder.getSeriesFromStaticGraph(line, list, false);
        assertNotNull(list.get(0).getData().get(0).getYValue());
        ArrayList<Series<Number, Number>> finalList = list;
        assertThrows(IndexOutOfBoundsException.class, () -> new Data<>(finalList.get(0).getData().get(1).getXValue(), finalList.get(0).getData().get(1).getYValue()));
        String line2 = "1, 155.80359784013942, 154.5542979566448, 5.785136173238553, 0.9504069579150511, 3.892595283656185, 138.564979156669, 138.5502942453243, 4.043585668948758, 0.27725930620953787, 4.0999761657090685, 93.74522664447895, 92.75839885163121, 6.1617471105001105, 1.0, -2.29050317355021, 89.01255760611137, 89.18909139262416, 1.0977928539850594, 0.34781455554287194, 1.1251633308188556, 5.395343954129576, 5.318662693372083, 0.7191084280594131, 0.8565052262441617, 0.9313854791722278\n";
        String[] attributes = line2.split(", ");
        simpleChartBuilder.addLineData(list, attributes);
        assertNotNull(list.get(0).getData().get(1).getYValue());
        ArrayList<Series<Number, Number>> finalList1 = list;
        assertDoesNotThrow(() -> new Data<>(finalList1.get(0).getData().get(1).getXValue(), finalList1.get(0).getData().get(1).getYValue()));
    }
}