package eu.quanticol.moonlight.gui.filter;

import eu.quanticol.moonlight.gui.graph.SimpleTimeGraph;
import eu.quanticol.moonlight.gui.graph.TimeGraph;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class SimpleFiltersControllerTest {

    final FiltersController filtersController = SimpleFiltersController.getInstance();

    @Test
    void validationFilterTest() {
        ArrayList<Filter> filters = new ArrayList<>();
        Filter filter = new SimpleFilter("Value", "=",2.0);
        Filter filter1 = new SimpleFilter("Value", "=", 1.0);
        Filter filter2 = new SimpleFilter("Direction","<",5.0);
        filters.add(filter);
        assertThrows(IllegalArgumentException.class, () -> filtersController.validationFilter(filter1,filters));
        assertDoesNotThrow(() -> filtersController.validationFilter(filter2,filters));
        assertEquals(filter.getAttribute(), filter1.getAttribute());
        assertEquals(filter.getOperator(),filter1.getOperator());
    }

    @Test
    void checkFilterTest() throws IOException {
        List<TimeGraph> timeGraphList = new ArrayList<>();
        filtersController.getGraphController().setGraphList(timeGraphList);
        File fileDynamic = new File((Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("dynamic.tra"))).getFile());
        filtersController.getGraphController().createGraphFromFile(fileDynamic);
        ArrayList<Filter> filters = new ArrayList<>();
        ArrayList<Double> times = new ArrayList<>();
        ArrayList<Node> nodes = new ArrayList<>();
        ArrayList<String> attributes = new ArrayList<>();
        attributes.add("time");
        attributes.add("x");
        attributes.add("y");
        attributes.add("direction");
        attributes.add("speed");
        attributes.add("v");
        filtersController.getGraphController().setColumnsAttributes(attributes);
        String line = "0,3,17,1,0.8,0,14,13,3,0.6,0,9,13,2,0.05,0,25,16,5,0.1,0,14,16,5,0.6,0";
        filtersController.getGraphController().createNodesVector(line,"x","y", true);
        Filter filter = new SimpleFilter("v", "=",0.0);
        filters.add(filter);
        times.add(0.0);
        TimeGraph g = new SimpleTimeGraph(filtersController.getGraphController().getGraphList().get(0).getGraph(),0.0);
        filtersController.checkFilter(filter,filters,nodes,g,times);
        List<Node> nodesWithVector = new ArrayList<>();
        filtersController.getGraphController().getGraphList().forEach(graph -> {
            for (int i = 0; i < 5; i++)
                if(graph.getGraph().getNode(String.valueOf(i)).hasAttribute("time" + 0.0))
                    nodesWithVector.add(graph.getGraph().getNode(String.valueOf(i)));
        });
        assertTrue(nodesWithVector.stream().allMatch(n -> n.hasAttribute("ui.class")));
        Filter filter1 = new SimpleFilter("direction","=", 3.0);
        filters.add(filter1);
        filtersController.checkFilter(filter1,filters,nodes,g,times);
        assertTrue(filtersController.getGraphController().getGraphList().get(0).getGraph().getNode(String.valueOf(1)).hasAttribute("ui.class"));
    }

    @Test
    void checkStaticFilterTest() throws IOException {
        ArrayList<Node> nodes = new ArrayList<>();
        File fileStatic = new File((Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("static.tra"))).getFile());
        filtersController.getGraphController().createGraphFromFile(fileStatic);
        ArrayList<String> attributes = new ArrayList<>();
        attributes.add("time");
        attributes.add("x");
        attributes.add("y");
        attributes.add("direction");
        attributes.add("speed");
        attributes.add("v");
        filtersController.getGraphController().setColumnsAttributes(attributes);
        String line = "0,3,17,1,0,0,14,19,3,0,0";
        String[] attributesCsv = line.split(",");
        filtersController.addAttributes(attributesCsv);
        filtersController.getGraphController().createPositions(line,"x","y");
        Graph graph = filtersController.getGraphController().getStaticGraph();
        ArrayList<Filter> filters = new ArrayList<>();
        Filter filter = new SimpleFilter("x", "=",3.0);
        filters.add(filter);
        filtersController.checkStaticFilter(filter,filters,nodes);
        assertTrue(graph.getNode(String.valueOf(0)).hasAttribute("ui.class"));
        Filter filter1 = new SimpleFilter("y", "=",15.0);
        filters.add(filter1);
        filtersController.checkStaticFilter(filter1,filters,nodes);
        assertFalse(graph.getNode(String.valueOf(0)).hasAttribute("ui.class"));
    }
}