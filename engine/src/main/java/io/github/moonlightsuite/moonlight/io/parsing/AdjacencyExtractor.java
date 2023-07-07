package io.github.moonlightsuite.moonlight.io.parsing;

import io.github.moonlightsuite.moonlight.space.ImmutableGraphModel;
import io.github.moonlightsuite.moonlight.core.space.SpatialModel;

/**
 * Parsing strategy that generates a GraphModel based on
 * a String-based adjacency matrix
 */
public class AdjacencyExtractor implements ParsingStrategy<SpatialModel<Double>> {
    private ImmutableGraphModel<Double> graph;
    private int row;

    /**
     * Initialize the adjacency matrix constants
     * (i.e. edge weights, row counter and graph size)
     * @param header the first row of the adjacency matrix
     */
    @Override
    public void initialize(String[] header) {
        this.graph = new ImmutableGraphModel<>(header.length);
        this.row = 0;
    }

    /**
     * Takes a row of the adjacency matrix and generates related graph edges
     * @param data a row of the adjacency matrix
     */
    @Override
    public void process(String[] data) {
        for(int i = 0; i < data.length; i++) {
            double v = Double.parseDouble(data[i]);
            if(v != 0)
                graph = graph.add(row, v, i);
        }

        row++;
        //System.out.println("Processed row:" + row);
    }

    /**
     * @return the graph spatial model generated so far
     */
    @Override
    public ImmutableGraphModel<Double> result() {
        return graph;
    }
}
