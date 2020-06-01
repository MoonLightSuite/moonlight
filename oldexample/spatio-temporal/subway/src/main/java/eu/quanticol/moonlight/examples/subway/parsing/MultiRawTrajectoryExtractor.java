package eu.quanticol.moonlight.examples.subway.parsing;

import eu.quanticol.moonlight.examples.subway.data.HashBiMap;
import eu.quanticol.moonlight.examples.subway.data.MultiValuedSignal;

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
public class MultiRawTrajectoryExtractor implements ParsingStrategy<Collection<MultiValuedSignal>> {
    private final int spaceNodes;
    private int timePoints;
    private final SignalProcessor<Float> processor;

    private int signalCount = 0;

    // List<[Space][Time]> signals
    private List<MultiValuedSignal> signals;

    // [Space][Time] signal
    private Float[][] singleSignal;

    private int spaceIterator;

    /**
     * Initializes the spatial dimension of the spatio-temporal signals
     * @param spaceSize dimension of the space of interest
     * @param proc Signal factory specific to the input and output types
     */
    public MultiRawTrajectoryExtractor(int spaceSize, SignalProcessor<Float> proc) {
        spaceNodes = spaceSize;
        processor = proc;
    }

    /**
     * Initializes the temporal dimension and
     * the list representing the sequence of spatio-temporal signals
     * @param header an array of strings containing the names of the time points
     */
    @Override
    public void initialize(String[] header) {
        timePoints = header.length;
        spaceIterator = 0;
        signals = new ArrayList<>();
        singleSignal = new Float[spaceNodes][timePoints];

        processor.initializeSpaceTime(spaceNodes, timePoints);
    }

    /**
     * Takes a String array representing a time series and updates the internal
     * structure of the (current) spatial-temporal signal.
     * It must also understand when a new signal is starting
     * It usually requires that the extractor has been previously initialized:
     * @see MultiRawTrajectoryExtractor#initialize(String[] header)
     *
     * @param data an array of strings to be processed
     */
    @Override
    public void process(String[] data) {

        for (int i = 0; i < timePoints; i++) {
            singleSignal[spaceIterator][i] = Float.parseFloat(data[i]);
        }

        spaceIterator++;

        if(spaceIterator == spaceNodes) {

            signalCount++;
            // When a signal is complete, we store the values
            signals.add(processor.generateSignal(singleSignal));

            // Then we reset
            spaceIterator = 0;
            singleSignal = new Float[spaceNodes][timePoints];

            System.out.println("Completed Processing of Signal " + signalCount);
        }
    }

    /**
     * @throws IllegalArgumentException if the input file had a number of points
     * not exactly divisible for the number of space nodes.
     *
     * @return the spatial-temporal signal containing the values added so far
     */
    @Override
    public Collection<MultiValuedSignal> result() {
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
