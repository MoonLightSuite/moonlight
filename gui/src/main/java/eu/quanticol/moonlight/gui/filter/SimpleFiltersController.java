package eu.quanticol.moonlight.gui.filter;

import eu.quanticol.moonlight.gui.graph.GraphController;
import eu.quanticol.moonlight.gui.graph.SimpleGraphController;
import eu.quanticol.moonlight.gui.graph.TimeGraph;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class that implements the {@link FiltersController} interface and defines a controller for filters
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class SimpleFiltersController implements FiltersController {

    private static SimpleFiltersController instance= null;
    private final GraphController graphController = SimpleGraphController.getInstance();
    ArrayList<ArrayList<String>> attributes = new ArrayList<>();

    @Override
    public void addAttributes(String[] attributes) {
        ArrayList<String> a = new ArrayList<>(Arrays.stream(attributes).toList());
        this.attributes.add(a);
    }

    private SimpleFiltersController() {}

    public static SimpleFiltersController getInstance() {
        if(instance==null)
            instance = new SimpleFiltersController();
        return instance;
    }

    public GraphController getGraphController() {
        return graphController;
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
     * @param f       {@link Filter} entered
     * @param filters filters of table
     * @param nodes   list of nodes
     * @param g       graph
     * @param times   all time instants
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
     * Based on the filter entered by the user, checks if there are nodes
     * in the static graph that correspond to it.
     *
     * @param f           {@link Filter} entered
     * @param filters     filters of table
     * @param staticNodes list of nodes of static graph
     */
    public void checkStaticFilter(Filter f, ArrayList<Filter> filters, ArrayList<Node> staticNodes){
        int index = graphController.getColumnsAttributes().indexOf(f.getAttribute());
        ArrayList<Integer> indexes = new ArrayList<>();
        for (ArrayList<String> line : attributes) {
            int s = graphController.getColumnsAttributes().size() - 1;
            for (int i = index; i < line.size(); i+=s) {
                if(i < s) {
                    if (checkOperator(f.getOperator(), Double.parseDouble(line.get(i)), f.getValue()))
                        if(!indexes.contains(0))
                            indexes.add(0);
                } else
                    if(checkOperator(f.getOperator(),Double.parseDouble(line.get(i)),f.getValue())) {
                        int result = ((i - 1) / 5);
                        if(!indexes.contains(result))
                            indexes.add(result);
                    }
            }
        }
        if(indexes.size() != 0)
            indexes.forEach(i-> changeStyleStaticNodes(f, filters, staticNodes, indexes, i));
        else {
            staticNodes.forEach(node-> node.removeAttribute("ui.class"));
            staticNodes.clear();
        }
}

    /**
     * Takes attributes of node which will be compared with the filter.
     *
     * @param n node from which take the attributes
     * @param t time of graph of node
     * @param f {@link Filter} to compare
     * @return  true, if there are any mismatches or false
     */
    private boolean getVector(Node n, Double t, Filter f) {
        String attributes = n.getAttribute("time" + t).toString();
        String[] vector = attributes.replaceAll("^\\s*\\[|]\\s*$", "").split("\\s*,\\s*");
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
     * Adds or removes style on nodes of static graph when the user adds a filter.
     *
     * @param f               {@link Filter} added
     * @param filters         filters of table
     * @param staticNodes     nodes already filtered
     * @param indexes         id of nodes to filtered
     * @param i               index
     */
    private void changeStyleStaticNodes(Filter f, ArrayList<Filter> filters, ArrayList<Node> staticNodes, ArrayList<Integer> indexes, Integer i) {
        Node n = graphController.getStaticGraph().getNode(String.valueOf(i));
        if (filters.indexOf(f) == 0) {
            if (!staticNodes.contains(n))
                staticNodes.add(n);
            n.setAttribute("ui.class", "filtered");
        } else {
            for (int j = 0; j < staticNodes.size(); j++) {
                if (!indexes.contains(Integer.parseInt(staticNodes.get(j).getId()))) {
                    staticNodes.get(j).removeAttribute("ui.class");
                    staticNodes.remove(n);
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
     * @return          true, if the node is to be showed, or false
     */
    private boolean checkAttribute(String attribute, String operator, double value, String[] vector) {
        double v;
        boolean toShow = false;
        ArrayList<String> attributes = graphController.getColumnsAttributes();
        for (int i = 1; i < attributes.size(); i++) {
            if(attribute.equals(attributes.get(i))) {
                v = Double.parseDouble(vector[i - 1]);
                toShow = checkOperator(operator, v, value);
            }
        }
        return toShow;
    }

    /**
     * Checks which operator is selected and if there are any mismatches.
     *
     * @param operator operator selected
     * @param v        value of node
     * @param value    value of textField
     * @return         true or false
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