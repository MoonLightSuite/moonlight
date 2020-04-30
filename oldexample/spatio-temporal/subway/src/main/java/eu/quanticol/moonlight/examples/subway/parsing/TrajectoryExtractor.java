package eu.quanticol.moonlight.examples.subway.parsing;

/**
 * Parsing strategy that generates a double[][] spatio-temporal signal
 * based on a set of String-based time series
 *
 * @see MultiTrajectoryExtractor for importing multiple trajectories from the
 * same file.
 */
public class TrajectoryExtractor implements ParsingStrategy<double[][]> {
    private final int spaceNodes;
    private int timePoints;
    private double[][] signal;  // [Space][Time] signal

    private int spaceIterator;

    /**
     * Initializes the spatial dimension of the spatio-temporal signal
     * @param spaceSize dimension of the space of interest
     */
    public TrajectoryExtractor(int spaceSize) {
        spaceNodes = spaceSize;
    }

    /**
     * Initializes the temporal dimension and
     * the matrix representing the spatio-temporal signal
     * @param header an array of strings containing the names of the time points
     */
    @Override
    public void initialize(String[] header) {
        this.timePoints = header.length;
        this.spaceIterator = 0;
        signal = new double[spaceNodes][timePoints];
    }

    /**
     * Takes a String array representing a time series and updates the internal
     * structure of the spatio-temporal signal
     * @param data an array of strings to be processed
     */
    @Override
    public void process(String[] data) {
        if(spaceIterator >= spaceNodes)
            throw new UnsupportedOperationException
                    ("Trajectory bigger than spatial dimension. Are you importing multiple trajectories?");

        for (int i = 0; i < data.length; i++) {
            signal[spaceIterator][i] = Double.parseDouble(data[i]);
        }

        spaceIterator++;
    }

    /**
     * @return the spatio-temporal signal containing the values added so far
     */
    @Override
    public double[][] result() {
        return signal;
    }

    /**
     * @return the number of samples expected to be in the trajectory
     */
    public int getTimePoints() {
        return timePoints;
    }
}
