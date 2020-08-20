package tutorial_utilities;

import eu.quanticol.jsstl.core.formula.Signal;
import eu.quanticol.jsstl.core.io.SyntaxErrorExpection;
import eu.quanticol.jsstl.core.io.TraGraphModelReader;
import eu.quanticol.jsstl.core.space.GraphModel;

import java.io.IOException;

public class SpatialTemporalSimHyA {

    private final SimHyAWrapper model = new SimHyAWrapper();
    private final String graphFile;
    private GraphModel graph;
    private int locations;

    public SpatialTemporalSimHyA() {
        graphFile = null;
    }

    public SpatialTemporalSimHyA(String graphFile, int locations) {
        this.graphFile = graphFile;
        this.locations = locations;
    }

    public void loadModel(String modelFile) {
        model.loadModel(modelFile);
        model.setGB();
        if(graphFile != null) {
            try {
                GraphModel g = new TraGraphModelReader().read(graphFile);
                g.dMcomputation();
                this.graph = g;
            } catch (IOException | SyntaxErrorExpection e) {
                System.out.println("Unable to load the Spatial Graph");
                e.printStackTrace();
            }
        } else {
            locations = model.getFlatModel().getVariablesValues().length;
            this.graph = createGraphForGrid(locations, 1);
        }
    }

    public GraphModel getGraphModel() {
        return graph;
    }

    public Signal simulate(double tfinal, int timepoints) {
        double[][] traj = model.simulate(tfinal, timepoints);

        int timeLength = traj[0].length;

        for (int i = 1; i < traj[0].length; i++) {
            if (traj[0][i] == 0) {
                timeLength = i;
            }

        }
        final double[] times = new double[timeLength];
        System.arraycopy(traj[0], 0, times, 0, timeLength);

        //final double[] times = traj[0];
        final double[][][] data = new double[locations][times.length][2];
        for (int i = 0; i < locations; i++)
            for (int t = 0; t < times.length; t++) {
                data[i][t][0] = traj[i + 1][t];
                data[i][t][0] = traj[i + locations + 1][t];
            }
        return new Signal(graph, times, data);
    }

    protected static GraphModel createGraphForGrid(int xMax, int yMax) {
        GraphModel graph = new GraphModel();
        for (int yCoord = 0; yCoord < yMax; yCoord++)
            for (int xCoord = 0; xCoord < xMax; xCoord++) {
                int position = yCoord * xMax + xCoord;
                // System.out.println(position);
                String label = "l" + Integer.toString(position);
                graph.addLoc(label, position);
            }
        for (int yCoord = 0; yCoord < yMax - 1; yCoord++) {
            for (int xCoord = 0; xCoord < xMax - 1; xCoord++) {
                int position = yCoord * xMax + xCoord;
                graph.addEdge(position, position + 1, 1);
                graph.addEdge(position, position + xMax, 1);
            }
            graph.addEdge(yCoord * xMax + xMax - 1, yCoord * xMax + xMax - 1
                    + xMax, 1);
        }
        for (int position = (yMax - 1) * xMax; position < (xMax * yMax) - 1; position++)
            graph.addEdge(position, position + 1, 1);
        // // Computation of the distance matrix
        graph.dMcomputation();
        return graph;
    }

}

