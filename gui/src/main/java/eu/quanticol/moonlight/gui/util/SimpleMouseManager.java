package eu.quanticol.moonlight.gui.util;

import eu.quanticol.moonlight.gui.chart.JavaFXChartController;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.util.InteractiveElement;
import org.graphstream.ui.view.util.MouseManager;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.EnumSet;
import java.util.Optional;

/**
 * Class that implements the {@link MouseManager} interface to manage mouse events on a graph
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class SimpleMouseManager implements MouseManager {
    private View view;
    private GraphicGraph gGraph;
    final private EnumSet<InteractiveElement> types;
    private double time = 0;
    private Graph graph;
    private PropertyChangeSupport propertyChangeSupport;
    private String label = "";
    private JavaFXChartController chartController;

    public SimpleMouseManager(EnumSet<InteractiveElement> types) {
        this.types = types;
    }

    public SimpleMouseManager(Graph graph, Double time, JavaFXChartController chartController) {
        this(EnumSet.of(InteractiveElement.NODE, InteractiveElement.SPRITE));
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.time = time;
        this.graph = graph;
        this.chartController = chartController;
    }

    public SimpleMouseManager(Graph graph, JavaFXChartController chartController) {
        this(EnumSet.of(InteractiveElement.NODE, InteractiveElement.SPRITE));
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.graph = graph;
        this.chartController = chartController;
    }

    /**
     * Set the value of the label about node info
     *
     * @param text info to display
     */
    public void setLabel(String text) {
        String oldText = this.label;
        this.label = text;
        propertyChangeSupport.firePropertyChange("LabelProperty", oldText, text);
    }

    /**
     * Add a listener to a property that changes
     *
     * @param listener listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Initialize all the listeners, the view, and the graph
     * @param graph a graphical graph
     * @param view the view used
     */
    public void init(GraphicGraph graph, View view) {
        this.view = view;
        this.gGraph = graph;
        view.addListener(MouseEvent.MOUSE_PRESSED, mousePressed);
        view.addListener(MouseEvent.MOUSE_RELEASED, mouseRelease);
        view.addListener(MouseEvent.MOUSE_CLICKED, mouseClicked);
    }

    private void mouseButtonPress(MouseEvent event) {
        view.requireFocus();
        if (!event.isShiftDown()) {
            gGraph.nodes().filter(n -> n.hasAttribute("ui.selected")).forEach(n -> n.removeAttribute("ui.selected"));
            gGraph.sprites().filter(s -> s.hasAttribute("ui.selected")).forEach(s -> s.removeAttribute("ui.selected"));
            gGraph.edges().filter(e -> e.hasAttribute("ui.selected")).forEach(e -> e.removeAttribute("ui.selected"));
        }
    }

    private void mouseButtonRelease(Iterable<GraphicElement> elementsInArea) {
        for (GraphicElement element : elementsInArea) {
            if (!element.hasAttribute("ui.selected"))
                element.setAttribute("ui.selected");
        }
    }

    private void mouseButtonPressOnElement(GraphicElement element,
                                           MouseEvent event) {
        view.freezeElement(element, true);
        if (event.getButton() == MouseButton.SECONDARY) {
            element.setAttribute("ui.selected");
        } else {
            element.setAttribute("ui.clicked");
        }
    }

    private void mouseButtonReleaseOffElement(GraphicElement element,
                                              MouseEvent event) {
        view.freezeElement(element, false);
        if (event.getButton() != MouseButton.SECONDARY) {
            element.removeAttribute("ui.clicked");
        }
    }

    private GraphicElement curElement;

    private double x1, y1;

    private final EventHandler<MouseEvent> mousePressed = new EventHandler<>() {
        @Override
        public void handle(MouseEvent e) {
            curElement = view.findGraphicElementAt(types, e.getX(), e.getY());
            if (curElement != null) {
                mouseButtonPressOnElement(curElement, e);
            } else {
                x1 = e.getX();
                y1 = e.getY();
                mouseButtonPress(e);
                view.beginSelectionAt(x1, y1);
            }
        }
    };

    /**
     * Blocks the drag behaviour of nodes
     */
    private final  EventHandler<MouseEvent> mouseDragged = event -> {
    };

    private final EventHandler<MouseEvent> mouseRelease = new EventHandler<>() {
        @Override
        public void handle(MouseEvent event) {
            if (curElement != null) {
                mouseButtonReleaseOffElement(curElement, event);
                curElement = null;
            } else {
                double x2 = event.getX();
                double y2 = event.getY();
                double t;
                if (x1 > x2) {
                    t = x1;
                    x1 = x2;
                    x2 = t;
                }
                if (y1 > y2) {
                    t = y1;
                    y1 = y2;
                    y2 = t;
                }
                mouseButtonRelease(view.allGraphicElementsIn(types, x1, y1, x2, y2));
                view.endSelectionAt(x2, y2);
            }
        }
    };

    @Override
    public void release() {
        view.removeListener(MouseEvent.MOUSE_PRESSED, mousePressed);
        view.removeListener(MouseEvent.MOUSE_RELEASED, mouseRelease);
    }

    @Override
    public EnumSet<InteractiveElement> getManagedTypes() {
        return types;
    }

    /**
     * Handler for a single or double click.
     * If it is clicked a node, it is displayed its information and/or selected the corresponding series in the chart
     * If it is clicked outside a node, the chart series are restored
     */
    EventHandler<MouseEvent> mouseClicked = new EventHandler<>() {
        @Override
        public void handle(MouseEvent e) {
            curElement = view.findGraphicElementAt(types, e.getX(), e.getY());
            if (curElement != null) {
                if (e.getButton().equals(MouseButton.PRIMARY)) {
                    if (e.getClickCount() == 1) {
                        mouseButtonClickOnElement(curElement, e);
                    }
                    if (e.getClickCount() > 1) {
                        mouseButtonTwoClickOnElement(curElement, e);
                    }
                }
            } else {
                selectAll();
            }
        }
    };

    private void selectAll() {
        setLabel(" ");
        chartController.selectAllSeries();
    }

    /**
     * Select series in a chart of the corresponding clicked node
     *
     * @param curElement current element
     * @param e          mouseEvent
     */
    private void mouseButtonTwoClickOnElement(GraphicElement curElement, MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            curElement.setAttribute("ui.clicked");
            Optional<Node> n1 = gGraph.nodes().filter(n -> n.hasAttribute("ui.clicked")).findFirst();
            if (n1.isPresent()) {
                Node n = graph.getNode(n1.get().getId());
                selectOnlyNodeSeries(n);
            }
            curElement.removeAttribute("ui.clicked");
        }
    }

    private void selectOnlyNodeSeries(Node n) {
        chartController.selectOnlyOneSeries("Node " + n.getId());
    }

    /**
     * Displays attributes of a clicked node
     *
     * @param element        current element
     * @param event          mouseEvent
     */
    protected void mouseButtonClickOnElement(GraphicElement element,
                                             MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            element.setAttribute("ui.clicked");
            Optional<Node> n1 = gGraph.nodes().filter(n -> n.hasAttribute("ui.clicked")).findFirst();
            if (n1.isPresent()) {
                Node n = graph.getNode(n1.get().getId());
                Object attribute = n.getAttribute("time" + this.time);
                if(attribute != null) {
                    String s = attribute.toString();
                    String[] list = s.substring(1, s.length() - 1).split(", ");
                    String newLabel = "Node " + n.getId() + " attributes:  x: " + list[0] + ", y: " + list[1] + ", direction: " + list[2] + ", speed: " + list[3] + ", v: " + list[4];
                    setLabel(newLabel);
                } else if((attribute = n.getAttribute("Attributes")) != null) {
                    String s = attribute.toString();
                    String[] list = s.substring(1, s.length() - 1).split(", ");
                    String newLabel = "Node " + n.getId() + " attributes:  x: " + list[0] + ", y: " + list[1] + ", v: " + list[2];
                    setLabel(newLabel);
                }
            }
            element.removeAttribute("ui.clicked");
        }
    }
}
