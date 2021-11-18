package eu.quanticol.moonlight.gui.filter;

import eu.quanticol.moonlight.gui.filter.Filter;
import eu.quanticol.moonlight.gui.graph.TimeGraph;
import javafx.collections.ObservableList;
import org.graphstream.graph.Node;

import java.util.ArrayList;

public interface FiltersController {

    void validationFilter(Filter filter, ObservableList<Filter> filters);

    void checkFilter(Filter f, ObservableList<Filter> filters, ArrayList<Node> nodes, TimeGraph g, ArrayList<Double> times);
}
