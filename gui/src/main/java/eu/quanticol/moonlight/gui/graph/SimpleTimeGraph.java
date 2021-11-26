package eu.quanticol.moonlight.gui.graph;

import org.graphstream.graph.Graph;

/**
 * Class that implements the {@link TimeGraph} interface and defines a graph with a time instant
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class SimpleTimeGraph implements TimeGraph {

    private final Graph graph;
    private final double time;

    public SimpleTimeGraph(Graph graph, double time) {
        this.graph = graph;
        this.time = time;
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    @Override
    public double getTime() {
        return time;
    }

    @Override
    public Graph getGraphFromTime(double time) {
        if (this.time == time)
            return this.graph;
        else return null;
    }

}
