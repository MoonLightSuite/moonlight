package eu.quanticol.moonlight.examples.subway.parsing;

import eu.quanticol.moonlight.signal.ImmutableGraphModel;

/**
 * Parsing strategy that generates a GraphModel based on
 * a String-based adjacency matrix
 */
public class AdjacencyExtractor implements ParsingStrategy<ImmutableGraphModel<Double>> {
    private double weight;
    private ImmutableGraphModel<Double> graph;
    private int row;

    /**
     * Initialize the adjacency matrix constants
     * (i.e. edge weights, row counter and graph size)
     * @param header the first row of the adjacency matrix
     */
    @Override
    public void initialize(String[] header) {
        this.weight = 1;
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
            if(v == 1)
                graph.add(row, weight, i);
        }

        row++;
    }

    /**
     * @return the graph spatial model generated so far
     */
    @Override
    public ImmutableGraphModel<Double> result() {
        return graph;
    }
}
