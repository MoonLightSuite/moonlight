package eu.quanticol.moonlight.examples.subway.data;

import eu.quanticol.moonlight.io.parsing.ParsingStrategy;
import eu.quanticol.moonlight.io.parsing.RawTrajectoryExtractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Parsing strategy that generates a collection of spatial-temporal
 * signals based on a set of String-based time series.
 * The output is represented as a Collection of HashBiMaps,
 * s.t. (Integer space, Integer time) -> Double value
 *
 * @see HashBiMap for more info about the internal data structure.
 * @see RawTrajectoryExtractor for importing a single trajectory from a file.
 */
public class MultiHashTrajectoryExtractor implements ParsingStrategy<Collection<HashBiMap<Integer, Integer, Double>>> {
    private final int spaceNodes;
    private int timePoints;

    // List<[Space][Time]> signals
    private List<HashBiMap<Integer, Integer, Double>> signals;

    // [Space][Time] signal
    private HashBiMap<Integer, Integer, Double> singleSignal;

    private int spaceIterator;

    /**
     * Initializes the spatial dimension of the spatio-temporal signals
     * @param spaceSize dimension of the space of interest
     */
    public MultiHashTrajectoryExtractor(int spaceSize) {
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
        signals = new ArrayList<>();
        singleSignal = new HashBiMap<>();
    }

    /**
     * Takes a String array representing a time series and updates the internal
     * structure of the (current) spatial-temporal signal.
     * It must also understand when a new signal is starting
     * It usually requires that the extractor has been previously initialized:
     * @see MultiHashTrajectoryExtractor#initialize(String[] header)
     *
     * @param data an array of strings to be processed
     */
    @Override
    public void process(String[] data) {

        for (int i = 0; i < timePoints; i++) {
            singleSignal.put(spaceIterator, i, Double.parseDouble(data[i]));
        }

        spaceIterator++;

        if(spaceIterator == spaceNodes) {
            // When a signal is complete, we store the values and reset
            signals.add(singleSignal);
            spaceIterator = 0;
            singleSignal = new HashBiMap<>();
        }
    }

    /**
     * @throws IllegalArgumentException if the input file had a number of points
     * not exactly divisible for the number of space nodes.
     *
     * @return the spatial-temporal signal containing the values added so far
     */
    @Override
    public Collection<HashBiMap<Integer, Integer, Double>> result() {
        if(spaceIterator > 0)
            throw new IllegalArgumentException ("It seems the input file was " +
                    "missing parts of some trajectories. Please correct the "  +
                    "input and retry!");

        return signals;
    }

    /**
     * @return the number of samples expected to be in each trajectory
     */
    public int getTimePoints() {
        return timePoints;
    }
}
