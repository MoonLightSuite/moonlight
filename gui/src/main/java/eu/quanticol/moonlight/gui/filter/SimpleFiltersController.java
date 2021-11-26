package eu.quanticol.moonlight.gui.filter;

import eu.quanticol.moonlight.gui.graph.TimeGraph;
import org.graphstream.graph.Node;

import java.util.ArrayList;

/**
 * A controller for filters
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class SimpleFiltersController implements FiltersController {

    private static SimpleFiltersController instance= null;

    private SimpleFiltersController() {}

    public static SimpleFiltersController getInstance() {
        if(instance==null)
            instance = new SimpleFiltersController();
        return instance;
    }

    /**
     * Checks if the attribute and the operator of filter are already used.
     *
     * @param filter {@link Filter} to validate
     * @param filters filters of table
     */
    public void validationFilter(Filter filter, ArrayList<Filter> filters) {
        filters.forEach(f -> {
            if (f.getOperator().equals(filter.getOperator()) && f.getAttribute().equals(filter.getAttribute()))
                throw new IllegalArgumentException("Operator already used.");
        });
    }

    /**
     * Based on the filter entered by the user, checks if there are nodes
     * in the graph that correspond to it.
     *
     * @param f {@link Filter} entered
     * @param filters filters of table
     * @param nodes  list of nodes
     */
    public void checkFilter(Filter f, ArrayList<Filter> filters, ArrayList<Node> nodes, TimeGraph g, ArrayList<Double> times) {
        boolean check;
        int countNodes = g.getGraph().getNodeCount();
        for (double t : times) {
            for (int i = 0; i < countNodes; i++) {
                Node n = g.getGraph().getNode(i);
                if (n.getAttribute("time" + t) != null) {
                    check = getVector(n, t, f);
                    changeStyleNodes(check, n, f, filters, nodes);
                }
            }
        }
    }

    /**
     * Takes attributes of node which will be compared with the filter.
     *
     * @param n node from which take the attributes
     * @param t time of graph of node
     * @param f {@link Filter} to compare
     *
     * @return true, if there are any mismatches or false
     */
    private boolean getVector(Node n, Double t, Filter f) {
        String attributes = n.getAttribute("time" + t).toString();
        String[] vector = attributes.replaceAll("^\\s*\\[|\\]\\s*$", "").split("\\s*,\\s*");
        return checkAttribute(f.getAttribute(), f.getOperator(), f.getValue(), vector);
    }

    /**
     * Adds or removes style on nodes when the user adds a filter.
     *
     * @param check boolean to know if there are any mismatches
     * @param n     node to change style to
     * @param f     {@link Filter} added
     * @param filters filters of table
     * @param nodes   list of nodes
     */
    private void changeStyleNodes(boolean check, Node n, Filter f, ArrayList<Filter> filters, ArrayList<Node> nodes) {
        if (check) {
            if (filters.indexOf(f) == 0) {
                if (!nodes.contains(n))
                    nodes.add(n);
                n.setAttribute("ui.class", "filtered");
            } else if (!nodes.contains(n))
                n.removeAttribute("ui.class");
        } else {
            if (filters.size() != 1) {
                if (nodes.contains(n)) {
                    nodes.remove(n);
                    n.removeAttribute("ui.class");
                }
            }
        }
    }

    /**
     * Check which attribute is selected.
     *
     * @param attribute attribute selected
     * @param operator  operator selected
     * @param value     value entered
     * @param vector    attributes of node
     *
     * @return true, if the node is to be showed, or false
     */
    private boolean checkAttribute(String attribute, String operator, double value, String[] vector) {
        double v;
        boolean toShow = false;
        if (attribute.equals("Direction")) {
            v = Double.parseDouble(vector[2]);
            toShow = checkOperator(operator, v, value);
        }
        if (attribute.equals("Speed")) {
            v = Double.parseDouble(vector[3]);
            toShow = checkOperator(operator, v, value);
        }
        if (attribute.equals("Value")) {
            v = Double.parseDouble(vector[4]);
            toShow = checkOperator(operator, v, value);
        }
        return toShow;
    }

    /**
     * Checks which operator is selected and if there are any mismatches.
     *
     * @param operator operator selected
     * @param v        value of node
     * @param value    value of textField
     *
     * @return true or false
     */
    private boolean checkOperator(String operator, double v, double value) {
        boolean b = false;
        switch (operator) {
            case "=":
                if (v == value) b = true;
                break;
            case ">":
                if (v > value) b = true;
                break;
            case "<":
                if (v < value) b = true;
                break;
            case ">=":
                if (v >= value) b = true;
                break;
            case "<=":
                if (v <= value) b = true;
                break;
            default:
                return false;
        }
        return b;
    }
}