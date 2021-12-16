package eu.quanticol.moonlight.gui.graph;

import org.graphstream.graph.Graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface that defines a controller for graphs
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public interface GraphController {

    List<TimeGraph> getGraphList();

    ArrayList<String> getColumnsAttributes();

    void setColumnsAttributes(ArrayList<String> columnsAttributes);

    int getTotNodes();

    Graph getStaticGraph();

    void setGraphList(List<TimeGraph> graphList);

    void createNodesVector(String line);

    void createPositions(String line);

    GraphType createGraphFromFile(File file) throws IOException;
}

