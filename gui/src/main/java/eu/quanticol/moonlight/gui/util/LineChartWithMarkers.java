package eu.quanticol.moonlight.gui.util;

import javafx.beans.InvalidationListener;
import javafx.beans.NamedArg;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.shape.Line;

import java.util.Objects;

/**
 * Class that creates a LineChart with a vertical marker. It extends the {@link LineChart} class
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class LineChartWithMarkers<X,Y> extends LineChart<X,Y>{

    private final ObservableList<Data<X,Y>> verticalMarkers;

    public LineChartWithMarkers(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
        super(xAxis, yAxis);
       verticalMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.XValueProperty()});
       verticalMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
    }

    @SuppressWarnings("unchecked")
    public LineChartWithMarkers() {
        super((Axis<X>) new NumberAxis(), (Axis<Y>) new NumberAxis());
        verticalMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.XValueProperty()});
        verticalMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
    }

    /**
     * Adds a vertical line
     *
     * @param marker   marker data
     */
    public void addVerticalValueMarker(Data<X,Y> marker) {
        Objects.requireNonNull(marker);
        if (verticalMarkers.contains(marker))
            return;
        Line line = new Line();
        marker.setNode(line);
        getPlotChildren().add(line);
        verticalMarkers.add(marker);
    }

    /**
     * Modifies the layoutPlotChildren method of {@link LineChart} class
     */
    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        for (Data<X,Y> verticalMarker : verticalMarkers) {
            Line line = (Line) verticalMarker.getNode();
            line.setStartX(getXAxis().getDisplayPosition( verticalMarker.getXValue()));
            line.setEndX(line.getStartX());
            line.setStartY(0d);
            line.setEndY(getBoundsInLocal().getHeight());
            line.toFront();
        }
    }
}