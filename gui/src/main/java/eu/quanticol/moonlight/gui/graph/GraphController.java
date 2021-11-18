package eu.quanticol.moonlight.gui.graph;

import org.graphstream.graph.Graph;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface GraphController {

    List<TimeGraph> getGraphList();

    int getTotNodes();

    Graph getStaticGraph();

    void setGraphList(List<TimeGraph> graphList);

    void createNodesVector(String line);

    void createPositions(String line);

    void getNodesValues(String line);

    GraphType createGraphFromFile(File file) throws IOException;
}
