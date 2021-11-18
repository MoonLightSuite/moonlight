package eu.quanticol.moonlight.gui.graph;

import org.graphstream.graph.Graph;

/**
 * A graph with a time instant
 */
public interface TimeGraph {

    Graph getGraph();

    double getTime();

    Graph getGraphFromTime(double time);

}