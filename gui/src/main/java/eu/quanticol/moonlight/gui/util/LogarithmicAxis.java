package eu.quanticol.moonlight.gui.util;

import eu.quanticol.moonlight.gui.util.IllegalLogarithmicRangeException;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.chart.ValueAxis;
import javafx.util.Duration;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LogarithmicAxis extends ValueAxis<Number> {

    private static final double ANIMATION_TIME = 2000;
    private final Timeline lowerRangeTimeline = new Timeline();
    private final Timeline upperRangeTimeline = new Timeline();
    private final DoubleProperty logUpperBound = new SimpleDoubleProperty();
    private final DoubleProperty logLowerBound = new SimpleDoubleProperty();

    public LogarithmicAxis() {
        super(0.0001,1000);
        bindLogBoundsToDefaultBounds();
    }

    private void validateBounds(double lowerBound, double upperBound) throws IllegalLogarithmicRangeException {
        if (lowerBound < 0 || upperBound < 0 || lowerBound > upperBound) {
            throw new IllegalLogarithmicRangeException(
                    "The logarithmic range should be in [0,Double.MAX_VALUE] and the lowerBound should be less than the upperBound");
        }
    }

    private void bindLogBoundsToDefaultBounds() {
        logLowerBound.bind(new DoubleBinding() {{super.bind(lowerBoundProperty());}
            @Override
            protected double computeValue() {
                return Math.log10(lowerBoundProperty().get());
            }
        });
        logUpperBound.bind(new DoubleBinding() {{super.bind(upperBoundProperty());}

            @Override
            protected double computeValue() {
                return Math.log10(upperBoundProperty().get());
            }
        });
    }

    @Override
    protected List<Number> calculateMinorTickMarks() {
        return new ArrayList<>();
    }

    @Override
    protected void setRange(Object range, boolean animate) {
        if (range != null) {
            Number lowerBound = ((Number[]) range)[0];
            Number upperBound = ((Number[]) range)[1];
            try {
                validateBounds(lowerBound.doubleValue(), upperBound.doubleValue());
            } catch (IllegalLogarithmicRangeException e) {
                e.printStackTrace();
            }
            if (animate) {
                try {
                    getKeysFrames(lowerBound,upperBound);
                } catch (Exception e) {
                    setBoundsProperty(lowerBound,upperBound);
                }
            }
            setBoundsProperty(lowerBound,upperBound);
        }
    }

    private void setBoundsProperty(Number lowerBound, Number upperBound){
        lowerBoundProperty().set(lowerBound.doubleValue());
        upperBoundProperty().set(upperBound.doubleValue());
    }

    private void getKeysFrames(Number lowerBound, Number upperBound){
        lowerRangeTimeline.getKeyFrames().clear();
        upperRangeTimeline.getKeyFrames().clear();
        lowerRangeTimeline.getKeyFrames()
                .addAll(new KeyFrame(Duration.ZERO, new KeyValue(lowerBoundProperty(), lowerBoundProperty()
                                .get())),
                        new KeyFrame(new Duration(ANIMATION_TIME), new KeyValue(lowerBoundProperty(),
                                lowerBound.doubleValue())));

        upperRangeTimeline.getKeyFrames()
                .addAll(new KeyFrame(Duration.ZERO, new KeyValue(upperBoundProperty(), upperBoundProperty()
                                .get())),
                        new KeyFrame(new Duration(ANIMATION_TIME), new KeyValue(upperBoundProperty(),
                                upperBound.doubleValue())));
        lowerRangeTimeline.play();
        upperRangeTimeline.play();
    }

    @Override
    protected double[] getRange() {
        return new double[]{
                getLowerBound(),
                getUpperBound()
        };
    }

    @Override
    protected List<Number> calculateTickValues(double length, Object range) {
        LinkedList<Number> tickPositions = new LinkedList<>();
        if (range != null) {
            double lowerBound = ((double[]) range)[0];
            double upperBound = ((double[]) range)[1];

            for (double i = Math.log10(lowerBound); i <= Math.log10(upperBound); i++) {
                tickPositions.add(Math.pow(10, i));
            }

            if (!tickPositions.isEmpty()) {
                if (tickPositions.getLast().doubleValue() != upperBound) {
                    tickPositions.add(upperBound);
                }
            }
        }

        return tickPositions;
    }

    @Override
    protected String getTickMarkLabel(Number value) {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMaximumIntegerDigits(10);
        formatter.setMinimumIntegerDigits(1);
        return formatter.format(value);
    }

    @Override
    public Number getValueForDisplay(double displayPosition) {
        double delta = logUpperBound.get() - logLowerBound.get();
        if (getSide().isVertical()) {
            return Math.pow(10, (((displayPosition - getHeight()) / -getHeight()) * delta) + logLowerBound.get());
        } else {
            return Math.pow(10, (((displayPosition / getWidth()) * delta) + logLowerBound.get()));
        }
    }

    @Override
    public double getDisplayPosition(Number value) {
        double delta = logUpperBound.get() - logLowerBound.get();
        double deltaV = Math.log10(value.doubleValue()) - logLowerBound.get();
        if (getSide().isVertical()) {
            return (1. - ((deltaV) / delta)) * getHeight();
        } else {
            return ((deltaV) / delta) * getWidth();
        }
    }
}