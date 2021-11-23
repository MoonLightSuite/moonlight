package eu.quanticol.moonlight.gui.filter;

import eu.quanticol.moonlight.gui.graph.TimeGraph;
import org.graphstream.graph.Node;

import java.util.ArrayList;

public interface FiltersController {

    void validationFilter(Filter filter, ArrayList<Filter> filters);

    void checkFilter(Filter f, ArrayList<Filter> filters, ArrayList<Node> nodes, TimeGraph g, ArrayList<Double> times);
}
