package eu.quanticol.moonlight.gui.filter;

import eu.quanticol.moonlight.gui.graph.GraphController;
import eu.quanticol.moonlight.gui.graph.TimeGraph;
import org.graphstream.graph.Node;

import java.util.ArrayList;

/**
 * Interface that defines a controller for filters
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public interface FiltersController {

    void validationFilter(Filter filter, ArrayList<Filter> filters);

    void checkFilter(Filter f, ArrayList<Filter> filters, ArrayList<Node> nodes, TimeGraph g, ArrayList<Double> times);

    void checkStaticFilter(Filter f, ArrayList<Filter> filters, ArrayList<Node> staticNodes);

    void addAttributes(String[] attributes);

    GraphController getGraphController();
}
