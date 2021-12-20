package eu.quanticol.moonlight.gui.graph;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleGraphControllerTest {

    final GraphController graphController = SimpleGraphController.getInstance();
    final List<TimeGraph> timeGraphList = new ArrayList<>();
    final File fileStatic = new File((Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("static.tra"))).getFile());
    final File fileDynamic = new File((Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("dynamic.tra"))).getFile());

    @Test
    void createNodesVectorTest() throws IOException {
        graphController.setGraphList(timeGraphList);
        graphController.createGraphFromFile(fileDynamic);
        ArrayList<String> attributes = new ArrayList<>();
        attributes.add("time");
        attributes.add("x");
        attributes.add("y");
        attributes.add("direction");
        attributes.add("speed");
        attributes.add("v");
        graphController.setColumnsAttributes(attributes);
        String line = "0,3,17,1,0.8,0,14,13,3,0.6,0,9,13,2,0.05,0,25,16,5,0.1,0,14,16,5,0.6,0";
        graphController.createNodesVector(line,"x","y", true);
        List<Node> nodesWithVector = new ArrayList<>();
        graphController.getGraphList().forEach(g -> {
            for (int i = 0; i < 5; i++)
                if(g.getGraph().getNode(String.valueOf(i)).hasAttribute("time" + 0.0))
                    nodesWithVector.add(g.getGraph().getNode(String.valueOf(i)));
        });
        assertTrue(nodesWithVector.stream().allMatch(n -> n.hasAttribute("x")));
        assertTrue(nodesWithVector.stream().allMatch(n -> n.hasAttribute("y")));
        assertEquals(5,nodesWithVector.size());
        String vectorNode0 = graphController.getGraphList().get(0).getGraph().getNode(String.valueOf(0)).getAttribute("time" + 0.0).toString();
        assertEquals("3, 17, 1, 0.8, 0",vectorNode0.replaceAll("\\[", "").replaceAll("]",""));
    }

    @Test
    void createPositionsTest() throws IOException {
        graphController.createGraphFromFile(fileStatic);
        ArrayList<String> attributes = new ArrayList<>();
        attributes.add("time");
        attributes.add("x");
        attributes.add("y");
        attributes.add("direction");
        attributes.add("speed");
        attributes.add("v");
        graphController.setColumnsAttributes(attributes);
        String line = "0,3,17,1,0,0,14,19,3,0,0";
        graphController.createPositions(line,"x","y");
        Graph graph = graphController.getStaticGraph();
        String id = String.valueOf(0);
        String id1 = String.valueOf(1);
        assertTrue(graph.getNode(id).hasAttribute("x"));
        assertTrue(graph.getNode(id1).hasAttribute("x"));
        assertTrue(graph.getNode(id).hasAttribute("y"));
        assertTrue(graph.getNode(id1).hasAttribute("y"));
        assertEquals(graph.getNode(id).getAttribute("x"),"3");
        assertEquals(graph.getNode(id1).getAttribute("x"),"14");
        assertEquals(graph.getNode(id).getAttribute("y"),"17");
        assertEquals(graph.getNode(id1).getAttribute("y"),"19");
    }

    @Test
    void createGraphFromFileTest() throws IOException {
        GraphType graphType = graphController.createGraphFromFile(fileStatic);
        assertEquals(GraphType.STATIC, graphType);
        graphController.setGraphList(timeGraphList);
        GraphType graphType1 = graphController.createGraphFromFile(fileDynamic);
        assertEquals(GraphType.DYNAMIC, graphType1);
        assertEquals(1, graphController.getGraphList().size());
        TimeGraph graph = graphController.getGraphList().get(0);
        assertEquals(5,graph.getGraph().getNodeCount());
        Node n0 = graphController.getGraphList().get(0).getGraph().getNode(String.valueOf(0));
        Node n1 = graphController.getGraphList().get(0).getGraph().getNode(String.valueOf(1));
        assertEquals(n0,graphController.getGraphList().get(0).getGraph().getEdge("id" + 1).getSourceNode());
        assertEquals(n1,graphController.getGraphList().get(0).getGraph().getEdge("id" + 1).getTargetNode());
    }

    @Test
    @AfterEach
    void resetList(){
        timeGraphList.clear();
        graphController.getColumnsAttributes().clear();
    }
}