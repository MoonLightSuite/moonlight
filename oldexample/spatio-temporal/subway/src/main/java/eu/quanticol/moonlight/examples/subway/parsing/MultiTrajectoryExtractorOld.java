package eu.quanticol.moonlight.examples.subway.parsing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Parsing strategy that generates a collection of double[][] spatio-temporal
 * signals based on a set of String-based time series
 *
 * @see TrajectoryExtractor for importing a single trajectory from a file.
 */
public class MultiTrajectoryExtractorOld implements ParsingStrategy<Collection<double[][]>> {
    private int spaceNodes;
    private int timePoints;
    private List<double[][]> signal;  // List of [Space][Time]> signals
    private double[][] tSignal;       // [Space][Time] signal

    private int spaceIterator;

    /**
     * Initializes the spatial dimension of the spatio-temporal signals
     * @param spaceSize dimension of the space of interest
     */
    public MultiTrajectoryExtractorOld(int spaceSize) {
        spaceNodes = spaceSize;
    }

    /**
     * Initializes the temporal dimension and
     * the list representing the sequence of spatio-temporal signals
     * @param header an array of strings containing the names of the time points
     */
    @Override
    public void initialize(String[] header) {
        this.timePoints = header.length;
        this.spaceIterator = 0;
        signal = new ArrayList<>();
        tSignal = new double[spaceNodes][timePoints];
    }

    /**
     * Takes a String array representing a time series and updates the internal
     * structure of the (current) spatio-temporal signal.
     * It must also understand when a new signal is starting
     * @param data an array of strings to be processed
     */
    @Override
    public void process(String[] data) {

        for (int i = 0; i < data.length; i++) {
            tSignal[spaceIterator][i] = Double.parseDouble(data[i]);
        }

        spaceIterator++;

        if(spaceIterator == spaceNodes) {
            // When a signal is complete, we store the values and reset
            signal.add(tSignal);
            spaceIterator = 0;
            tSignal = new double[spaceNodes][timePoints];
        }
    }

    /**
     * @throws IllegalArgumentException if the input file had a number of points
     * not exactly divisible for the number of space nodes.
     *
     * @return the spatio-temporal signal containing the values added so far
     */
    @Override
    public Collection<double[][]> result() {
        if(spaceIterator > 0)
            throw new IllegalArgumentException ("It seems the input file was " +
                    "missing parts of some trajectories. Please correct the "  +
                    "input and retry!");

        return signal;
    }
}
