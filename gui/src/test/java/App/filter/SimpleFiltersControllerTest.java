package App.filter;

import eu.quanticol.moonlight.gui.filter.Filter;
import eu.quanticol.moonlight.gui.filter.FiltersController;
import eu.quanticol.moonlight.gui.filter.SimpleFilter;
import eu.quanticol.moonlight.gui.filter.SimpleFiltersController;
import eu.quanticol.moonlight.gui.graph.SimpleTimeGraph;
import eu.quanticol.moonlight.gui.graph.TimeGraph;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SimpleFiltersControllerTest {

    FiltersController filtersController = SimpleFiltersController.getInstance();

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
    void checkFilterTest() {
        ArrayList<Filter> filters = new ArrayList<>();
        ArrayList<Node> nodes = new ArrayList<>();
        ArrayList<String> vector = new ArrayList<>();
        ArrayList<Double> times = new ArrayList<>();
        times.add(0.0);
        vector.add("1");
        vector.add("2");
        vector.add("2.5");
        vector.add("0.5");
        vector.add("0");
        Filter filter = new SimpleFilter("Value", "=",0.0);
        filters.add(filter);
        Filter filter1 = new SimpleFilter("Direction",">", 3.0);
        filters.add(filter1);
        Graph graph = new SingleGraph("0");
        Node n = graph.addNode(String.valueOf(0));
        n.setAttribute("time" + 0.0, vector);
        nodes.add(n);
        TimeGraph g = new SimpleTimeGraph(graph,0.0);
        filtersController.checkFilter(filter,filters,nodes,g,times);
        assertTrue(n.hasAttribute("ui.class"));
        filtersController.checkFilter(filter1,filters,nodes,g,times);
        assertFalse(n.hasAttribute("ui.class"));
    }
}