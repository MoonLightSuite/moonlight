package io.github.moonlightsuite.moonlight.io.parsing;

/**
 * Parsing strategy that generates a double[][] spatial-temporal signal
 * based on a set of String-based time series
 *
 * @see MultiRawTrajectoryExtractor for importing multiple trajectories from the
 * same file.
 * @see ParsingStrategy
 */
public class RawTrajectoryExtractor
        implements ParsingStrategy<double[][]>, PrintingStrategy<double[][]>
{
    private final int spaceNodes;
    private int timePoints;
    private int spaceLocations;
    private double[][] signal;  // [Space][Time] signal

    private int spaceIterator;

    /**
     * Initializes the spatial dimension of the spatio-temporal signal
     * @param spaceSize dimension of the space of interest
     */
    public RawTrajectoryExtractor(int spaceSize) {
        spaceNodes = spaceSize;
    }

    /**
     * Initializes the temporal dimension and
     * the matrix representing the spatio-temporal signal
     * @param header an array of strings containing the names of the time points
     */
    @Override
    public void initialize(String[] header) {
        timePoints = header.length;
        spaceIterator = 0;
        signal = new double[spaceNodes][timePoints];
    }

    /**
     * Takes a String array representing a time series and updates the internal
     * structure of the spatio-temporal signal
     * @param data an array of strings to be processed
     */
    @Override
    public void process(String[] data) {
        if (spaceIterator >= spaceNodes)
            throw new UnsupportedOperationException
                    ("Trajectory bigger than spatial dimension. " +
                     "Are you importing multiple trajectories?");

        for (int i = 0; i < data.length; i++) {
            signal[spaceIterator][i] = Double.parseDouble(data[i]);
        }

        spaceIterator++;
    }


    @Override
    public String initialize(double[][] header, String wordBreak) {
        spaceIterator = 0;
        spaceLocations = header.length;

        StringBuilder head = new StringBuilder("\"Space\"" + wordBreak);
        head.append("\"T").append(0).append("\"");
        for(int i = 1; i < header[0].length; i++) {
            head.append(wordBreak).append("\"T").append(i).append("\"");
        }

        return head.toString();
    }

    @Override
    public String print(double[][] data, String wordBreak) {
        StringBuilder line = new StringBuilder("\"S" + spaceIterator +
                                               "\"" + wordBreak);

        if(spaceIterator == spaceLocations)
            return null;

        line.append(data[spaceIterator][0]);
        for(int i = 1; i < data[spaceIterator].length; i++) {
            line.append(wordBreak).append(data[spaceIterator][i]);
        }

        spaceIterator++;
        return line.toString();
    }

    @Override
    public boolean isComplete() {
        return spaceIterator == spaceLocations;
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
